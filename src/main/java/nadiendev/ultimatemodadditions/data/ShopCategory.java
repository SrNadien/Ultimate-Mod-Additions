package nadiendev.ultimatemodadditions.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public record ShopCategory(String name, Item icon, int order, List<ShopItem> items) {

    static final Codec<List<ShopItem>> ITEMS = Codec.of(ShopItem.CODEC.listOf(), new Decoder<>() {
        @Override
        public <T> DataResult<Pair<List<ShopItem>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getList(input).map(consumer -> {
                List<ShopItem> items = new ArrayList<>();
                consumer.accept(element -> ShopItem.CODEC.parse(ops, element)
                        .resultOrPartial(error -> UMA.LOGGER.error("Skipping shop item: {}", error))
                        .ifPresent(items::add));
                return Pair.of(items, input);
            });
        }
    });

    public static final Codec<ShopCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(ShopCategory::name),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("icon").forGetter(ShopCategory::icon),
            Codec.INT.optionalFieldOf("order", 0).forGetter(ShopCategory::order),
            ITEMS.optionalFieldOf("items", List.of()).forGetter(ShopCategory::items)
    ).apply(instance, ShopCategory::new));
}
