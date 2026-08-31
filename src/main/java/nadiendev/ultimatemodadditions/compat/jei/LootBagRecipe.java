package nadiendev.ultimatemodadditions.compat.jei;

import nadiendev.ultimatemodadditions.currency.LootBagItem;
import nadiendev.ultimatemodadditions.data.DropEntry;
import nadiendev.ultimatemodadditions.data.LootBagDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record LootBagRecipe(ResourceLocation id, ItemStack bag, List<ItemStack> guaranteed, List<ItemStack> pool,
                            LootBagDefinition definition) {

    public static LootBagRecipe of(ResourceLocation id, LootBagDefinition definition) {
        return new LootBagRecipe(id, LootBagItem.create(id, 1),
                stacks(definition.guaranteed()), stacks(definition.entries()), definition);
    }

    private static List<ItemStack> stacks(List<DropEntry> entries) {
        List<ItemStack> stacks = new ArrayList<>();
        for (DropEntry entry : entries) {
            entry.item().ifPresent(item -> stacks.add(new ItemStack(item, Math.max(1, entry.count().max()))));
            entry.bag().ifPresent(bagId -> {
                ItemStack nested = LootBagItem.create(bagId, 1);
                if (!nested.isEmpty()) {
                    stacks.add(nested);
                }
            });
        }
        return stacks;
    }
}
