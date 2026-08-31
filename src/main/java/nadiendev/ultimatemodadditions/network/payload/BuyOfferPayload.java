package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BuyOfferPayload(ResourceLocation offer, int amount) implements CustomPacketPayload {

    public static final Type<BuyOfferPayload> TYPE = new Type<>(UMA.id("buy_offer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuyOfferPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, BuyOfferPayload::offer,
            ByteBufCodecs.VAR_INT, BuyOfferPayload::amount,
            BuyOfferPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
