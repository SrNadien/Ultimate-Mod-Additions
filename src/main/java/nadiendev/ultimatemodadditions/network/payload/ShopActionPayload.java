package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShopActionPayload(Action action, ResourceLocation category, int index, int amount, String text,
                                double buy, double sell) implements CustomPacketPayload {

    public enum Action {
        BUY,
        SELL,
        ADD_ITEM,
        EDIT_PRICE,
        REMOVE_ITEM,
        ADD_CATEGORY,
        REMOVE_CATEGORY
    }

    public static final Type<ShopActionPayload> TYPE = new Type<>(UMA.id("shop_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShopActionPayload> STREAM_CODEC =
            StreamCodec.of(ShopActionPayload::write, ShopActionPayload::read);

    private static final ResourceLocation NONE = ResourceLocation.withDefaultNamespace("none");

    public static ShopActionPayload trade(Action action, ResourceLocation category, int index, int amount) {
        return new ShopActionPayload(action, category, index, amount, "", 0.0D, 0.0D);
    }

    public static ShopActionPayload edit(Action action, ResourceLocation category, int index, String text,
                                         double buy, double sell) {
        return new ShopActionPayload(action, category == null ? NONE : category, index, 1, text, buy, sell);
    }

    private static void write(RegistryFriendlyByteBuf buffer, ShopActionPayload payload) {
        buffer.writeEnum(payload.action);
        buffer.writeResourceLocation(payload.category);
        buffer.writeVarInt(payload.index);
        buffer.writeVarInt(payload.amount);
        buffer.writeUtf(payload.text, 64);
        buffer.writeDouble(payload.buy);
        buffer.writeDouble(payload.sell);
    }

    private static ShopActionPayload read(RegistryFriendlyByteBuf buffer) {
        return new ShopActionPayload(buffer.readEnum(Action.class), buffer.readResourceLocation(),
                buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(64), buffer.readDouble(),
                buffer.readDouble());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
