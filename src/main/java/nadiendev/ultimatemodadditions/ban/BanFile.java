package nadiendev.ultimatemodadditions.ban;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import nadiendev.ultimatemodadditions.config.Json5;
import nadiendev.ultimatemodadditions.config.Yaml;
import nadiendev.ultimatemodadditions.event.EventDefaults;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BanFile {

    private BanFile() {
    }

    public static int remove(String id) {
        Path path = EventConfigs.fileFor(EventDefaults.BANS);
        JsonObject root = read(path);
        if (root == null) {
            return 0;
        }
        String wanted = Selector.normalize(id, false);
        int removed = 0;
        for (String key : new String[]{"banned", "disabled"}) {
            removed += filter(root, key, wanted);
        }
        JsonElement rules = root.get("rules");
        if (rules != null && rules.isJsonArray()) {
            JsonArray kept = new JsonArray();
            for (JsonElement rule : rules.getAsJsonArray()) {
                String item = rule.isJsonObject() && rule.getAsJsonObject().has("item")
                        ? rule.getAsJsonObject().get("item").getAsString() : "";
                if (!item.isEmpty() && Selector.normalize(item, false).equals(wanted)) {
                    removed++;
                } else {
                    kept.add(rule);
                }
            }
            root.add("rules", kept);
        }
        if (removed > 0) {
            write(path, root);
        }
        return removed;
    }

    public static int clear() {
        Path path = EventConfigs.fileFor(EventDefaults.BANS);
        JsonObject root = read(path);
        if (root == null) {
            return 0;
        }
        int removed = 0;
        for (String key : new String[]{"banned", "disabled", "rules"}) {
            JsonElement element = root.get(key);
            if (element != null && element.isJsonArray()) {
                removed += element.getAsJsonArray().size();
            }
            root.add(key, new JsonArray());
        }
        JsonElement blacklist = root.get("blacklist");
        if (blacklist != null && blacklist.isJsonObject()) {
            removed += blacklist.getAsJsonObject().size();
        }
        root.add("blacklist", new JsonObject());
        write(path, root);
        return removed;
    }

    private static int filter(JsonObject root, String key, String wanted) {
        JsonElement element = root.get(key);
        if (element == null || !element.isJsonArray()) {
            return 0;
        }
        JsonArray kept = new JsonArray();
        int removed = 0;
        for (JsonElement entry : element.getAsJsonArray()) {
            if (entry.isJsonPrimitive() && Selector.normalize(entry.getAsString(), false).equals(wanted)) {
                removed++;
            } else {
                kept.add(entry);
            }
        }
        root.add(key, kept);
        return removed;
    }

    private static JsonObject read(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        return path.getFileName().toString().endsWith(".json5") ? Json5.readObject(path) : Yaml.readObject(path);
    }

    private static void write(Path path, JsonObject root) {
        StringBuilder header = new StringBuilder();
        try {
            for (String line : Files.readString(path, StandardCharsets.UTF_8).split("\n")) {
                String trimmed = line.stripLeading();
                if (trimmed.startsWith("#") || trimmed.startsWith("//") || trimmed.isEmpty()) {
                    header.append(line.replace("\r", "")).append('\n');
                } else {
                    break;
                }
            }
        } catch (IOException ignored) {
        }
        if (path.getFileName().toString().endsWith(".json5")) {
            Json5.write(path, header.toString(), root);
        } else {
            Yaml.write(path, header.toString(), root);
        }
        EventConfigs.forget(EventDefaults.BANS.name());
    }
}
