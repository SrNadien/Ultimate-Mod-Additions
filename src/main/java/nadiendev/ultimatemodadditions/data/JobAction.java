package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;

public record JobAction(Type type, String target, boolean fullyGrown, double pay, double xp) {

    public enum Type implements StringRepresentable {
        BREAK_BLOCK,
        PLACE_BLOCK,
        KILL_ENTITY,
        FISH,
        CRAFT,
        SMELT,
        BREW,
        ENCHANT,
        BREED,
        TAME,
        EXPLORE;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final Codec<JobAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(JobAction::type),
            Codec.STRING.optionalFieldOf("target", "").forGetter(JobAction::target),
            Codec.BOOL.optionalFieldOf("fully_grown", false).forGetter(JobAction::fullyGrown),
            Codec.DOUBLE.optionalFieldOf("pay", 0.0D).forGetter(JobAction::pay),
            Codec.DOUBLE.optionalFieldOf("xp", 0.0D).forGetter(JobAction::xp)
    ).apply(instance, JobAction::new));

    public boolean matchesBlock(BlockState state) {
        if (target.isEmpty()) {
            return true;
        }
        if (target.startsWith("#")) {
            ResourceLocation tag = ResourceLocation.tryParse(target.substring(1));
            return tag != null && state.is(TagKey.create(Registries.BLOCK, tag));
        }
        return target.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
    }

    public boolean matchesEntity(EntityType<?> entityType) {
        if (target.isEmpty()) {
            return true;
        }
        if (target.startsWith("#")) {
            ResourceLocation tag = ResourceLocation.tryParse(target.substring(1));
            return tag != null && entityType.is(TagKey.create(Registries.ENTITY_TYPE, tag));
        }
        return target.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    public boolean matchesItem(ItemStack stack) {
        if (target.isEmpty()) {
            return true;
        }
        if (target.startsWith("#")) {
            ResourceLocation tag = ResourceLocation.tryParse(target.substring(1));
            return tag != null && stack.is(TagKey.create(Registries.ITEM, tag));
        }
        return target.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
}
