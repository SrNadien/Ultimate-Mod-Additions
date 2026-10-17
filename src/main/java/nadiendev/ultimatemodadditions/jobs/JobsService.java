package nadiendev.ultimatemodadditions.jobs;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Payout;
import nadiendev.ultimatemodadditions.data.JobAction;
import nadiendev.ultimatemodadditions.data.JobDefinition;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.registry.UMAAttachments;
import nadiendev.ultimatemodadditions.network.payload.JobsSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class JobsService {

    private JobsService() {
    }

    public static Map<ResourceLocation, JobProgress> progress(ServerPlayer player) {
        return player.getData(UMAAttachments.JOBS);
    }

    public static Component displayName(ResourceLocation id, JobDefinition job) {
        return Component.literal(job.name().isEmpty() ? id.getPath() : job.name());
    }

    public static boolean join(ServerPlayer player, ResourceLocation id) {
        Map<ResourceLocation, JobProgress> jobs = progress(player);
        if (jobs.containsKey(id) || !UMAData.jobs().containsKey(id)) {
            return false;
        }
        if (jobs.size() >= UMAConfig.COMMON.jobsMaxPerPlayer.get()) {
            return false;
        }
        jobs.put(id, JobProgress.start());
        JobsBoard.get(player.server).update(id, player.getUUID(), player.getGameProfile().getName(),
                JobProgress.start());
        return true;
    }

    public static boolean leave(ServerPlayer player, ResourceLocation id) {
        boolean removed = progress(player).remove(id) != null;
        if (removed) {
            JobsBoard.get(player.server).remove(id, player.getUUID());
        }
        return removed;
    }

    public static void sync(ServerPlayer player, boolean open) {
        Map<ResourceLocation, JobProgress> mine = progress(player);
        List<JobsSyncPayload.JobView> views = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JobDefinition> entry : UMAData.jobs().entrySet()) {
            JobDefinition job = entry.getValue();
            JobProgress progress = mine.get(entry.getKey());
            List<JobsSyncPayload.ActionView> actions = new ArrayList<>();
            for (JobAction action : job.actions()) {
                actions.add(new JobsSyncPayload.ActionView(action.type(), label(action), action.pay()));
            }
            int level = progress == null ? 1 : progress.level();
            List<JobsSyncPayload.TitleView> titles = new ArrayList<>();
            for (JobDefinition.Title title : job.titles()) {
                titles.add(new JobsSyncPayload.TitleView(title.level(), title.name()));
            }
            List<JobsSyncPayload.RewardView> rewards = new ArrayList<>();
            for (JobDefinition.Reward reward : job.rewards()) {
                List<ItemStack> items = new ArrayList<>();
                for (JobDefinition.RewardItem item : reward.items()) {
                    items.add(new ItemStack(item.item(), item.count()));
                }
                rewards.add(new JobsSyncPayload.RewardView(reward.level(), reward.every(), reward.money(), items));
            }
            views.add(new JobsSyncPayload.JobView(entry.getKey(),
                    displayName(entry.getKey(), job).getString(), job.description(), job.icon(), job.maxLevel(),
                    progress != null, level, progress == null ? 0.0D : progress.xp(),
                    job.xpNeeded(level), job.titleFor(level), titles, rewards, actions));
        }
        PacketDistributor.sendToPlayer(player,
                new JobsSyncPayload(open, UMAConfig.COMMON.jobsMaxPerPlayer.get(), views));
    }

    public static Component label(JobAction action) {
        String target = action.target();
        if (target.isEmpty()) {
            return Component.translatable("screen.ultimatemodadditions.job_anything");
        }
        if (target.startsWith("#")) {
            return Component.literal(target.substring(1).replaceFirst("^[^:]*:", ""));
        }
        ResourceLocation id = ResourceLocation.tryParse(target);
        if (id == null) {
            return Component.literal(target);
        }
        return switch (action.type()) {
            case BREAK_BLOCK, PLACE_BLOCK -> BuiltInRegistries.BLOCK.containsKey(id)
                    ? BuiltInRegistries.BLOCK.get(id).getName() : Component.literal(target);
            case KILL_ENTITY, BREED, TAME -> BuiltInRegistries.ENTITY_TYPE.containsKey(id)
                    ? BuiltInRegistries.ENTITY_TYPE.get(id).getDescription() : Component.literal(target);
            case FISH, CRAFT, SMELT, BREW, ENCHANT -> BuiltInRegistries.ITEM.containsKey(id)
                    ? BuiltInRegistries.ITEM.get(id).getDescription() : Component.literal(target);
            case EXPLORE -> Component.translatable("screen.ultimatemodadditions.job_anything");
        };
    }

    private static long today() {
        return System.currentTimeMillis() / 86_400_000L;
    }

    public static void award(ServerPlayer player, JobAction.Type type, Predicate<JobAction> matcher) {
        award(player, type, 1, matcher);
    }

    public static void award(ServerPlayer player, JobAction.Type type, int times, Predicate<JobAction> matcher) {
        if (!UMAConfig.COMMON.jobsEnabled.get() || times <= 0) {
            return;
        }
        if (UMAConfig.COMMON.jobsIgnoreCreative.get() && player.isCreative()) {
            return;
        }
        Map<ResourceLocation, JobProgress> jobs = progress(player);
        if (jobs.isEmpty()) {
            return;
        }
        double income = UMAConfig.COMMON.jobsIncomeMultiplier.get();
        double experience = UMAConfig.COMMON.jobsXpMultiplier.get();
        double limit = UMAConfig.COMMON.jobsDailyLimit.get();
        long day = today();
        double mangos = 0.0D;
        JobsBoard board = JobsBoard.get(player.server);
        JobsBoosts boosts = JobsBoosts.get(player.server);

        for (Map.Entry<ResourceLocation, JobProgress> entry : new ArrayList<>(jobs.entrySet())) {
            JobDefinition job = UMAData.jobs().get(entry.getKey());
            if (job == null) {
                continue;
            }
            JobAction hit = null;
            for (JobAction action : job.actions()) {
                if (action.type() == type && matcher.test(action)) {
                    hit = action;
                    break;
                }
            }
            if (hit == null) {
                continue;
            }
            JobProgress current = entry.getValue();
            double boost = boosts.multiplier(entry.getKey().toString());
            double pay = hit.pay() * times * income * job.incomeFactor(current.level());
            if (limit > 0.0D) {
                pay = Math.max(0.0D, Math.min(pay, limit - current.earnedOn(day)));
            }
            mangos += pay;
            JobProgress updated = grow(player, entry.getKey(), job, current, hit.xp() * times * experience * boost)
                    .earn(pay, day);
            jobs.put(entry.getKey(), updated);
            board.update(entry.getKey(), player.getUUID(), player.getGameProfile().getName(), updated);
        }

        long cents = Math.round(mangos * Money.toCents(1L));
        if (cents <= 0L) {
            return;
        }
        BigInteger paid = Payout.give(player, BigInteger.valueOf(cents), UMAConfig.COMMON.jobsPayTo.get());
        if (paid.signum() > 0 && UMAConfig.COMMON.jobsShowIncome.get()) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.job_income",
                    Money.format(paid)).withStyle(ChatFormatting.GREEN), true);
        }
    }

    private static JobProgress grow(ServerPlayer player, ResourceLocation id, JobDefinition job, JobProgress current,
                                    double gained) {
        int level = current.level();
        double before = current.xp() / Math.max(1.0D, job.xpNeeded(level));
        double xp = current.xp() + gained;
        boolean leveled = false;
        while (level < job.maxLevel() && xp >= job.xpNeeded(level)) {
            xp -= job.xpNeeded(level);
            level++;
            leveled = true;
            announceLevel(player, id, job, level);
            giveRewards(player, job, level);
        }
        if (level >= job.maxLevel()) {
            xp = 0.0D;
        } else if (!leveled) {
            progressAlert(player, id, job, before, xp / Math.max(1.0D, job.xpNeeded(level)));
        }
        return current.with(level, xp);
    }

    private static void progressAlert(ServerPlayer player, ResourceLocation id, JobDefinition job, double before,
                                      double after) {
        for (int percent : UMAConfig.COMMON.jobsProgressAlerts.get()) {
            double mark = percent / 100.0D;
            if (before < mark && after >= mark) {
                player.displayClientMessage(Component.translatable("message.ultimatemodadditions.job_progress",
                        displayName(id, job), percent).withStyle(ChatFormatting.AQUA), true);
            }
        }
    }

    private static void giveRewards(ServerPlayer player, JobDefinition job, int level) {
        for (JobDefinition.Reward reward : job.rewards()) {
            if (!reward.reaches(level)) {
                continue;
            }
            if (reward.money() > 0.0D) {
                Payout.give(player, BigInteger.valueOf(Math.round(reward.money() * Money.toCents(1L))),
                        UMAConfig.COMMON.jobsPayTo.get());
            }
            for (JobDefinition.RewardItem item : reward.items()) {
                ItemStack stack = new ItemStack(item.item(), item.count());
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
            if (UMAConfig.COMMON.jobsRewardCommands.get()) {
                for (String command : reward.commands()) {
                    player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack()
                            .withSuppressedOutput(), command.replace("%player%", player.getGameProfile().getName()));
                }
            }
            player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.job_reward", level)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static void announceLevel(ServerPlayer player, ResourceLocation id, JobDefinition job, int level) {
        String title = job.titleFor(level);
        Component message = title.isEmpty()
                ? Component.translatable("message.ultimatemodadditions.job_level_up", displayName(id, job), level)
                : Component.translatable("message.ultimatemodadditions.job_level_up_title", displayName(id, job),
                level, title);
        player.sendSystemMessage(message.copy().withStyle(ChatFormatting.GOLD));
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 15));
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.translatable("message.ultimatemodadditions.job_level_title", level)
                        .withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(displayName(id, job).copy()
                .withStyle(ChatFormatting.YELLOW)));
    }

    public static boolean setLevel(ServerPlayer player, ResourceLocation id, int level) {
        JobDefinition job = UMAData.jobs().get(id);
        JobProgress current = progress(player).get(id);
        if (job == null || current == null) {
            return false;
        }
        JobProgress updated = current.with(Math.max(1, Math.min(level, job.maxLevel())), 0.0D);
        progress(player).put(id, updated);
        JobsBoard.get(player.server).update(id, player.getUUID(), player.getGameProfile().getName(), updated);
        return true;
    }

    public static boolean addExperience(ServerPlayer player, ResourceLocation id, double xp) {
        JobDefinition job = UMAData.jobs().get(id);
        JobProgress current = progress(player).get(id);
        if (job == null || current == null) {
            return false;
        }
        JobProgress updated = grow(player, id, job, current, xp);
        progress(player).put(id, updated);
        JobsBoard.get(player.server).update(id, player.getUUID(), player.getGameProfile().getName(), updated);
        return true;
    }

    public static boolean employ(ServerPlayer player, ResourceLocation id) {
        Map<ResourceLocation, JobProgress> jobs = progress(player);
        if (jobs.containsKey(id) || !UMAData.jobs().containsKey(id)) {
            return false;
        }
        jobs.put(id, JobProgress.start());
        JobsBoard.get(player.server).update(id, player.getUUID(), player.getGameProfile().getName(),
                JobProgress.start());
        return true;
    }
}
