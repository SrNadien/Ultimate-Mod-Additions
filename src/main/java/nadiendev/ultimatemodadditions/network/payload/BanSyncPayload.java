package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record BanSyncPayload(List<Entry> entries) implements CustomPacketPayload {

    public record Entry(ResourceLocation item, int modes, boolean disabled, String message) {
    }

    public static final Type<BanSyncPayload> TYPE = new Type<>(UMA.id("ban_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BanSyncPayload> STREAM_CODEC =
            StreamCodec.of(BanSyncPayload::write, BanSyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, BanSyncPayload payload) {
        buffer.writeVarInt(payload.entries.size());
        for (Entry entry : payload.entries) {
            buffer.writeResourceLocation(entry.item);
            buffer.writeVarInt(entry.modes);
            buffer.writeBoolean(entry.disabled);
            buffer.writeUtf(entry.message);
        }
    }

    private static BanSyncPayload read(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(buffer.readResourceLocation(), buffer.readVarInt(), buffer.readBoolean(), buffer.readUtf()));
        }
        return new BanSyncPayload(entries);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
