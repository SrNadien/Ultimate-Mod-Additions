package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.currency.CashItem;
import nadiendev.ultimatemodadditions.data.ShopCategory;
import nadiendev.ultimatemodadditions.data.ShopItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ShopFloors {

    private final Set<ResourceLocation> ids = new HashSet<>();
    private final List<TagKey<Item>> tags = new ArrayList<>();
    private final Map<ResourceLocation, Double> fixedById = new HashMap<>();
    private final Map<TagKey<Item>, Double> fixedByTag = new LinkedHashMap<>();

    private ShopFloors() {
        for (String entry : ShopPrices.OP_ITEMS) {
            boolean tag = entry.startsWith("#");
            ResourceLocation id = ResourceLocation.tryParse(tag ? entry.substring(1) : entry);
            if (id == null) {
                continue;
            }
            if (tag) {
                tags.add(TagKey.create(Registries.ITEM, id));
            } else {
                ids.add(id);
            }
        }
        ShopPrices.FIXED.forEach((key, price) -> {
            boolean tag = key.startsWith("#");
            ResourceLocation id = ResourceLocation.tryParse(tag ? key.substring(1) : key);
            if (id == null) {
                return;
            }
            if (tag) {
                fixedByTag.put(TagKey.create(Registries.ITEM, id), price);
            } else {
                fixedById.put(id, price);
            }
        });
    }

    static ShopFloors current() {
        return new ShopFloors();
    }

    private Double fixed(ItemStack stack) {
        Double byId = fixedById.get(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        if (byId != null) {
            return byId;
        }
        for (Map.Entry<TagKey<Item>, Double> entry : fixedByTag.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private boolean listed(ItemStack stack) {
        if (ids.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return true;
        }
        for (TagKey<Item> tag : tags) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    double shift(String categoryId, ShopCategory category, double buyScale) {
        double start = ShopPrices.STARTS.getOrDefault(categoryId, ShopPrices.DEFAULT_START);
        double cheapest = Double.MAX_VALUE;
        for (ShopItem item : category.items()) {
            if (item.stack().getItem() instanceof CashItem || item.buy() <= 0.0D || fixed(item.stack()) != null) {
                continue;
            }
            cheapest = Math.min(cheapest, item.buy() * buyScale);
        }
        return cheapest == Double.MAX_VALUE ? 0.0D : Math.max(0.0D, start - cheapest);
    }

    double apply(ItemStack stack, double price, double shift) {
        if (price <= 0.0D) {
            return price;
        }
        Double fixed = fixed(stack);
        if (fixed != null) {
            return fixed;
        }
        double result = price + shift;
        boolean op = listed(stack) || price >= ShopPrices.OP_AUTO_FROM;
        return op ? Math.max(result, ShopPrices.OP_START) : result;
    }
}
