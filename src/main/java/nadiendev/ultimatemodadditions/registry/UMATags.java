package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class UMATags {

    public static final TagKey<Item> NOT_PURCHASABLE = item("not_purchasable");



    private UMATags() {
    }

    private static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, UMA.id(path));
    }
}
