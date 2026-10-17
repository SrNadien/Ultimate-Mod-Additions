package nadiendev.ultimatemodadditions.currency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record CardOwner(UUID id, String name) {

    public static final Codec<CardOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(CardOwner::id),
            Codec.STRING.fieldOf("name").forGetter(CardOwner::name)
    ).apply(instance, CardOwner::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CardOwner> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, CardOwner::id,
            ByteBufCodecs.STRING_UTF8, CardOwner::name,
            CardOwner::new);

    public String maskedNumber() {
        long hash = id.getMostSignificantBits() ^ id.getLeastSignificantBits();
        return String.format("%04d", Math.floorMod(hash, 10_000L));
    }
}
