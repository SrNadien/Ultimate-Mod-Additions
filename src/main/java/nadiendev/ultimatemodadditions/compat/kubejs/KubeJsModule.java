package nadiendev.ultimatemodadditions.compat.kubejs;

import nadiendev.ultimatemodadditions.compat.CompatModule;
import nadiendev.ultimatemodadditions.data.RuntimeDataSink;

public class KubeJsModule implements CompatModule {

    @Override
    public void onDataReload(RuntimeDataSink sink) {
        ScriptedEntries.replay(sink);
    }
}
