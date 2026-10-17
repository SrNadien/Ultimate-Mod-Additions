package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RentSyncPayload(boolean open, boolean inTeam, String teamName, int chunks, String costCents,
                              int periodDays, long remainingMs, long graceMs, long dayMs, boolean canPay)
        implements CustomPacketPayload {

    public static final Type<RentSyncPayload> TYPE = new Type<>(UMA.id("rent_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RentSyncPayload> STREAM_CODEC =
            StreamCodec.of(RentSyncPayload::write, RentSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, RentSyncPayload payload) {
        buffer.writeBoolean(payload.open);
        buffer.writeBoolean(payload.inTeam);
        buffer.writeUtf(payload.teamName);
        buffer.writeVarInt(payload.chunks);
        buffer.writeUtf(payload.costCents);
        buffer.writeVarInt(payload.periodDays);
        buffer.writeLong(payload.remainingMs);
        buffer.writeLong(payload.graceMs);
        buffer.writeLong(payload.dayMs);
        buffer.writeBoolean(payload.canPay);
    }

    private static RentSyncPayload read(RegistryFriendlyByteBuf buffer) {
        return new RentSyncPayload(buffer.readBoolean(), buffer.readBoolean(), buffer.readUtf(),
                buffer.readVarInt(), buffer.readUtf(), buffer.readVarInt(), buffer.readLong(), buffer.readLong(),
                buffer.readLong(), buffer.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
