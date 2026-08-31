package nadiendev.ultimatemodadditions.compat;

import nadiendev.ultimatemodadditions.data.RuntimeDataSink;
import net.neoforged.bus.api.IEventBus;

public interface CompatModule {

    default void onConstruct(IEventBus modBus) {
    }

    default void onDataReload(RuntimeDataSink sink) {
    }
}
