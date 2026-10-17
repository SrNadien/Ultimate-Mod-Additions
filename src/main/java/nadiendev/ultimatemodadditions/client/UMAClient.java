package nadiendev.ultimatemodadditions.client;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.client.screen.AtmScreen;
import nadiendev.ultimatemodadditions.client.screen.ExchangeScreen;
import nadiendev.ultimatemodadditions.client.screen.WalletScreen;
import nadiendev.ultimatemodadditions.currency.CashPreview;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import nadiendev.ultimatemodadditions.registry.UMAMenus;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = UMA.MODID, value = Dist.CLIENT)
public final class UMAClient {

    private UMAClient() {
    }

    @SubscribeEvent
    public static void onRegisterTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(CashPreview.class, ClientCashPreview::new);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> FastColor.ARGB32.opaque(tint == 0 ? 0xC8A24A : 0x2E7D32),
                UMAItems.FLASHERO_SPAWN_EGG.get());
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(UMAMenus.EXCHANGE.get(), ExchangeScreen::new);
        event.register(UMAMenus.WALLET.get(), WalletScreen::new);
        event.register(UMAMenus.ATM.get(), AtmScreen::new);
    }
}
