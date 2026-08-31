package nadiendev.ultimatemodadditions.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class LootingHelper {

    private LootingHelper() {
    }

    public static int level(ServerLevel level, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity killer)) {
            return 0;
        }
        try {
            Holder<Enchantment> looting = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.LOOTING);
            return EnchantmentHelper.getEnchantmentLevel(looting, killer);
        } catch (Throwable ignored) {
            return 0;
        }
    }
}
