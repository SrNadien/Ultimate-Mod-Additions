package nadiendev.ultimatemodadditions.compat;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.RuntimeDataSink;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;

public final class CompatModules {

    private static final List<CompatModule> ACTIVE = new ArrayList<>();

    private CompatModules() {
    }

    public static void init(IEventBus modBus) {
        load(ModIds.KUBEJS, "nadiendev.ultimatemodadditions.compat.kubejs.KubeJsModule");

        for (CompatModule module : ACTIVE) {
            module.onConstruct(modBus);
        }
    }

    public static void onDataReload(RuntimeDataSink sink) {
        for (CompatModule module : ACTIVE) {
            try {
                module.onDataReload(sink);
            } catch (Throwable throwable) {
                UMA.LOGGER.error("Compat module {} failed during data reload", module.getClass().getName(), throwable);
            }
        }
    }

    public static List<CompatModule> active() {
        return ACTIVE;
    }

    private static void load(String modId, String className) {
        if (!ModIds.loaded(modId)) {
            return;
        }
        try {
            Class<?> type = Class.forName(className);
            ACTIVE.add((CompatModule) type.getDeclaredConstructor().newInstance());
            UMA.LOGGER.info("Enabled {} integration", modId);
        } catch (ClassNotFoundException notBuilt) {
            UMA.LOGGER.info("{} is installed but this build was compiled without its integration", modId);
        } catch (Throwable throwable) {
            UMA.LOGGER.error("Could not enable the {} integration", modId, throwable);
        }
    }
}
