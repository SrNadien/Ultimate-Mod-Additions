package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.DailyReward;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DailyClaimPayload(DailyReward.Period period) implements CustomPacketPayload {

    public static final Type<DailyClaimPayload> TYPE = new Type<>(UMA.id("daily_claim"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DailyClaimPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> DailyReward.Period.values()[index], DailyReward.Period::ordinal),
            DailyClaimPayload::period, DailyClaimPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
