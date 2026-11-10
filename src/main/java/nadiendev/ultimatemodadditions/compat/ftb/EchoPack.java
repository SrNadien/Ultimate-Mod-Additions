package nadiendev.ultimatemodadditions.compat.ftb;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EchoPack extends AbstractPackResources {

    private static final Gson GSON = new Gson();
    private static final String ROOT = "/echoes/";
    private static final String FOLDER = "echo_definitions";
    private static final byte[] META = "{\"pack\":{\"description\":\"Ultimate Mod Additions echoes\",\"pack_format\":48}}"
            .getBytes(StandardCharsets.UTF_8);

    private Map<ResourceLocation, byte[]> files;

    public static List<String> names() {
        List<String> names = new ArrayList<>();
        JsonElement index = read(ROOT + "index.json");
        if (index != null && index.isJsonArray()) {
            index.getAsJsonArray().forEach(name -> names.add(name.getAsString()));
        }
        return names;
    }

    public static List<String> shortNames() {
        return names().stream().map(name -> name.substring(name.indexOf("echo_") + 5)).toList();
    }

    public static String resolve(String input) {
        String wanted = input.contains(":") ? input.substring(input.indexOf(':') + 1) : input;
        for (String name : names()) {
            if (name.equals(wanted) || name.endsWith("echo_" + wanted)) {
                return UMA.MODID + ":" + name;
            }
        }
        return null;
    }

    public EchoPack(PackLocationInfo location) {
        super(location);
    }

    private Map<ResourceLocation, byte[]> files() {
        if (files == null) {
            files = build();
        }
        return files;
    }

    private static Map<ResourceLocation, byte[]> build() {
        Map<ResourceLocation, byte[]> built = new HashMap<>();
        JsonElement index = read(ROOT + "index.json");
        if (index == null || !index.isJsonArray()) {
            return built;
        }
        for (JsonElement name : index.getAsJsonArray()) {
            JsonElement template = read(ROOT + FOLDER + "/" + name.getAsString() + ".json");
            if (template != null && template.isJsonObject()) {
                JsonObject echo = template.getAsJsonObject();
                filter(echo);
                built.put(ResourceLocation.fromNamespaceAndPath(UMA.MODID, FOLDER + "/" + name.getAsString() + ".json"),
                        GSON.toJson(echo).getBytes(StandardCharsets.UTF_8));
            }
        }
        return built;
    }

    private static JsonElement read(String path) {
        try (InputStream stream = EchoPack.class.getResourceAsStream(path)) {
            if (stream == null) {
                return null;
            }
            return GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonElement.class);
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    private static void filter(JsonObject echo) {
        if (!echo.has("stages") || !echo.get("stages").isJsonArray()) {
            return;
        }
        for (JsonElement element : echo.getAsJsonArray("stages")) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject stage = element.getAsJsonObject();
            if (stage.has("shop_unlock") && stage.get("shop_unlock").isJsonArray()) {
                JsonArray kept = new JsonArray();
                for (JsonElement entry : stage.getAsJsonArray("shop_unlock")) {
                    if (entry.isJsonObject() && filterItems(entry.getAsJsonObject())) {
                        kept.add(entry);
                    }
                }
                stage.add("shop_unlock", kept);
            }
            if (stage.has("completion_reward") && stage.get("completion_reward").isJsonObject()
                    && !filterItems(stage.getAsJsonObject("completion_reward"))) {
                stage.remove("completion_reward");
            }
        }
    }

    private static boolean filterItems(JsonObject holder) {
        if (!holder.has("item")) {
            return true;
        }
        JsonElement item = holder.get("item");
        boolean other = holder.has("experience") || holder.has("currency") || holder.has("command");
        if (item.isJsonArray()) {
            JsonArray kept = new JsonArray();
            for (JsonElement stack : item.getAsJsonArray()) {
                if (installed(stack)) {
                    kept.add(stack);
                }
            }
            if (kept.isEmpty()) {
                holder.remove("item");
                return other;
            }
            holder.add("item", kept);
            return true;
        }
        if (installed(item)) {
            return true;
        }
        holder.remove("item");
        return other;
    }

    private static boolean installed(JsonElement stack) {
        if (!stack.isJsonObject() || !stack.getAsJsonObject().has("id")) {
            return false;
        }
        ResourceLocation id = ResourceLocation.tryParse(stack.getAsJsonObject().get("id").getAsString());
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length == 1 && elements[0].equals("pack.mcmeta")) {
            return () -> new ByteArrayInputStream(META);
        }
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        byte[] data = type == PackType.SERVER_DATA ? files().get(location) : null;
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.SERVER_DATA || !UMA.MODID.equals(namespace)) {
            return;
        }
        files().forEach((location, data) -> {
            if (location.getPath().startsWith(path)) {
                output.accept(location, () -> new ByteArrayInputStream(data));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? Set.of(UMA.MODID) : Set.of();
    }

    @Override
    public void close() {
    }
}
