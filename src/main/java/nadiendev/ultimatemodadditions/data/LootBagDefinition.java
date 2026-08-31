package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatemodadditions.util.IntRange;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record LootBagDefinition(Optional<String> name,
                                String tier,
                                IntRange rolls,
                                List<DropEntry> guaranteed,
                                List<DropEntry> entries,
                                boolean unique,
                                int color,
                                Optional<ResourceLocation> openSound,
                                boolean announce,
                                int sortOrder) {

    public static final Codec<LootBagDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name").forGetter(LootBagDefinition::name),
            Codec.STRING.optionalFieldOf("tier", "common").forGetter(LootBagDefinition::tier),
            IntRange.CODEC.optionalFieldOf("rolls", IntRange.exactly(1)).forGetter(LootBagDefinition::rolls),
            DropEntry.CODEC.listOf().optionalFieldOf("guaranteed", List.of()).forGetter(LootBagDefinition::guaranteed),
            DropEntry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(LootBagDefinition::entries),
            Codec.BOOL.optionalFieldOf("unique_rolls", false).forGetter(LootBagDefinition::unique),
            Codec.INT.optionalFieldOf("color", 0xFFFFFF).forGetter(LootBagDefinition::color),
            ResourceLocation.CODEC.optionalFieldOf("open_sound").forGetter(LootBagDefinition::openSound),
            Codec.BOOL.optionalFieldOf("announce", false).forGetter(LootBagDefinition::announce),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(LootBagDefinition::sortOrder)
    ).apply(instance, LootBagDefinition::new));

    public int totalWeight() {
        int total = 0;
        for (DropEntry entry : entries) {
            total += Math.max(0, entry.weight());
        }
        return total;
    }
}
