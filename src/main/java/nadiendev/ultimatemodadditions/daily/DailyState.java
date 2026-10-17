package nadiendev.ultimatemodadditions.daily;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DailyState(long lastDay, int streak, long lastWeek, long lastMonth) {

    public static final Codec<DailyState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("last_day", -1L).forGetter(DailyState::lastDay),
            Codec.INT.optionalFieldOf("streak", 0).forGetter(DailyState::streak),
            Codec.LONG.optionalFieldOf("last_week", -1L).forGetter(DailyState::lastWeek),
            Codec.LONG.optionalFieldOf("last_month", -1L).forGetter(DailyState::lastMonth)
    ).apply(instance, DailyState::new));

    public static DailyState fresh() {
        return new DailyState(-1L, 0, -1L, -1L);
    }
}
