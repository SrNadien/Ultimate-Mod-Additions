package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public final class UMAWorldgen {

    public static final ResourceKey<DimensionType> COLISEUM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, UMA.id("coliseum"));

    public static final ResourceKey<LevelStem> COLISEUM_STEM =
            ResourceKey.create(Registries.LEVEL_STEM, UMA.id("coliseum"));

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, UMAWorldgen::dimensionTypes)
            .add(Registries.LEVEL_STEM, UMAWorldgen::levelStems);

    private UMAWorldgen() {
    }

    private static void dimensionTypes(BootstrapContext<DimensionType> context) {
        context.register(COLISEUM_TYPE, new DimensionType(
                OptionalLong.empty(),
                true,
                false,
                false,
                false,
                1.0D,
                false,
                false,
                0,
                256,
                256,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                0.1F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    private static void levelStems(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> types = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                Optional.of(HolderSet.<StructureSet>direct()),
                biomes.getOrThrow(Biomes.DESERT),
                List.of());
        ChunkGenerator generator = new FlatLevelSource(settings);
        context.register(COLISEUM_STEM, new LevelStem(types.getOrThrow(COLISEUM_TYPE), generator));
    }
}
