package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.LootBagDefinition;
import nadiendev.ultimatemodadditions.data.TradeOffer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public record DataSyncPayload(Map<ResourceLocation, LootBagDefinition> bags,
                              Map<ResourceLocation, TradeOffer> trades) implements CustomPacketPayload {

    public static final Type<DataSyncPayload> TYPE = new Type<>(UMA.id("data_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DataSyncPayload> STREAM_CODEC = StreamCodec.composite(
            map(LootBagDefinition.CODEC), DataSyncPayload::bags,
            map(TradeOffer.CODEC), DataSyncPayload::trades,
            DataSyncPayload::new);

    private static <T> StreamCodec<RegistryFriendlyByteBuf, Map<ResourceLocation, T>> map(com.mojang.serialization.Codec<T> codec) {
        return ByteBufCodecs.map(size -> new LinkedHashMap<>(), ResourceLocation.STREAM_CODEC,
                ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
