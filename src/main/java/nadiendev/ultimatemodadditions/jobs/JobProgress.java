package nadiendev.ultimatemodadditions.jobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record JobProgress(int level, double xp, double earned, long day, double today) {

    public static final Codec<JobProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("level", 1).forGetter(JobProgress::level),
            Codec.DOUBLE.optionalFieldOf("xp", 0.0D).forGetter(JobProgress::xp),
            Codec.DOUBLE.optionalFieldOf("earned", 0.0D).forGetter(JobProgress::earned),
            Codec.LONG.optionalFieldOf("day", 0L).forGetter(JobProgress::day),
            Codec.DOUBLE.optionalFieldOf("today", 0.0D).forGetter(JobProgress::today)
    ).apply(instance, JobProgress::new));

    public static JobProgress start() {
        return new JobProgress(1, 0.0D, 0.0D, 0L, 0.0D);
    }

    public JobProgress with(int newLevel, double newXp) {
        return new JobProgress(newLevel, newXp, earned, day, today);
    }

    public double earnedOn(long currentDay) {
        return day == currentDay ? today : 0.0D;
    }

    public JobProgress earn(double mangos, long currentDay) {
        return new JobProgress(level, xp, earned + mangos, currentDay, earnedOn(currentDay) + mangos);
    }
}
