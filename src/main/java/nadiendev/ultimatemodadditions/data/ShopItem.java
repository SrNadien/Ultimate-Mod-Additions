package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public record ShopItem(ItemStack stack, double buy, double sell, boolean taxed) {

    private static final Codec<ItemStack> STACK = Codec.withAlternative(ItemStack.SINGLE_ITEM_CODEC,
            BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem));

    public static final Codec<ShopItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            STACK.fieldOf("item").forGetter(ShopItem::stack),
            Codec.doubleRange(0.0D, 1.0E15D).optionalFieldOf("buy", 0.0D).forGetter(ShopItem::buy),
            Codec.doubleRange(0.0D, 1.0E15D).optionalFieldOf("sell", 0.0D).forGetter(ShopItem::sell),
            Codec.BOOL.optionalFieldOf("taxed", true).forGetter(ShopItem::taxed)
    ).apply(instance, ShopItem::new));

    public boolean buyable() {
        return buy > 0.0D;
    }

    public boolean sellable() {
        return sell > 0.0D;
    }
}
