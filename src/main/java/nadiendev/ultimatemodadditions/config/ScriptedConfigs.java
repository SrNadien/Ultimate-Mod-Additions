package nadiendev.ultimatemodadditions.config;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ScriptedConfigs {

    private static final Map<String, JsonObject> OVERRIDES = new HashMap<>();
    private static final Map<String, List<String>> BANS = new HashMap<>();
    private static int version;

    private ScriptedConfigs() {
    }

    public static synchronized void clear() {
        OVERRIDES.clear();
        BANS.clear();
        version++;
    }

    public static synchronized void merge(String name, JsonObject values) {
        JsonObject target = OVERRIDES.computeIfAbsent(name, key -> new JsonObject());
        EventConfigs.merge(target, values);
        version++;
    }

    public static synchronized JsonObject overrides(String name) {
        JsonObject found = OVERRIDES.get(name);
        return found == null ? null : found.deepCopy();
    }

    public static synchronized void addBan(String mode, String entry) {
        BANS.computeIfAbsent(mode, key -> new ArrayList<>()).add(entry);
        version++;
    }

    public static synchronized Map<String, List<String>> bans() {
        Map<String, List<String>> copy = new HashMap<>();
        BANS.forEach((mode, entries) -> copy.put(mode, List.copyOf(entries)));
        return copy;
    }

    public static synchronized int version() {
        return version;
    }
}
