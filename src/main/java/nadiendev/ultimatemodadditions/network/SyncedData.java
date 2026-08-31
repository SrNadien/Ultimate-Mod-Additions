package nadiendev.ultimatemodadditions.network;

import nadiendev.ultimatemodadditions.data.LootBagDefinition;
import nadiendev.ultimatemodadditions.data.TradeOffer;
import nadiendev.ultimatemodadditions.data.UMAData;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SyncedData {

    private static final Map<ResourceLocation, LootBagDefinition> BAGS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, TradeOffer> TRADES = new LinkedHashMap<>();

    private SyncedData() {
    }

    public static void accept(Map<ResourceLocation, LootBagDefinition> bags,
                              Map<ResourceLocation, TradeOffer> trades) {
        BAGS.clear();
        BAGS.putAll(bags);
        TRADES.clear();
        TRADES.putAll(trades);
        refreshJei();
    }

    private static void refreshJei() {
        if (!nadiendev.ultimatemodadditions.compat.ModIds.jei()
                || !net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            return;
        }
        try {
            Class.forName("nadiendev.ultimatemodadditions.compat.jei.UMAJeiPlugin")
                    .getMethod("refresh").invoke(null);
        } catch (Throwable ignored) {
        }
    }

    public static Map<ResourceLocation, LootBagDefinition> bags() {
        return BAGS.isEmpty() ? UMAData.lootBags() : BAGS;
    }

    public static LootBagDefinition bag(ResourceLocation id) {
        return bags().get(id);
    }

    public static Map<ResourceLocation, TradeOffer> trades() {
        return TRADES.isEmpty() ? UMAData.trades() : TRADES;
    }
}
