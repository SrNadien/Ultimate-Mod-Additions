package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record LootBagAddition(Optional<ResourceLocation> bag,
                              List<ResourceLocation> bags,
                              List<String> tiers,
                              List<DropEntry> entries,
                              List<DropEntry> guaranteed,
                              boolean replaceEntries) {

    public static final Codec<LootBagAddition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("bag").forGetter(LootBagAddition::bag),
            ResourceLocation.CODEC.listOf().optionalFieldOf("bags", List.of()).forGetter(LootBagAddition::bags),
            Codec.STRING.listOf().optionalFieldOf("tiers", List.of()).forGetter(LootBagAddition::tiers),
            DropEntry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(LootBagAddition::entries),
            DropEntry.CODEC.listOf().optionalFieldOf("guaranteed", List.of()).forGetter(LootBagAddition::guaranteed),
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(LootBagAddition::replaceEntries)
    ).apply(instance, LootBagAddition::new));

    public boolean targets(ResourceLocation bagId, String tier) {
        if (bag.isPresent() && bag.get().equals(bagId)) {
            return true;
        }
        if (bags.contains(bagId)) {
            return true;
        }
        for (String candidate : tiers) {
            if (candidate.equalsIgnoreCase(tier) || candidate.equalsIgnoreCase("all")) {
                return true;
            }
        }
        return false;
    }

    public LootBagDefinition applyTo(LootBagDefinition definition) {
        List<DropEntry> mergedEntries = replaceEntries ? new ArrayList<>() : new ArrayList<>(definition.entries());
        mergedEntries.addAll(entries);
        List<DropEntry> mergedGuaranteed = replaceEntries ? new ArrayList<>() : new ArrayList<>(definition.guaranteed());
        mergedGuaranteed.addAll(guaranteed);
        return new LootBagDefinition(definition.name(), definition.tier(), definition.rolls(),
                List.copyOf(mergedGuaranteed), List.copyOf(mergedEntries), definition.unique(), definition.color(),
                definition.openSound(), definition.announce(), definition.sortOrder());
    }
}
