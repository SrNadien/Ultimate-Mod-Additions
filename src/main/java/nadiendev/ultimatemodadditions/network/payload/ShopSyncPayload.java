package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.ShopCategory;
import nadiendev.ultimatemodadditions.data.ShopItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ShopSyncPayload(boolean open, boolean stand, boolean tiendita, boolean reload, boolean canAdd, boolean canRemove, String balance,
                              double commission, List<Entry> categories)
        implements CustomPacketPayload {

    public record Entry(ResourceLocation id, ShopCategory category) {
    }

    public static final Type<ShopSyncPayload> TYPE = new Type<>(UMA.id("shop_sync"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Item> ITEM = ByteBufCodecs.registry(Registries.ITEM);

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopSyncPayload> STREAM_CODEC =
            StreamCodec.of(ShopSyncPayload::write, ShopSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, ShopSyncPayload payload) {
        buffer.writeBoolean(payload.open);
        buffer.writeBoolean(payload.stand);
        buffer.writeBoolean(payload.tiendita);
        buffer.writeBoolean(payload.reload);
        buffer.writeBoolean(payload.canAdd);
        buffer.writeBoolean(payload.canRemove);
        buffer.writeUtf(payload.balance);
        buffer.writeDouble(payload.commission);
        if (!payload.reload && !payload.open) {
            return;
        }
        buffer.writeVarInt(payload.categories.size());
        for (Entry entry : payload.categories) {
            buffer.writeResourceLocation(entry.id);
            ShopCategory category = entry.category;
            buffer.writeUtf(category.name());
            ITEM.encode(buffer, category.icon());
            buffer.writeVarInt(category.order());
            buffer.writeVarInt(category.items().size());
            for (ShopItem item : category.items()) {
                ItemStack.STREAM_CODEC.encode(buffer, item.stack());
                buffer.writeDouble(item.buy());
                buffer.writeDouble(item.sell());
                buffer.writeBoolean(item.taxed());
            }
        }
    }

    private static ShopSyncPayload read(RegistryFriendlyByteBuf buffer) {
        boolean open = buffer.readBoolean();
        boolean stand = buffer.readBoolean();
        boolean tiendita = buffer.readBoolean();
        boolean reload = buffer.readBoolean();
        boolean canAdd = buffer.readBoolean();
        boolean canRemove = buffer.readBoolean();
        String balance = buffer.readUtf();
        double commission = buffer.readDouble();
        List<Entry> categories = new ArrayList<>();
        if (reload || open) {
            int count = buffer.readVarInt();
            for (int i = 0; i < count; i++) {
                ResourceLocation id = buffer.readResourceLocation();
                String name = buffer.readUtf();
                Item icon = ITEM.decode(buffer);
                int order = buffer.readVarInt();
                int size = buffer.readVarInt();
                List<ShopItem> items = new ArrayList<>(size);
                for (int j = 0; j < size; j++) {
                    ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
                    items.add(new ShopItem(stack, buffer.readDouble(), buffer.readDouble(), buffer.readBoolean()));
                }
                categories.add(new Entry(id, new ShopCategory(name, icon, order, items)));
            }
        }
        return new ShopSyncPayload(open, stand, tiendita, reload, canAdd, canRemove, balance, commission, categories);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
