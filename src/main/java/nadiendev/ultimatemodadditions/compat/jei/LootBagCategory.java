package nadiendev.ultimatemodadditions.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class LootBagCategory implements IRecipeCategory<LootBagRecipe> {

    private static final int COLUMNS = 8;
    private static final int WIDTH = 162;
    private static final int HEIGHT = 96;

    private final IDrawable icon;

    public LootBagCategory(IGuiHelper helper, ItemStack iconStack) {
        this.icon = helper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<LootBagRecipe> getRecipeType() {
        return UMARecipeTypes.LOOT_BAG;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ultimatemodadditions.loot_bag");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LootBagRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addItemStack(recipe.bag());

        int x = 24;
        int y = 1;
        for (ItemStack stack : recipe.guaranteed()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(stack);
            x += 18;
            if (x > 1 + COLUMNS * 18) {
                x = 24;
                y += 18;
            }
        }

        x = 1;
        y = 24;
        List<ItemStack> pool = recipe.pool();
        for (ItemStack stack : pool) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(stack);
            x += 18;
            if (x + 18 > WIDTH) {
                x = 1;
                y += 18;
            }
        }
    }
}
