package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;

public record DailyReward(Period period, List<Entry> entries) {

    public enum Period implements StringRepresentable {
        DAILY,
        WEEKLY,
        MONTHLY;

        public static final Codec<Period> CODEC = StringRepresentable.fromEnum(Period::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record Entry(double money, List<JobDefinition.RewardItem> items, List<String> commands) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.doubleRange(0.0D, 1.0E12D).optionalFieldOf("money", 0.0D).forGetter(Entry::money),
                JobDefinition.RewardItem.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Entry::items),
                Codec.STRING.listOf().optionalFieldOf("commands", List.of()).forGetter(Entry::commands)
        ).apply(instance, Entry::new));
    }

    public static final Codec<DailyReward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Period.CODEC.fieldOf("period").forGetter(DailyReward::period),
            Entry.CODEC.listOf().fieldOf("rewards").forGetter(DailyReward::entries)
    ).apply(instance, DailyReward::new));
}
