package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.currency.Denomination;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Map;

@EventBusSubscriber(modid = UMA.MODID)
public final class UMAMigration {

    private static final Map<String, Long> LEGACY_COINS = Map.of(
            "coin_10", 10L,
            "coin_20", 20L,
            "coin_50", 50L,
            "coin_100", 100L,
            "coin_200", 200L,
            "coin_500", 500L,
            "coin_1000", 1_000L,
            "coin_10000", 10_000L,
            "coin_20000", 20_000L);

    private UMAMigration() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (!event.getRegistryKey().equals(Registries.ITEM)) {
            return;
        }
        LEGACY_COINS.forEach((legacy, mangos) ->
                BuiltInRegistries.ITEM.addAlias(UMA.id(legacy), UMA.id(Denomination.nearestToMangos(mangos).itemName())));
    }
}
