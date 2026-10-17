package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record JobActionPayload(ResourceLocation job, boolean join) implements CustomPacketPayload {

    public static final Type<JobActionPayload> TYPE = new Type<>(UMA.id("job_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, JobActionPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, JobActionPayload::job,
            ByteBufCodecs.BOOL, JobActionPayload::join,
            JobActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
