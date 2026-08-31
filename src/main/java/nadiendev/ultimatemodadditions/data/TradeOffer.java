package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record TradeOffer(Optional<String> name,
                         String category,
                         int price,
                         List<CostEntry> cost,
                         ResultEntry result,
                         int sortOrder,
                         int stock,
                         boolean enabled) {

    public static final Codec<TradeOffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name").forGetter(TradeOffer::name),
            Codec.STRING.optionalFieldOf("category", "general").forGetter(TradeOffer::category),
            Codec.INT.optionalFieldOf("price", 0).forGetter(TradeOffer::price),
            CostEntry.CODEC.listOf().optionalFieldOf("cost", List.of()).forGetter(TradeOffer::cost),
            ResultEntry.CODEC.fieldOf("result").forGetter(TradeOffer::result),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(TradeOffer::sortOrder),
            Codec.INT.optionalFieldOf("stock", -1).forGetter(TradeOffer::stock),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(TradeOffer::enabled)
    ).apply(instance, TradeOffer::new));

    public record CostEntry(Item item, int count) {
        public static final Codec<CostEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(CostEntry::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(CostEntry::count)
        ).apply(instance, CostEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CostEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(net.minecraft.core.registries.Registries.ITEM), CostEntry::item,
                ByteBufCodecs.VAR_INT, CostEntry::count,
                CostEntry::new);

        public ItemStack asStack() {
            return new ItemStack(item, count);
        }
    }

    public record ResultEntry(Optional<Item> item,
                              int count,
                              Optional<ResourceLocation> bag,
                              Optional<DataComponentPatch> components) {
        public static final Codec<ResultEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("item").forGetter(ResultEntry::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(ResultEntry::count),
                ResourceLocation.CODEC.optionalFieldOf("bag").forGetter(ResultEntry::bag),
                DataComponentPatch.CODEC.optionalFieldOf("components").forGetter(ResultEntry::components)
        ).apply(instance, ResultEntry::new));

        public ItemStack asStack() {
            if (bag.isPresent()) {
                return nadiendev.ultimatemodadditions.currency.LootBagItem.create(bag.get(), count);
            }
            return item.map(value -> {
                ItemStack stack = new ItemStack(value, count);
                components.ifPresent(stack::applyComponents);
                return stack;
            }).orElse(ItemStack.EMPTY);
        }
    }
}
