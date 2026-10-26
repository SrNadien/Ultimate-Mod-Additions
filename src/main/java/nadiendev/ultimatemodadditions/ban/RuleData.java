package nadiendev.ultimatemodadditions.ban;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.config.JsonValues;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public record RuleData(List<String> messages, Set<String> gamemodes, long cooldown, int bypassLevel, boolean log,
                       List<String> run, Set<String> entities, Selector materials) {

    public static final RuleData EMPTY = new RuleData(List.of(), Set.of(), 0L, -1, false, List.of(), Set.of(), null);

    public static RuleData of(List<String> messages) {
        return new RuleData(messages, Set.of(), 0L, -1, false, List.of(), Set.of(), null);
    }

    public static RuleData parse(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return EMPTY;
        }
        if (element.isJsonPrimitive()) {
            return of(List.of(element.getAsString()));
        }
        if (element.isJsonArray()) {
            return of(strings(element));
        }
        JsonObject object = element.getAsJsonObject();
        Set<String> gamemodes = new HashSet<>();
        for (String mode : names(object.get("gamemode"))) {
            gamemodes.add(mode.toLowerCase(Locale.ROOT));
        }
        Set<String> entities = new HashSet<>();
        for (String entity : names(object.get("entity"))) {
            entities.add(Selector.normalize(entity, false));
        }
        List<String> materials = names(object.get("material"));
        return new RuleData(strings(object.get("message")), gamemodes, JsonValues.number(object, "cooldown", 0L),
                JsonValues.integer(object, "bypass_level", -1), JsonValues.bool(object, "log", false),
                strings(object.get("run")), entities,
                materials.isEmpty() ? null : Selector.parse(String.join(",", materials), false));
    }

    private static List<String> strings(JsonElement element) {
        List<String> list = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return list;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(entry -> list.add(entry.getAsString()));
        } else {
            list.add(element.getAsString());
        }
        return list;
    }

    private static List<String> names(JsonElement element) {
        List<String> names = new ArrayList<>();
        for (String text : strings(element)) {
            names.addAll(Selector.split(text));
        }
        return names;
    }
}
