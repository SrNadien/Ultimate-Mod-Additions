package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.data.JobDefinition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UMADailyProvider extends UMAJsonProvider {

    public UMADailyProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, "uma/daily", "Daily rewards");
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    @Override
    void generate(Emitter emitter) {
        emitter.accept("daily", DailyReward.CODEC, new DailyReward(DailyReward.Period.DAILY, List.of(
                new DailyReward.Entry(10.0, List.of(), List.of()),
                new DailyReward.Entry(15.0, List.of(), List.of()),
                new DailyReward.Entry(20.0, List.of(new JobDefinition.RewardItem(item("minecraft:bread"), 8)), List.of()),
                new DailyReward.Entry(30.0, List.of(), List.of()),
                new DailyReward.Entry(40.0, List.of(new JobDefinition.RewardItem(item("minecraft:iron_ingot"), 4)), List.of()),
                new DailyReward.Entry(60.0, List.of(), List.of()),
                new DailyReward.Entry(100.0, List.of(new JobDefinition.RewardItem(item("minecraft:diamond"), 1)), List.of()))));
        emitter.accept("monthly", DailyReward.CODEC, new DailyReward(DailyReward.Period.MONTHLY, List.of(
                new DailyReward.Entry(600.0, List.of(new JobDefinition.RewardItem(item("minecraft:netherite_ingot"), 1), new JobDefinition.RewardItem(item("minecraft:diamond"), 8), new JobDefinition.RewardItem(item("minecraft:enchanted_golden_apple"), 1)), List.of()))));
        emitter.accept("weekly", DailyReward.CODEC, new DailyReward(DailyReward.Period.WEEKLY, List.of(
                new DailyReward.Entry(150.0, List.of(new JobDefinition.RewardItem(item("minecraft:diamond"), 2), new JobDefinition.RewardItem(item("minecraft:golden_apple"), 2)), List.of()))));
    }
}
