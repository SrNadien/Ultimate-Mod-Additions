package nadiendev.ultimatemodadditions.client;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.client.screen.ExchangeScreen;
import nadiendev.ultimatemodadditions.registry.UMAMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = UMA.MODID, value = Dist.CLIENT)
public final class UMAClient {

    private UMAClient() {
    }

    @SubscribeEvent
    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(UMAMenus.EXCHANGE.get(), ExchangeScreen::new);
    }
}
