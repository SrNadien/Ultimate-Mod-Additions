package nadiendev.ultimatemodadditions.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import nadiendev.ultimatemodadditions.UMA;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.resources.ResourceLocation;

public class UMAKubeJsBindings {

    private final ScriptType type;

    public UMAKubeJsBindings(ScriptType type) {
        this.type = type;
    }

    private void checkScope(String what) {
        if (type != ScriptType.SERVER) {
            UMA.LOGGER.warn("UMA.{} se llamo desde {} scripts. Los datos de UMA solo sobreviven en "
                    + "server_scripts: esta entrada se va a descartar al cargar el mundo.", what, type);
        }
    }

    public UMABuilders.Difficulty difficulty(String id) {
        checkScope("difficulty");
        return new UMABuilders.Difficulty(id);
    }

    public UMABuilders.MobRule mobRule(String id) {
        checkScope("mobRule");
        return new UMABuilders.MobRule(id);
    }

    public UMABuilders.LootBag lootBag(String id) {
        checkScope("lootBag");
        return new UMABuilders.LootBag(id);
    }

    public UMABuilders.BagDrop bagDrop(String id) {
        checkScope("bagDrop");
        return new UMABuilders.BagDrop(id);
    }

    public UMABuilders.Trade trade(String id) {
        checkScope("trade");
        return new UMABuilders.Trade(id);
    }


    public void addDifficulty(Context cx, String id, Object json) {
        store(cx, ScriptedEntries.Kind.DIFFICULTY, id, json);
    }

    public void addMobRule(Context cx, String id, Object json) {
        store(cx, ScriptedEntries.Kind.MOB_RULE, id, json);
    }

    public void addLootBag(Context cx, String id, Object json) {
        store(cx, ScriptedEntries.Kind.LOOT_BAG, id, json);
    }

    public void addBagDrop(Context cx, String id, Object json) {
        store(cx, ScriptedEntries.Kind.BAG_ADDITION, id, json);
    }

    public void addTrade(Context cx, String id, Object json) {
        store(cx, ScriptedEntries.Kind.TRADE, id, json);
    }

    private void store(Context cx, ScriptedEntries.Kind kind, String id, Object json) {
        checkScope(kind.name().toLowerCase(java.util.Locale.ROOT));
        ResourceLocation key = UMA.parse(id);
        JsonElement element = json instanceof String text
                ? JsonParser.parseString(text)
                : JsonUtils.of(cx, json);
        if (element == null || element.isJsonNull()) {
            UMA.LOGGER.error("KubeJS passed empty data for {}", key);
            return;
        }
        ScriptedEntries.add(kind, key, element);
    }
}
