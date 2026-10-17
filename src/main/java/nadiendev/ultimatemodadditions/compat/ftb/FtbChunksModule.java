package nadiendev.ultimatemodadditions.compat.ftb;

import nadiendev.ultimatemodadditions.compat.CompatModule;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class FtbChunksModule implements CompatModule {

    @Override
    public void onConstruct(IEventBus modBus) {
        modBus.addListener(RentNetwork::register);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, RentService::onServerTick);
    }
}
