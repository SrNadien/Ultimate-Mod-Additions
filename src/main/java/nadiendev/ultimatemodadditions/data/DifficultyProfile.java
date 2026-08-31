package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record DifficultyProfile(Optional<String> name,
                                double healthMultiplier,
                                double damageMultiplier,
                                double speedMultiplier,
                                double armorBonus,
                                double armorToughnessBonus,
                                double knockbackResistance,
                                double followRangeBonus,
                                double experienceMultiplier,
                                double coinMultiplier,
                                double bagChanceBonus,
                                int extraLootRolls,
                                List<EffectSpec> effects,
                                Map<String, EquipmentSpec> equipment,
                                boolean glowing) {

    public static final DifficultyProfile IDENTITY = new DifficultyProfile(Optional.empty(),
            1.0D, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.0D, 0, List.of(), Map.of(), false);

    public static final Codec<DifficultyProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name").forGetter(DifficultyProfile::name),
            Codec.DOUBLE.optionalFieldOf("health_multiplier", 1.0D).forGetter(DifficultyProfile::healthMultiplier),
            Codec.DOUBLE.optionalFieldOf("damage_multiplier", 1.0D).forGetter(DifficultyProfile::damageMultiplier),
            Codec.DOUBLE.optionalFieldOf("speed_multiplier", 1.0D).forGetter(DifficultyProfile::speedMultiplier),
            Codec.DOUBLE.optionalFieldOf("armor_bonus", 0.0D).forGetter(DifficultyProfile::armorBonus),
            Codec.DOUBLE.optionalFieldOf("armor_toughness_bonus", 0.0D).forGetter(DifficultyProfile::armorToughnessBonus),
            Codec.DOUBLE.optionalFieldOf("knockback_resistance", 0.0D).forGetter(DifficultyProfile::knockbackResistance),
            Codec.DOUBLE.optionalFieldOf("follow_range_bonus", 0.0D).forGetter(DifficultyProfile::followRangeBonus),
            Codec.DOUBLE.optionalFieldOf("experience_multiplier", 1.0D).forGetter(DifficultyProfile::experienceMultiplier),
            Codec.DOUBLE.optionalFieldOf("coin_multiplier", 1.0D).forGetter(DifficultyProfile::coinMultiplier),
            Codec.DOUBLE.optionalFieldOf("bag_chance_bonus", 0.0D).forGetter(DifficultyProfile::bagChanceBonus),
            Codec.INT.optionalFieldOf("extra_loot_rolls", 0).forGetter(DifficultyProfile::extraLootRolls),
            EffectSpec.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(DifficultyProfile::effects),
            Codec.unboundedMap(Codec.STRING, EquipmentSpec.CODEC).optionalFieldOf("equipment", Map.of()).forGetter(DifficultyProfile::equipment),
            Codec.BOOL.optionalFieldOf("glowing", false).forGetter(DifficultyProfile::glowing)
    ).apply(instance, DifficultyProfile::new));

    public void applyEffects(LivingEntity entity, RandomSource random) {
        for (EffectSpec spec : effects) {
            if (spec.chance() < 1.0F && random.nextFloat() >= spec.chance()) {
                continue;
            }
            entity.addEffect(new MobEffectInstance(spec.effect(), spec.duration(), spec.amplifier(), false, false, true));
        }
        if (glowing) {
            entity.setGlowingTag(true);
        }
    }

    public void applyEquipment(LivingEntity entity, RandomSource random) {
        if (equipment.isEmpty() || !(entity instanceof Mob mob)) {
            return;
        }
        for (Map.Entry<String, EquipmentSpec> entry : equipment.entrySet()) {
            EquipmentSlot slot = slotByName(entry.getKey());
            if (slot == null) {
                continue;
            }
            EquipmentSpec spec = entry.getValue();
            if (spec.chance() < 1.0F && random.nextFloat() >= spec.chance()) {
                continue;
            }
            ItemStack stack = new ItemStack(spec.item(), Math.max(1, spec.count()));
            spec.components().ifPresent(stack::applyComponents);
            mob.setItemSlot(slot, stack);
            mob.setDropChance(slot, spec.dropChance());
        }
    }

    private static EquipmentSlot slotByName(String name) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getName().equalsIgnoreCase(name) || slot.name().equalsIgnoreCase(name)) {
                return slot;
            }
        }
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "hand", "main_hand", "weapon" -> EquipmentSlot.MAINHAND;
            case "off_hand", "shield" -> EquipmentSlot.OFFHAND;
            case "helmet", "hat" -> EquipmentSlot.HEAD;
            case "chestplate" -> EquipmentSlot.CHEST;
            case "leggings", "pants" -> EquipmentSlot.LEGS;
            case "boots" -> EquipmentSlot.FEET;
            default -> null;
        };
    }

    public record EffectSpec(Holder<MobEffect> effect, int amplifier, int duration, float chance) {
        public static final Codec<EffectSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(EffectSpec::effect),
                Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectSpec::amplifier),
                Codec.INT.optionalFieldOf("duration", -1).forGetter(EffectSpec::duration),
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(EffectSpec::chance)
        ).apply(instance, EffectSpec::new));
    }

    public record EquipmentSpec(Item item, int count, float chance, float dropChance,
                                Optional<DataComponentPatch> components) {
        public static final Codec<EquipmentSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(EquipmentSpec::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(EquipmentSpec::count),
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(EquipmentSpec::chance),
                Codec.FLOAT.optionalFieldOf("drop_chance", 0.085F).forGetter(EquipmentSpec::dropChance),
                DataComponentPatch.CODEC.optionalFieldOf("components").forGetter(EquipmentSpec::components)
        ).apply(instance, EquipmentSpec::new));
    }
}
