package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BagTier;
import nadiendev.ultimatemodadditions.currency.CashItem;
import nadiendev.ultimatemodadditions.currency.Denomination;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UMACreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UMA.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ultimatemodadditions"))
                    .icon(() -> new ItemStack(UMAItems.cash(Denomination.L1)))
                    .displayItems((parameters, output) -> {
                        for (DeferredItem<CashItem> cash : UMAItems.allCash()) {
                            output.accept(cash.get());
                        }
                        output.accept(UMAItems.WALLET.get());
                        output.accept(UMAItems.DEBIT_CARD.get());
                        output.accept(UMABlocks.ATM.get());
                        for (BagTier tier : BagTier.values()) {
                            output.accept(UMAItems.bag(tier));
                        }
                        output.accept(UMAItems.COLISEUM_KEY.get());
                        output.accept(UMABlocks.EXCHANGE_STAND.get());
                        output.accept(UMAItems.FLASHERO_SPAWN_EGG.get());
                    })
                    .build());

    private UMACreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
