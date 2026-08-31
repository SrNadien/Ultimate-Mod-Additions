package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.LootBagItem;
import nadiendev.ultimatemodadditions.data.DifficultyProfile;
import nadiendev.ultimatemodadditions.data.DropEntry;
import nadiendev.ultimatemodadditions.data.MobRule;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import nadiendev.ultimatemodadditions.util.LootingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = UMA.MODID)
public final class ColiseumEvents {

    private ColiseumEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (!MobScaler.shouldScale(living)) {
            return;
        }
        DifficultyResolver.Resolved resolved = DifficultyResolver.resolve(living);
        MobScaler.apply(living, resolved.profile(), resolved.profileId());
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player || !(entity.level() instanceof ServerLevel level)) {
            return;
        }
        boolean inColiseum = Coliseum.isColiseum(level);
        boolean customLootAllowed = inColiseum || !UMAConfig.COMMON.coliseumLootOnly.get();

        DifficultyResolver.Resolved resolved = DifficultyResolver.resolve(entity);
        MobRule rule = customLootAllowed ? resolved.rule() : null;
        DifficultyProfile profile = resolved.profile();
        RandomSource random = entity.getRandom();
        BlockPos pos = entity.blockPosition();
        int looting = LootingHelper.level(level, event.getSource());
        boolean playerKill = event.getSource().getEntity() instanceof Player;

        List<ItemStack> extra = new ArrayList<>();

        if (rule != null) {
            boolean clearVanilla = rule.replaceVanillaDrops()
                    || (inColiseum && !UMAConfig.COMMON.keepVanillaDropsInColiseum.get());
            if (clearVanilla) {
                event.getDrops().clear();
            }
            int rolls = 1 + Math.max(0, profile.extraLootRolls());
            for (int roll = 0; roll < rolls; roll++) {
                for (DropEntry drop : rule.drops()) {
                    drop.generate(level, pos, random, looting, extra::add);
                }
            }
            for (MobRule.BagDrop bagDrop : rule.bags()) {
                float chance = bagDrop.chance() + (float) profile.bagChanceBonus();
                if (random.nextFloat() < chance) {
                    ItemStack bag = LootBagItem.create(bagDrop.bag(), bagDrop.count().sample(random));
                    if (!bag.isEmpty()) {
                        extra.add(bag);
                    }
                }
            }
        }

        addCoins(rule, profile, random, playerKill, inColiseum, extra);
        addRandomBag(profile, random, playerKill, inColiseum, extra);

        for (ItemStack stack : extra) {
            event.getDrops().add(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack));
        }
    }

    private static void addCoins(MobRule rule, DifficultyProfile profile, RandomSource random,
                                 boolean playerKill, boolean inColiseum, List<ItemStack> output) {
        if (!UMAConfig.COMMON.coinsEnabled.get()) {
            return;
        }
        if (UMAConfig.COMMON.coinsColiseumOnly.get() && !inColiseum) {
            return;
        }
        if (UMAConfig.COMMON.coinsOnlyFromPlayerKills.get() && !playerKill) {
            return;
        }
        if (rule != null && !rule.coins().isEmpty()) {
            for (MobRule.CoinDrop coin : rule.coins()) {
                if (random.nextFloat() >= coin.chance()) {
                    continue;
                }
                int amount = (int) Math.round(coin.count().sample(random) * profile.coinMultiplier());
                if (amount > 0) {
                    output.add(new ItemStack(UMAItems.coinByTier(coin.tier()), amount));
                }
            }
            return;
        }
        if (random.nextDouble() >= UMAConfig.COMMON.coinDropChance.get()) {
            return;
        }
        int min = UMAConfig.COMMON.coinMin.get();
        int max = Math.max(min, UMAConfig.COMMON.coinMax.get());
        int amount = (int) Math.round((min + (max > min ? random.nextInt(max - min + 1) : 0)) * profile.coinMultiplier());
        if (amount > 0) {
            output.add(new ItemStack(UMAItems.coin(nadiendev.ultimatemodadditions.currency.Denomination.D10), amount));
        }
    }

    private static void addRandomBag(DifficultyProfile profile, RandomSource random, boolean playerKill,
                                     boolean inColiseum, List<ItemStack> output) {
        if (!UMAConfig.COMMON.bagsEnabled.get() || UMAData.lootBags().isEmpty()) {
            return;
        }
        if (UMAConfig.COMMON.bagsColiseumOnly.get() && !inColiseum) {
            return;
        }
        if (UMAConfig.COMMON.coinsOnlyFromPlayerKills.get() && !playerKill) {
            return;
        }
        double chance = UMAConfig.COMMON.bagDropChance.get() + profile.bagChanceBonus();
        if (random.nextDouble() >= chance) {
            return;
        }
        List<ResourceLocation> ids = new ArrayList<>(UMAData.lootBags().keySet());
        ResourceLocation picked = ids.get(random.nextInt(ids.size()));
        ItemStack bag = LootBagItem.create(picked, 1);
        if (!bag.isEmpty()) {
            output.add(bag);
        }
    }

    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player || entity.level().isClientSide) {
            return;
        }
        DifficultyResolver.Resolved resolved = DifficultyResolver.resolve(event.getEntity());
        double multiplier = resolved.profile().experienceMultiplier()
                * (resolved.rule() != null ? resolved.rule().experienceMultiplier() : 1.0D);
        if (Math.abs(multiplier - 1.0D) > 1.0E-4) {
            event.setDroppedExperience((int) Math.round(event.getDroppedExperience() * multiplier));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        if (!Coliseum.isColiseum(player.level()) || player.tickCount % 10 != 0) {
            return;
        }
        if (player.getY() >= ColiseumArena.floorY() - 16) {
            return;
        }
        ColiseumTravel.sendToArena(player, player.serverLevel());
        player.fallDistance = 0.0F;
        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
    }
}
