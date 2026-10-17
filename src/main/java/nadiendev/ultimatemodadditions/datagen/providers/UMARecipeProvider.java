package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.registry.UMABlocks;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class UMARecipeProvider extends RecipeProvider {

    public UMARecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, UMAItems.COLISEUM_KEY.get())
                .pattern(" N ")
                .pattern(" P ")
                .pattern(" G ")
                .define('N', Items.GOLD_NUGGET)
                .define('P', Items.ENDER_PEARL)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, UMABlocks.EXCHANGE_STAND.get())
                .pattern("GGG")
                .pattern("PCP")
                .pattern("PPP")
                .define('G', Items.GOLD_INGOT)
                .define('P', ItemTags.PLANKS)
                .define('C', Items.CHEST)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, UMABlocks.ATM.get())
                .pattern("III")
                .pattern("IGI")
                .pattern("IRI")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, UMAItems.WALLET.get())
                .pattern("LLL")
                .pattern("LGL")
                .define('L', Items.LEATHER)
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .save(output);
    }
}
