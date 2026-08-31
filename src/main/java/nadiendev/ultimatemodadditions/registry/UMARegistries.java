package nadiendev.ultimatemodadditions.registry;

import net.neoforged.bus.api.IEventBus;

public final class UMARegistries {

    private UMARegistries() {
    }

    public static void register(IEventBus modBus) {
        UMADataComponents.register(modBus);
        UMABlocks.register(modBus);
        UMAItems.register(modBus);
        UMAMenus.register(modBus);
        UMASounds.register(modBus);
        UMAAttachments.register(modBus);
        UMACreativeTabs.register(modBus);
    }
}
