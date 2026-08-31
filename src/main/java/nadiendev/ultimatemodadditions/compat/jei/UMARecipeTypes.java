package nadiendev.ultimatemodadditions.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import nadiendev.ultimatemodadditions.UMA;

public final class UMARecipeTypes {

    public static final RecipeType<LootBagRecipe> LOOT_BAG =
            RecipeType.create(UMA.MODID, "loot_bag", LootBagRecipe.class);

    public static final RecipeType<ExchangeRecipe> EXCHANGE =
            RecipeType.create(UMA.MODID, "exchange", ExchangeRecipe.class);

    private UMARecipeTypes() {
    }
}
