package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RentActionPayload() implements CustomPacketPayload {

    public static final Type<RentActionPayload> TYPE = new Type<>(UMA.id("rent_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RentActionPayload> STREAM_CODEC =
            StreamCodec.unit(new RentActionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
