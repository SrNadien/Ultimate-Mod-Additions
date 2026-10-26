package nadiendev.ultimatemodadditions.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class EventConfigs {

    public record Spec(String name, String header, Supplier<JsonObject> defaults, boolean yaml) {
    }

    private static final class Cached {
        private long modified;
        private long size;
        private int scripted;
        private long checkedAt;
        private JsonObject merged;
    }

    private static final long RECHECK_MILLIS = 1000L;
    private static final Map<String, Cached> CACHE = new HashMap<>();

    private EventConfigs() {
    }

    public static Path path(String name, String extension) {
        return FMLPaths.CONFIGDIR.get().resolve(UMA.MODID).resolve(name + "." + extension);
    }

    private static Path find(Spec spec) {
        Path primary = path(spec.name(), spec.yaml() ? "yml" : "json5");
        Path other = path(spec.name(), spec.yaml() ? "json5" : "yml");
        if (!Files.exists(primary) && Files.exists(other)) {
            return other;
        }
        Path yaml = path(spec.name(), "yaml");
        if (!Files.exists(primary) && Files.exists(yaml)) {
            return yaml;
        }
        return primary;
    }

    public static Path fileFor(Spec spec) {
        return find(spec);
    }

    public static synchronized void forget(String name) {
        CACHE.remove(name);
    }

    private static JsonObject read(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".json5") ? Json5.readObject(path) : Yaml.readObject(path);
    }

    private static void writeDefaults(Spec spec, Path path) {
        if (path.getFileName().toString().endsWith(".json5")) {
            Json5.write(path, spec.header(), spec.defaults().get());
        } else {
            Yaml.write(path, spec.header().replaceAll("(?m)^\s*//", "#"), spec.defaults().get());
        }
    }

    public static synchronized JsonObject get(Spec spec) {
        long now = System.currentTimeMillis();
        int scripted = ScriptedConfigs.version();
        Cached cached = CACHE.get(spec.name());
        if (cached != null && cached.scripted == scripted && now - cached.checkedAt < RECHECK_MILLIS) {
            return cached.merged;
        }
        Path path = find(spec);
        if (!Files.exists(path)) {
            writeDefaults(spec, path);
        }
        long modified = 0L;
        long size = 0L;
        try {
            modified = Files.getLastModifiedTime(path).toMillis();
            size = Files.size(path);
        } catch (IOException ignored) {
        }
        if (cached != null && cached.scripted == scripted && cached.modified == modified && cached.size == size) {
            cached.checkedAt = now;
            return cached.merged;
        }
        JsonObject merged = spec.defaults().get();
        JsonObject file = Files.exists(path) ? read(path) : null;
        if (file != null) {
            merge(merged, file);
        }
        JsonObject overrides = ScriptedConfigs.overrides(spec.name());
        if (overrides != null) {
            merge(merged, overrides);
        }
        Cached fresh = new Cached();
        fresh.modified = modified;
        fresh.size = size;
        fresh.scripted = scripted;
        fresh.checkedAt = now;
        fresh.merged = merged;
        CACHE.put(spec.name(), fresh);
        return merged;
    }

    public static void merge(JsonObject target, JsonObject source) {
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            JsonElement existing = target.get(entry.getKey());
            if (existing != null && existing.isJsonObject() && entry.getValue().isJsonObject()) {
                merge(existing.getAsJsonObject(), entry.getValue().getAsJsonObject());
            } else {
                target.add(entry.getKey(), entry.getValue().deepCopy());
            }
        }
    }
}
