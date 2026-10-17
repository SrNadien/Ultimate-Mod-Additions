package nadiendev.ultimatemodadditions.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public record JobDefinition(String name,
                            String description,
                            Item icon,
                            int maxLevel,
                            double xpBase,
                            double xpGrowth,
                            double incomePerLevel,
                            List<Title> titles,
                            List<Reward> rewards,
                            List<JobAction> actions) {

    public record Title(int level, String name) {
        public static final Codec<Title> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 10_000).fieldOf("level").forGetter(Title::level),
                Codec.STRING.fieldOf("name").forGetter(Title::name)
        ).apply(instance, Title::new));
    }

    public record RewardItem(Item item, int count) {
        public static final Codec<RewardItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(RewardItem::item),
                Codec.intRange(1, 4096).optionalFieldOf("count", 1).forGetter(RewardItem::count)
        ).apply(instance, RewardItem::new));
    }

    public record Reward(int level, int every, double money, List<RewardItem> items, List<String> commands) {
        public static final Codec<Reward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 1_000_000).optionalFieldOf("level", 0).forGetter(Reward::level),
                Codec.intRange(0, 1_000_000).optionalFieldOf("every", 0).forGetter(Reward::every),
                Codec.doubleRange(0.0D, 1.0E12D).optionalFieldOf("money", 0.0D).forGetter(Reward::money),
                RewardItem.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(Reward::items),
                Codec.STRING.listOf().optionalFieldOf("commands", List.of()).forGetter(Reward::commands)
        ).apply(instance, Reward::new));

        public boolean reaches(int reached) {
            return (level > 0 && level == reached) || (every > 0 && reached % every == 0);
        }
    }

    public static final Codec<JobDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(JobDefinition::name),
            Codec.STRING.optionalFieldOf("description", "").forGetter(JobDefinition::description),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("icon", Items.BOOK).forGetter(JobDefinition::icon),
            Codec.intRange(1, 1_000_000).optionalFieldOf("max_level", 100).forGetter(JobDefinition::maxLevel),
            Codec.doubleRange(1.0D, 1.0E9D).optionalFieldOf("xp_base", 100.0D).forGetter(JobDefinition::xpBase),
            Codec.doubleRange(1.0D, 10.0D).optionalFieldOf("xp_growth", 1.08D).forGetter(JobDefinition::xpGrowth),
            Codec.doubleRange(0.0D, 100.0D).optionalFieldOf("income_per_level", 0.0D)
                    .forGetter(JobDefinition::incomePerLevel),
            Title.CODEC.listOf().optionalFieldOf("titles", List.of()).forGetter(JobDefinition::titles),
            Reward.CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(JobDefinition::rewards),
            JobAction.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(JobDefinition::actions)
    ).apply(instance, JobDefinition::new));

    public String titleFor(int level) {
        String best = "";
        int bestLevel = 0;
        for (Title title : titles) {
            if (title.level() <= level && title.level() >= bestLevel) {
                best = title.name();
                bestLevel = title.level();
            }
        }
        return best;
    }

    public double xpNeeded(int level) {
        return xpBase * UMAConfig.COMMON.jobsXpNeededMultiplier.get() * Math.pow(xpGrowth, Math.max(0, level - 1));
    }

    public double incomeFactor(int level) {
        return 1.0D + incomePerLevel * Math.max(0, level - 1);
    }
}
