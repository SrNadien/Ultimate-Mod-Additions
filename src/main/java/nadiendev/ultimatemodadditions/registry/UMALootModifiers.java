package nadiendev.ultimatemodadditions.registry;

import com.mojang.serialization.MapCodec;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.currency.CashChestModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class UMALootModifiers {

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, UMA.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<CashChestModifier>> CASH_IN_CHESTS =
            SERIALIZERS.register("cash_in_chests", () -> CashChestModifier.CODEC);

    private UMALootModifiers() {
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
