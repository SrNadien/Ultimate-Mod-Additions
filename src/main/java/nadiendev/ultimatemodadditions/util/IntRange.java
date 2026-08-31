package nadiendev.ultimatemodadditions.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

public record IntRange(int min, int max) {

    public static final Codec<IntRange> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("min", 0).forGetter(IntRange::min),
            Codec.INT.optionalFieldOf("max", 0).forGetter(IntRange::max)
    ).apply(instance, IntRange::new));

    public static final Codec<IntRange> CODEC = Codec.either(Codec.INT, OBJECT_CODEC)
            .xmap(either -> either.map(IntRange::exactly, range -> range),
                    range -> range.min == range.max ? com.mojang.datafixers.util.Either.left(range.min)
                            : com.mojang.datafixers.util.Either.right(range));

    public static final StreamCodec<RegistryFriendlyByteBuf, IntRange> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, IntRange::min,
            ByteBufCodecs.VAR_INT, IntRange::max,
            IntRange::new);

    public static IntRange exactly(int value) {
        return new IntRange(value, value);
    }

    public IntRange {
        if (max < min) {
            max = min;
        }
    }

    public int sample(RandomSource random) {
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }

    public boolean isZero() {
        return min <= 0 && max <= 0;
    }
}
