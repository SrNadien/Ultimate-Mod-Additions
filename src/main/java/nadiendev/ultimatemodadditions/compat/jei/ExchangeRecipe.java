package nadiendev.ultimatemodadditions.compat.jei;

import nadiendev.ultimatemodadditions.data.TradeOffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ExchangeRecipe(ResourceLocation id, List<ItemStack> cost, ItemStack result, TradeOffer offer) {

    public static ExchangeRecipe of(ResourceLocation id, TradeOffer offer) {
        List<ItemStack> cost = new ArrayList<>();
        for (TradeOffer.CostEntry entry : offer.cost()) {
            cost.add(entry.asStack());
        }
        return new ExchangeRecipe(id, cost, offer.result().asStack(), offer);
    }
}
