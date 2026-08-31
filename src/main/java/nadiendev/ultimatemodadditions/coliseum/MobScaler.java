package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.data.DifficultyProfile;
import nadiendev.ultimatemodadditions.registry.UMAAttachments;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class MobScaler {

    private static final ResourceLocation HEALTH_ID = UMA.id("coliseum/health");
    private static final ResourceLocation DAMAGE_ID = UMA.id("coliseum/damage");
    private static final ResourceLocation SPEED_ID = UMA.id("coliseum/speed");
    private static final ResourceLocation ARMOR_ID = UMA.id("coliseum/armor");
    private static final ResourceLocation TOUGHNESS_ID = UMA.id("coliseum/armor_toughness");
    private static final ResourceLocation KNOCKBACK_ID = UMA.id("coliseum/knockback_resistance");
    private static final ResourceLocation FOLLOW_ID = UMA.id("coliseum/follow_range");

    private MobScaler() {
    }

    public static boolean shouldScale(LivingEntity entity) {
        if (entity instanceof Player || entity.level().isClientSide) {
            return false;
        }
        if (!UMAConfig.COMMON.mobScalingEnabled.get()) {
            return false;
        }
        if (UMAConfig.COMMON.mobScalingColiseumOnly.get() && !Coliseum.isColiseum(entity.level())) {
            return false;
        }
        return true;
    }

    public static void apply(LivingEntity entity, DifficultyProfile profile, ResourceLocation profileId) {
        boolean firstTime = !entity.getData(UMAAttachments.SCALED.get());
        double crowd = crowdFactor(entity);
        double health = profile.healthMultiplier() * UMAConfig.COMMON.globalHealthMultiplier.get() * crowd;
        double damage = profile.damageMultiplier() * UMAConfig.COMMON.globalDamageMultiplier.get() * crowd;

        multiply(entity, Attributes.MAX_HEALTH, HEALTH_ID, health);
        multiply(entity, Attributes.ATTACK_DAMAGE, DAMAGE_ID, damage);
        multiply(entity, Attributes.MOVEMENT_SPEED, SPEED_ID, profile.speedMultiplier());
        add(entity, Attributes.ARMOR, ARMOR_ID, profile.armorBonus());
        add(entity, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, profile.armorToughnessBonus());
        add(entity, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_ID, profile.knockbackResistance());
        add(entity, Attributes.FOLLOW_RANGE, FOLLOW_ID, profile.followRangeBonus());

        int cap = UMAConfig.COMMON.maxScaledHealth.get();
        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (cap > 0 && maxHealth != null && maxHealth.getValue() > cap) {
            maxHealth.removeModifier(HEALTH_ID);
            double base = maxHealth.getBaseValue();
            if (base > 0 && cap > base) {
                maxHealth.addTransientModifier(new AttributeModifier(HEALTH_ID, cap / base - 1.0D,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }

        if (firstTime) {
            profile.applyEquipment(entity, entity.getRandom());
            profile.applyEffects(entity, entity.getRandom());
            entity.setHealth(entity.getMaxHealth());
            entity.setData(UMAAttachments.SCALED.get(), Boolean.TRUE);
            entity.setData(UMAAttachments.APPLIED_DIFFICULTY.get(), profileId.toString());
        }
    }

    private static double crowdFactor(LivingEntity entity) {
        if (!UMAConfig.COMMON.scaleWithPlayerCount.get() || !Coliseum.isColiseum(entity.level())) {
            return 1.0D;
        }
        int players = entity.level().players().size();
        if (players <= 1) {
            return 1.0D;
        }
        return 1.0D + UMAConfig.COMMON.playerCountScaling.get() * (players - 1);
    }

    private static void multiply(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double multiplier) {
        if (Math.abs(multiplier - 1.0D) < 1.0E-4) {
            return;
        }
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, multiplier - 1.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void add(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount) {
        if (Math.abs(amount) < 1.0E-4) {
            return;
        }
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
    }
}
