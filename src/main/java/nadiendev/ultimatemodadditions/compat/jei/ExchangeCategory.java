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

public class ExchangeCategory implements IRecipeCategory<ExchangeRecipe> {

    private static final int WIDTH = 140;
    private static final int HEIGHT = 26;

    private final IDrawable icon;

    public ExchangeCategory(IGuiHelper helper, ItemStack iconStack) {
        this.icon = helper.createDrawableItemStack(iconStack);
    }

    @Override
    public RecipeType<ExchangeRecipe> getRecipeType() {
        return UMARecipeTypes.EXCHANGE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ultimatemodadditions.exchange");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ExchangeRecipe recipe, IFocusGroup focuses) {
        int x = 1;
        for (ItemStack stack : recipe.cost()) {
            builder.addSlot(RecipeIngredientRole.INPUT, x, 4).addItemStack(stack);
            x += 18;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH - 19, 4).addItemStack(recipe.result());
    }
}
