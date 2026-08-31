package nadiendev.ultimatemodadditions.compat.kubejs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.DifficultyProfile;
import nadiendev.ultimatemodadditions.data.LootBagDefinition;
import nadiendev.ultimatemodadditions.data.MobRule;
import nadiendev.ultimatemodadditions.data.RuntimeDataSink;
import nadiendev.ultimatemodadditions.data.TradeOffer;
import nadiendev.ultimatemodadditions.data.UMAData;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ScriptedEntries {

    public enum Kind {
        DIFFICULTY, MOB_RULE, LOOT_BAG, BAG_ADDITION, TRADE
    }

    private static final Map<Kind, Map<ResourceLocation, JsonElement>> ENTRIES = new LinkedHashMap<>();

    private ScriptedEntries() {
    }

    public static void clear() {
        ENTRIES.clear();
    }

    public static void add(Kind kind, ResourceLocation id, JsonElement json) {
        ENTRIES.computeIfAbsent(kind, key -> new LinkedHashMap<>()).put(id, json);
    }

    public static void replay(RuntimeDataSink sink) {
        each(Kind.DIFFICULTY, DifficultyProfile.CODEC, sink::addDifficulty);
        each(Kind.MOB_RULE, MobRule.CODEC, sink::addMobRule);
        each(Kind.LOOT_BAG, LootBagDefinition.CODEC, sink::addLootBag);
        each(Kind.BAG_ADDITION, nadiendev.ultimatemodadditions.data.LootBagAddition.CODEC, sink::addBagAddition);
        each(Kind.TRADE, TradeOffer.CODEC, sink::addTrade);
    }

    private static <T> void each(Kind kind, Codec<T> codec, java.util.function.BiConsumer<ResourceLocation, T> consumer) {
        Map<ResourceLocation, JsonElement> map = ENTRIES.get(kind);
        if (map == null) {
            return;
        }
        DynamicOps<JsonElement> ops = ops();
        map.forEach((id, json) -> codec.parse(ops, json)
                .resultOrPartial(error -> UMA.LOGGER.error("KubeJS entry {} is invalid: {}", id, error))
                .ifPresent(value -> consumer.accept(id, value)));
    }

    private static DynamicOps<JsonElement> ops() {
        HolderLookup.Provider provider = UMAData.registryProvider();
        return provider == null ? JsonOps.INSTANCE : RegistryOps.create(JsonOps.INSTANCE, provider);
    }
}
