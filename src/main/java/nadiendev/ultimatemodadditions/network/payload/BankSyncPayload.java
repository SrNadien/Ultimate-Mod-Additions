package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BankSyncPayload(boolean own, String cents) implements CustomPacketPayload {

    public static final Type<BankSyncPayload> TYPE = new Type<>(UMA.id("bank_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BankSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, BankSyncPayload::own,
            ByteBufCodecs.STRING_UTF8, BankSyncPayload::cents,
            BankSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
