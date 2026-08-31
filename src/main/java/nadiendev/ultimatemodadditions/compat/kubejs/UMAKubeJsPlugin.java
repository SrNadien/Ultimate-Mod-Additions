package nadiendev.ultimatemodadditions.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;

public class UMAKubeJsPlugin implements KubeJSPlugin {

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow("nadiendev.ultimatemodadditions.compat.kubejs");
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        if (bindings.type() == ScriptType.SERVER) {
            ScriptedEntries.clear();
        }
        bindings.add("UMA", new UMAKubeJsBindings(bindings.type()));
    }
}
