package nadiendev.ultimatemodadditions.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public final class JsonValues {

    private JsonValues() {
    }

    public static boolean bool(JsonObject object, String key, boolean fallback) {
        JsonElement element = object == null ? null : object.get(key);
        try {
            return element == null || element.isJsonNull() ? fallback : element.getAsBoolean();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public static int integer(JsonObject object, String key, int fallback) {
        JsonElement element = object == null ? null : object.get(key);
        try {
            return element == null || element.isJsonNull() ? fallback : element.getAsInt();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public static long number(JsonObject object, String key, long fallback) {
        JsonElement element = object == null ? null : object.get(key);
        try {
            return element == null || element.isJsonNull() ? fallback : element.getAsLong();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public static double decimal(JsonObject object, String key, double fallback) {
        JsonElement element = object == null ? null : object.get(key);
        try {
            return element == null || element.isJsonNull() ? fallback : element.getAsDouble();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public static String string(JsonObject object, String key, String fallback) {
        JsonElement element = object == null ? null : object.get(key);
        try {
            return element == null || element.isJsonNull() ? fallback : element.getAsString();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public static JsonObject object(JsonObject object, String key) {
        JsonElement element = object == null ? null : object.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
    }

    public static List<String> strings(JsonObject object, String key) {
        List<String> result = new ArrayList<>();
        JsonElement element = object == null ? null : object.get(key);
        if (element != null && element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                if (entry.isJsonPrimitive()) {
                    result.add(entry.getAsString());
                }
            }
        }
        return result;
    }

    public static List<JsonObject> objects(JsonObject object, String key) {
        List<JsonObject> result = new ArrayList<>();
        JsonElement element = object == null ? null : object.get(key);
        if (element != null && element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                if (entry.isJsonObject()) {
                    result.add(entry.getAsJsonObject());
                }
            }
        }
        return result;
    }

    public static JsonArray array(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    public static JsonArray range(double min, double max) {
        JsonArray array = new JsonArray();
        array.add(min);
        array.add(max);
        return array;
    }

    public static double[] range(JsonObject object, String key, double min, double max) {
        JsonElement element = object == null ? null : object.get(key);
        if (element != null && element.isJsonArray() && element.getAsJsonArray().size() >= 2) {
            try {
                JsonArray array = element.getAsJsonArray();
                double first = array.get(0).getAsDouble();
                double second = array.get(1).getAsDouble();
                return new double[]{Math.min(first, second), Math.max(first, second)};
            } catch (RuntimeException ignored) {
            }
        }
        return new double[]{min, max};
    }
}
