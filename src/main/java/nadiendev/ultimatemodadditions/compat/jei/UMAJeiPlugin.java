package nadiendev.ultimatemodadditions.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.network.SyncedData;
import nadiendev.ultimatemodadditions.registry.UMABlocks;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class UMAJeiPlugin implements IModPlugin {

    private static IJeiRuntime runtime;

    @Override
    public ResourceLocation getPluginUid() {
        return UMA.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new LootBagCategory(registration.getJeiHelpers().getGuiHelper(),
                        new ItemStack(UMAItems.bag(nadiendev.ultimatemodadditions.currency.BagTier.LEGENDARY))),
                new ExchangeCategory(registration.getJeiHelpers().getGuiHelper(),
                        new ItemStack(UMABlocks.EXCHANGE_STAND.get())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(UMARecipeTypes.LOOT_BAG, bagRecipes());
        registration.addRecipes(UMARecipeTypes.EXCHANGE, exchangeRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(UMABlocks.EXCHANGE_STAND.get()), UMARecipeTypes.EXCHANGE);
        for (nadiendev.ultimatemodadditions.currency.BagTier tier : nadiendev.ultimatemodadditions.currency.BagTier.values()) {
            registration.addRecipeCatalyst(new ItemStack(UMAItems.bag(tier)), UMARecipeTypes.LOOT_BAG);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
    }

    public static void refresh() {
        if (runtime == null) {
            return;
        }
        try {
            runtime.getRecipeManager().addRecipes(UMARecipeTypes.LOOT_BAG, bagRecipes());
            runtime.getRecipeManager().addRecipes(UMARecipeTypes.EXCHANGE, exchangeRecipes());
        } catch (Throwable throwable) {
            UMA.LOGGER.warn("Could not refresh the JEI recipe list", throwable);
        }
    }

    private static List<LootBagRecipe> bagRecipes() {
        List<LootBagRecipe> recipes = new ArrayList<>();
        SyncedData.bags().forEach((id, definition) -> recipes.add(LootBagRecipe.of(id, definition)));
        return recipes;
    }

    private static List<ExchangeRecipe> exchangeRecipes() {
        List<ExchangeRecipe> recipes = new ArrayList<>();
        SyncedData.trades().forEach((id, offer) -> {
            if (offer.enabled() && nadiendev.ultimatemodadditions.currency.ExchangeLogic.purchasable(offer)) {
                recipes.add(ExchangeRecipe.of(id, offer));
            }
        });
        return recipes;
    }
}
