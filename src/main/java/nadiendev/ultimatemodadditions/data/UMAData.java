package nadiendev.ultimatemodadditions.data;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.compat.CompatModules;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = UMA.MODID)
public final class UMAData {

    private static HolderLookup.Provider registries;

    private static final List<MobRule> SORTED_MOB_RULES = new ArrayList<>();

    public static final UMAJsonLoader<DifficultyProfile> DIFFICULTIES =
            new UMAJsonLoader<>("uma/difficulty", DifficultyProfile.CODEC, UMAData::registries, map -> {
            });

    public static final UMAJsonLoader<MobRule> MOB_RULES =
            new UMAJsonLoader<>("uma/mob_rules", MobRule.CODEC, UMAData::registries, map -> {
                SORTED_MOB_RULES.clear();
                SORTED_MOB_RULES.addAll(map.values());
                SORTED_MOB_RULES.sort(Comparator.comparingInt(MobRule::priority).reversed());
            });

    public static final UMAJsonLoader<LootBagDefinition> LOOT_BAGS =
            new UMAJsonLoader<>("uma/loot_bags", LootBagDefinition.CODEC, UMAData::registries, map -> {
            });

    public static final UMAJsonLoader<LootBagAddition> BAG_ADDITIONS =
            new UMAJsonLoader<>("uma/loot_bag_additions", LootBagAddition.CODEC, UMAData::registries, map -> {
            });

    public static final UMAJsonLoader<TradeOffer> TRADES =
            new UMAJsonLoader<>("uma/trades", TradeOffer.CODEC, UMAData::registries, map -> {
            });

    private UMAData() {
    }

    public static HolderLookup.Provider registryProvider() {
        return registries;
    }

    private static HolderLookup.Provider registries() {
        return registries;
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        registries = event.getRegistryAccess();
        event.addListener(DIFFICULTIES);
        event.addListener(MOB_RULES);
        event.addListener(LOOT_BAGS);
        event.addListener(BAG_ADDITIONS);
        event.addListener(TRADES);
        event.addListener(new RuntimeHookListener());
    }

    public static List<MobRule> mobRules() {
        return SORTED_MOB_RULES;
    }

    public static Map<ResourceLocation, LootBagDefinition> lootBags() {
        return LOOT_BAGS.values();
    }

    public static Map<ResourceLocation, TradeOffer> trades() {
        return TRADES.values();
    }

    public static DifficultyProfile difficulty(ResourceLocation id) {
        DifficultyProfile profile = DIFFICULTIES.values().get(id);
        return profile == null ? DifficultyProfile.IDENTITY : profile;
    }

    public static DifficultyProfile defaultDifficulty() {
        return difficulty(UMA.parse(UMAConfig.COMMON.defaultDifficulty.get()));
    }

    public static Map<ResourceLocation, DifficultyProfile> difficulties() {
        return DIFFICULTIES.values();
    }

    private static void applyJsonBagAdditions(RuntimeDataSink sink) {
        BAG_ADDITIONS.values().forEach(sink::addBagAddition);
    }

    private static final class RuntimeHookListener implements net.minecraft.server.packs.resources.PreparableReloadListener {

        @Override
        public java.util.concurrent.CompletableFuture<Void> reload(PreparationBarrier barrier,
                                                                   net.minecraft.server.packs.resources.ResourceManager manager,
                                                                   net.minecraft.util.profiling.ProfilerFiller preparationsProfiler,
                                                                   net.minecraft.util.profiling.ProfilerFiller reloadProfiler,
                                                                   java.util.concurrent.Executor backgroundExecutor,
                                                                   java.util.concurrent.Executor gameExecutor) {
            return barrier.wait(null).thenRunAsync(() -> {
                RuntimeDataSink sink = new RuntimeDataSink();
                CompatModules.onDataReload(sink);
                applyJsonBagAdditions(sink);
                if (sink.mobRulesDirty()) {
                    SORTED_MOB_RULES.clear();
                    SORTED_MOB_RULES.addAll(MOB_RULES.values().values());
                    SORTED_MOB_RULES.sort(Comparator.comparingInt(MobRule::priority).reversed());
                }
            }, gameExecutor);
        }
    }
}
