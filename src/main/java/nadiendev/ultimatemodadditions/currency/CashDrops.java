package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.data.DifficultyProfile;
import nadiendev.ultimatemodadditions.data.MobRule;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public final class CashDrops {

    private CashDrops() {
    }

    public static void addMobDrops(LivingEntity entity, MobRule rule, DifficultyProfile profile, boolean playerKill,
                                   boolean inColiseum, List<ItemStack> output) {
        if (!UMAConfig.COMMON.coinsEnabled.get()) {
            return;
        }
        if (UMAConfig.COMMON.coinsColiseumOnly.get() && !inColiseum) {
            return;
        }
        if (UMAConfig.COMMON.coinsOnlyFromPlayerKills.get() && !playerKill) {
            return;
        }
        String dimension = entity.level().dimension().location().toString();
        if (UMAConfig.COMMON.cashDimensionBlacklist.get().contains(dimension)) {
            return;
        }
        RandomSource random = entity.getRandom();
        if (rule != null && !rule.coins().isEmpty()) {
            for (MobRule.CoinDrop coin : rule.coins()) {
                if (random.nextFloat() >= coin.chance()) {
                    continue;
                }
                int amount = scaled(coin.count().sample(random), profile);
                if (amount > 0) {
                    output.add(new ItemStack(UMAItems.cashByKey(coin.tier()), amount));
                }
            }
            return;
        }
        double multiplier = UMAConfig.COMMON.cashChanceMultiplier.get();
        if (entity.getType().is(Tags.EntityTypes.BOSSES)) {
            multiplier *= UMAConfig.COMMON.cashBossChanceMultiplier.get();
        }
        int maxCount = UMAConfig.COMMON.cashMaxCount.get();
        for (Denomination denomination : Denomination.values()) {
            double chance = Math.min(1.0D, UMAConfig.COMMON.cashDropChance(denomination) * multiplier);
            if (random.nextDouble() >= chance) {
                continue;
            }
            int amount = scaled(1 + random.nextInt(maxCount), profile);
            if (amount > 0) {
                output.add(new ItemStack(UMAItems.cash(denomination), amount));
            }
        }
    }

    public static ItemStack randomChestCash(RandomSource random) {
        double total = 0.0D;
        for (Denomination denomination : Denomination.values()) {
            total += UMAConfig.COMMON.cashDropChance(denomination);
        }
        if (total <= 0.0D) {
            return ItemStack.EMPTY;
        }
        double pick = random.nextDouble() * total;
        Denomination chosen = Denomination.C5;
        for (Denomination denomination : Denomination.values()) {
            pick -= UMAConfig.COMMON.cashDropChance(denomination);
            if (pick <= 0.0D) {
                chosen = denomination;
                break;
            }
        }
        int min = UMAConfig.COMMON.cashChestMinCount.get();
        int max = Math.max(min, UMAConfig.COMMON.cashChestMaxCount.get());
        int count = min + (max > min ? random.nextInt(max - min + 1) : 0);
        return new ItemStack(UMAItems.cash(chosen), count);
    }

    private static int scaled(int amount, DifficultyProfile profile) {
        double multiplier = profile.coinMultiplier();
        if (multiplier <= 0.0D) {
            return 0;
        }
        return Math.max(1, (int) Math.round(amount * multiplier));
    }
}
