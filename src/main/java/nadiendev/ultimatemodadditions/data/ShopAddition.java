package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ShopAddition(ResourceLocation category, List<ShopItem> items) {

    public static final Codec<ShopAddition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("category").forGetter(ShopAddition::category),
            ShopCategory.ITEMS.fieldOf("items").forGetter(ShopAddition::items)
    ).apply(instance, ShopAddition::new));

    public ShopCategory applyTo(ShopCategory base) {
        List<ShopItem> merged = new ArrayList<>(base.items());
        for (ShopItem item : items) {
            merged.removeIf(existing -> ItemStackKey.same(existing.stack(), item.stack()));
            merged.add(item);
        }
        return new ShopCategory(base.name(), base.icon(), base.order(), merged);
    }
}
