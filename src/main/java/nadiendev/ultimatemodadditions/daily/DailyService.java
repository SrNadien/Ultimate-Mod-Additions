package nadiendev.ultimatemodadditions.daily;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Payout;
import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.data.JobDefinition;
import nadiendev.ultimatemodadditions.data.ShopCategory;
import nadiendev.ultimatemodadditions.data.ShopItem;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload;
import nadiendev.ultimatemodadditions.registry.UMAAttachments;
import nadiendev.ultimatemodadditions.shop.ModShops;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public final class DailyService {

    private DailyService() {
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().minusHours(UMAConfig.COMMON.dailyResetHour.get());
    }

    private static long dayId(LocalDateTime now) {
        return now.toLocalDate().toEpochDay();
    }

    private static long weekId(LocalDateTime now) {
        return (dayId(now) + 3L) / 7L;
    }

    private static long monthId(LocalDateTime now) {
        return now.getYear() * 12L + now.getMonthValue();
    }

    private static long secondsUntil(LocalDateTime now, LocalDateTime next) {
        return Math.max(1L, Duration.between(now, next).getSeconds());
    }

    public static DailyState state(ServerPlayer player) {
        return player.getData(UMAAttachments.DAILY);
    }

    public static List<DailyReward.Entry> entries(MinecraftServer server, DailyReward.Period period) {
        List<DailyReward.Entry> edited = DailyEdits.get(server).custom(period);
        if (edited != null) {
            return edited;
        }
        List<DailyReward.Entry> entries = new ArrayList<>();
        UMAData.daily().values().stream().filter(reward -> reward.period() == period)
                .forEach(reward -> entries.addAll(reward.entries()));
        entries.addAll(ModShops.rewardEntries(server, period, periodId(period, now())));
        return entries;
    }

    private static long periodId(DailyReward.Period period, LocalDateTime now) {
        return switch (period) {
            case DAILY -> dayId(now);
            case WEEKLY -> weekId(now);
            case MONTHLY -> monthId(now);
        };
    }

    private static DailyReward.Entry rotating(MinecraftServer server, DailyReward.Period period, long id) {
        if (DailyEdits.get(server).custom(period) != null) {
            return null;
        }
        List<DailyReward.Entry> mods = ModShops.rewardEntries(server, period, id);
        return mods.isEmpty() ? null : mods.get((int) Math.floorMod(id, (long) mods.size()));
    }

    private static boolean available(DailyState state, DailyReward.Period period, LocalDateTime now) {
        return switch (period) {
            case DAILY -> state.lastDay() != dayId(now);
            case WEEKLY -> state.lastWeek() != weekId(now);
            case MONTHLY -> state.lastMonth() != monthId(now);
        };
    }

    private static long secondsLeft(DailyReward.Period period, LocalDateTime now) {
        return switch (period) {
            case DAILY -> secondsUntil(now, now.toLocalDate().plusDays(1).atStartOfDay());
            case WEEKLY -> secondsUntil(now, now.toLocalDate().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                    .atStartOfDay());
            case MONTHLY -> secondsUntil(now, now.toLocalDate().with(TemporalAdjusters.firstDayOfNextMonth())
                    .atStartOfDay());
        };
    }

    public static boolean anythingToClaim(ServerPlayer player) {
        if (!UMAConfig.COMMON.dailyEnabled.get()) {
            return false;
        }
        LocalDateTime now = now();
        DailyState state = state(player);
        for (DailyReward.Period period : DailyReward.Period.values()) {
            if (!entries(player.server, period).isEmpty() && available(state, period, now)) {
                return true;
            }
        }
        return false;
    }

    public static void sync(ServerPlayer player, boolean open) {
        LocalDateTime now = now();
        DailyState state = state(player);
        List<DailySyncPayload.PeriodView> views = new ArrayList<>();
        for (DailyReward.Period period : DailyReward.Period.values()) {
            List<DailyReward.Entry> entries = entries(player.server, period);
            List<DailySyncPayload.EntryView> previews = new ArrayList<>();
            for (DailyReward.Entry entry : entries) {
                List<ItemStack> items = new ArrayList<>();
                for (JobDefinition.RewardItem item : entry.items()) {
                    items.add(new ItemStack(item.item(), item.count()));
                }
                previews.add(new DailySyncPayload.EntryView(entry.money(), items));
            }
            boolean ready = !entries.isEmpty() && available(state, period, now);
            views.add(new DailySyncPayload.PeriodView(period, ready, ready ? 0L : secondsLeft(period, now), previews));
        }
        int streak = state.lastDay() == dayId(now) || state.lastDay() == dayId(now) - 1L ? state.streak() : 0;
        PacketDistributor.sendToPlayer(player, new DailySyncPayload(open, streak, views));
    }

    public static void claim(ServerPlayer player, DailyReward.Period period) {
        if (!UMAConfig.COMMON.dailyEnabled.get()) {
            return;
        }
        List<DailyReward.Entry> entries = entries(player.server, period);
        LocalDateTime now = now();
        DailyState state = state(player);
        if (entries.isEmpty() || !available(state, period, now)) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.daily_not_ready")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        DailyReward.Entry entry;
        DailyReward.Entry extra = null;
        DailyState updated;
        switch (period) {
            case DAILY -> {
                int streak = state.lastDay() == dayId(now) - 1L ? state.streak() + 1 : 1;
                entry = entries.get((streak - 1) % entries.size());
                updated = new DailyState(dayId(now), streak, state.lastWeek(), state.lastMonth());
            }
            case WEEKLY -> {
                entry = entries.get(0);
                extra = rotating(player.server, period, weekId(now));
                updated = new DailyState(state.lastDay(), state.streak(), weekId(now), state.lastMonth());
            }
            default -> {
                entry = entries.get(0);
                extra = rotating(player.server, period, monthId(now));
                updated = new DailyState(state.lastDay(), state.streak(), state.lastWeek(), monthId(now));
            }
        }
        player.setData(UMAAttachments.DAILY, updated);
        grant(player, entry);
        if (extra != null) {
            grant(player, extra);
        }
        giveModBonus(player, period);
        player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.daily_claimed_"
                + period.getSerializedName()).withStyle(ChatFormatting.GOLD));
        sync(player, false);
    }

    private static void giveModBonus(ServerPlayer player, DailyReward.Period period) {
        if (!UMAConfig.COMMON.dailyModBonus.get()) {
            return;
        }
        double budget = switch (period) {
            case DAILY -> UMAConfig.COMMON.dailyModBonusDaily.get();
            case WEEKLY -> UMAConfig.COMMON.dailyModBonusWeekly.get();
            case MONTHLY -> UMAConfig.COMMON.dailyModBonusMonthly.get();
        };
        List<ShopCategory> zones = new ArrayList<>(ModShops.get(player.server).values());
        double chance = UMAConfig.COMMON.dailyModBonusChance.get();
        for (ShopCategory zone : zones) {
            if (player.getRandom().nextDouble() * 100.0D >= chance) {
                continue;
            }
            List<ShopItem> options = zone.items().stream()
                    .filter(item -> item.buyable() && item.buy() <= budget).toList();
            if (options.isEmpty()) {
                continue;
            }
            ItemStack stack = options.get(player.getRandom().nextInt(options.size())).stack().copyWithCount(1);
            player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.daily_mod_bonus",
                    stack.getHoverName(), Component.translatable(zone.name())).withStyle(ChatFormatting.AQUA));
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    private static void grant(ServerPlayer player, DailyReward.Entry entry) {
        if (entry.money() > 0.0D) {
            BigInteger paid = Payout.give(player, BigInteger.valueOf(Math.round(entry.money() * Money.toCents(1L))),
                    UMAConfig.COMMON.dailyPayTo.get());
            player.sendSystemMessage(Component.translatable("message.ultimatemodadditions.daily_money",
                    Money.format(paid)).withStyle(ChatFormatting.GREEN));
        }
        for (JobDefinition.RewardItem item : entry.items()) {
            ItemStack stack = new ItemStack(item.item(), item.count());
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        if (UMAConfig.COMMON.dailyCommands.get()) {
            for (String command : entry.commands()) {
                player.server.getCommands().performPrefixedCommand(player.server.createCommandSourceStack()
                        .withSuppressedOutput(), command.replace("%player%", player.getGameProfile().getName()));
            }
        }
    }

    public static void resetState(ServerPlayer player) {
        player.setData(UMAAttachments.DAILY, DailyState.fresh());
        sync(player, false);
    }
}
