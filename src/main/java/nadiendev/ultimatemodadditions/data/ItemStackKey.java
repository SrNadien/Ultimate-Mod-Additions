package nadiendev.ultimatemodadditions.data;

import net.minecraft.world.item.ItemStack;

public final class ItemStackKey {

    private ItemStackKey() {
    }

    public static boolean same(ItemStack first, ItemStack second) {
        return ItemStack.isSameItemSameComponents(first, second);
    }
}
