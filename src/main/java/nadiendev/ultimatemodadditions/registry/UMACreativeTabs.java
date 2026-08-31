package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BagTier;
import nadiendev.ultimatemodadditions.currency.Denomination;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UMACreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UMA.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ultimatemodadditions"))
                    .icon(() -> new ItemStack(UMAItems.coin(Denomination.D1000)))
                    .displayItems((parameters, output) -> {
                        for (Denomination denomination : Denomination.values()) {
                            output.accept(UMAItems.coin(denomination));
                        }
                        for (BagTier tier : BagTier.values()) {
                            output.accept(UMAItems.bag(tier));
                        }
                        output.accept(UMAItems.COLISEUM_KEY.get());
                        output.accept(UMABlocks.EXCHANGE_STAND.get());
                    })
                    .build());

    private UMACreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
