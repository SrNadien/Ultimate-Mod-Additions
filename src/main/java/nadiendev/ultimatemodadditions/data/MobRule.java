package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatemodadditions.util.EntityMatcher;
import nadiendev.ultimatemodadditions.util.IntRange;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record MobRule(int priority,
                      EntityMatcher match,
                      List<ResourceLocation> dimensions,
                      Optional<ResourceLocation> difficulty,
                      Optional<DifficultyProfile> overrides,
                      boolean replaceVanillaDrops,
                      List<DropEntry> drops,
                      List<CoinDrop> coins,
                      List<BagDrop> bags,
                      double experienceMultiplier,
                      boolean stopMatching) {

    public static final Codec<MobRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("priority", 0).forGetter(MobRule::priority),
            EntityMatcher.CODEC.optionalFieldOf("match", EntityMatcher.ANY).forGetter(MobRule::match),
            ResourceLocation.CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(MobRule::dimensions),
            ResourceLocation.CODEC.optionalFieldOf("difficulty").forGetter(MobRule::difficulty),
            DifficultyProfile.CODEC.optionalFieldOf("inline_difficulty").forGetter(MobRule::overrides),
            Codec.BOOL.optionalFieldOf("replace_vanilla_drops", false).forGetter(MobRule::replaceVanillaDrops),
            DropEntry.CODEC.listOf().optionalFieldOf("drops", List.of()).forGetter(MobRule::drops),
            CoinDrop.CODEC.listOf().optionalFieldOf("coins", List.of()).forGetter(MobRule::coins),
            BagDrop.CODEC.listOf().optionalFieldOf("bags", List.of()).forGetter(MobRule::bags),
            Codec.DOUBLE.optionalFieldOf("experience_multiplier", 1.0D).forGetter(MobRule::experienceMultiplier),
            Codec.BOOL.optionalFieldOf("stop_matching", false).forGetter(MobRule::stopMatching)
    ).apply(instance, MobRule::new));

    public boolean appliesToDimension(ResourceLocation dimension) {
        return dimensions.isEmpty() || dimensions.contains(dimension);
    }

    public record CoinDrop(String tier, IntRange count, float chance) {
        public static final Codec<CoinDrop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("tier", "bronze").forGetter(CoinDrop::tier),
                IntRange.CODEC.optionalFieldOf("count", IntRange.exactly(1)).forGetter(CoinDrop::count),
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(CoinDrop::chance)
        ).apply(instance, CoinDrop::new));
    }

    public record BagDrop(ResourceLocation bag, IntRange count, float chance) {
        public static final Codec<BagDrop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("bag").forGetter(BagDrop::bag),
                IntRange.CODEC.optionalFieldOf("count", IntRange.exactly(1)).forGetter(BagDrop::count),
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(BagDrop::chance)
        ).apply(instance, BagDrop::new));
    }
}
