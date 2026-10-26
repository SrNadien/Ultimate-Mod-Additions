package nadiendev.ultimatemodadditions.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import nadiendev.ultimatemodadditions.UMA;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Yaml {

    private record Line(int indent, String text) {
    }

    private static final Pattern INTEGER = Pattern.compile("-?\\d{1,18}");
    private static final Pattern DECIMAL = Pattern.compile("-?\\d+\\.\\d+");
    private static final Pattern PLAIN_KEY = Pattern.compile("[A-Za-z0-9_.\\-]+");

    private Yaml() {
    }

    public static JsonObject readObject(Path path) {
        try {
            return parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException exception) {
            UMA.LOGGER.error("Could not read {}: {}", path, exception.getMessage());
            return null;
        }
    }

    public static void write(Path path, String header, JsonObject object) {
        try {
            Files.createDirectories(path.getParent());
            StringBuilder out = new StringBuilder(header);
            emitMap(object, 0, out);
            Files.writeString(path, out.toString(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            UMA.LOGGER.error("Could not write {}: {}", path, exception.getMessage());
        }
    }

    public static JsonObject parse(String text) {
        List<Line> lines = new ArrayList<>();
        for (String raw : text.split("\n", -1)) {
            String line = stripComment(raw.replace("\r", "")).stripTrailing();
            if (line.isBlank() || line.strip().equals("---")) {
                continue;
            }
            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') {
                indent++;
            }
            if (indent < line.length() && line.charAt(indent) == '\t') {
                throw new IllegalArgumentException("tabs are not allowed for indentation");
            }
            lines.add(new Line(indent, line.substring(indent)));
        }
        if (lines.isEmpty()) {
            return new JsonObject();
        }
        int[] index = {0};
        JsonElement root = block(lines, index, lines.get(0).indent());
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("the file must hold a map");
        }
        return root.getAsJsonObject();
    }

    private static String stripComment(String line) {
        char quote = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quote != 0) {
                if (c == '\\' && quote == '"') {
                    i++;
                } else if (c == quote) {
                    quote = 0;
                }
            } else if (c == '"' || c == '\'') {
                if (i == 0 || " \t[{,:-".indexOf(line.charAt(i - 1)) >= 0) {
                    quote = c;
                }
            } else if (c == '#' && (i == 0 || Character.isWhitespace(line.charAt(i - 1)))) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static JsonElement block(List<Line> lines, int[] index, int indent) {
        Line first = lines.get(index[0]);
        if (first.text().equals("-") || first.text().startsWith("- ")) {
            return list(lines, index, first.indent());
        }
        return map(lines, index, first.indent());
    }

    private static JsonObject map(List<Line> lines, int[] index, int indent) {
        JsonObject object = new JsonObject();
        while (index[0] < lines.size()) {
            Line line = lines.get(index[0]);
            if (line.indent() < indent || line.indent() == indent && (line.text().equals("-") || line.text().startsWith("- "))) {
                break;
            }
            if (line.indent() > indent) {
                throw new IllegalArgumentException("bad indentation near '" + line.text() + "'");
            }
            int colon = keyEnd(line.text());
            if (colon < 0) {
                throw new IllegalArgumentException("expected 'key: value' near '" + line.text() + "'");
            }
            String key = unquoteKey(line.text().substring(0, colon).trim());
            String rest = line.text().substring(colon + 1).trim();
            index[0]++;
            if (rest.isEmpty()) {
                if (index[0] < lines.size() && (lines.get(index[0]).indent() > indent
                        || lines.get(index[0]).indent() == indent && lines.get(index[0]).text().startsWith("-"))) {
                    object.add(key, block(lines, index, lines.get(index[0]).indent()));
                } else {
                    object.add(key, JsonNull.INSTANCE);
                }
            } else {
                object.add(key, scalar(rest));
            }
        }
        return object;
    }

    private static JsonArray list(List<Line> lines, int[] index, int indent) {
        JsonArray array = new JsonArray();
        while (index[0] < lines.size()) {
            Line line = lines.get(index[0]);
            if (line.indent() != indent || !(line.text().equals("-") || line.text().startsWith("- "))) {
                break;
            }
            String item = line.text().substring(1).trim();
            if (item.isEmpty()) {
                index[0]++;
                if (index[0] < lines.size() && lines.get(index[0]).indent() > indent) {
                    array.add(block(lines, index, lines.get(index[0]).indent()));
                } else {
                    array.add(JsonNull.INSTANCE);
                }
            } else if (keyEnd(item) >= 0 && !item.startsWith("[") && !item.startsWith("{") && !item.startsWith("\"") && !item.startsWith("'")
                    || quotedKey(item)) {
                lines.set(index[0], new Line(indent + 2, item));
                array.add(map(lines, index, indent + 2));
            } else {
                index[0]++;
                array.add(scalar(item));
            }
        }
        return array;
    }

    private static boolean quotedKey(String text) {
        if (!text.startsWith("\"") && !text.startsWith("'")) {
            return false;
        }
        char quote = text.charAt(0);
        int end = text.indexOf(quote, 1);
        return end > 0 && end + 1 < text.length() && text.charAt(end + 1) == ':'
                && (end + 2 >= text.length() || text.charAt(end + 2) == ' ');
    }

    private static int keyEnd(String text) {
        if (text.startsWith("\"") || text.startsWith("'")) {
            char quote = text.charAt(0);
            int end = 1;
            while (end < text.length()) {
                if (text.charAt(end) == '\\' && quote == '"') {
                    end += 2;
                    continue;
                }
                if (text.charAt(end) == quote) {
                    if (quote == '\'' && end + 1 < text.length() && text.charAt(end + 1) == '\'') {
                        end += 2;
                        continue;
                    }
                    break;
                }
                end++;
            }
            int colon = end + 1;
            return colon < text.length() && text.charAt(colon) == ':' && (colon + 1 >= text.length() || text.charAt(colon + 1) == ' ')
                    ? colon : -1;
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ':' && (i + 1 >= text.length() || text.charAt(i + 1) == ' ')) {
                return i;
            }
            if (text.charAt(i) == '[' || text.charAt(i) == '{') {
                return -1;
            }
        }
        return -1;
    }

    private static String unquoteKey(String key) {
        if (key.length() >= 2 && (key.startsWith("\"") || key.startsWith("'"))) {
            return unquote(key);
        }
        return key;
    }

    private static JsonElement scalar(String text) {
        String value = text.trim();
        if (value.startsWith("[") || value.startsWith("{")) {
            int[] position = {0};
            return flow(value, position);
        }
        return plain(value);
    }

    private static JsonElement plain(String value) {
        if (value.isEmpty() || value.equals("~") || value.equalsIgnoreCase("null")) {
            return JsonNull.INSTANCE;
        }
        if (value.startsWith("\"") || value.startsWith("'")) {
            return new JsonPrimitive(unquote(value));
        }
        if (value.equalsIgnoreCase("true")) {
            return new JsonPrimitive(true);
        }
        if (value.equalsIgnoreCase("false")) {
            return new JsonPrimitive(false);
        }
        if (INTEGER.matcher(value).matches()) {
            return new JsonPrimitive(Long.parseLong(value));
        }
        if (DECIMAL.matcher(value).matches()) {
            return new JsonPrimitive(Double.parseDouble(value));
        }
        return new JsonPrimitive(value);
    }

    private static String unquote(String value) {
        char quote = value.charAt(0);
        StringBuilder out = new StringBuilder();
        int i = 1;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (quote == '\'' && c == '\'') {
                if (i + 1 < value.length() && value.charAt(i + 1) == '\'') {
                    out.append('\'');
                    i += 2;
                    continue;
                }
                break;
            }
            if (quote == '"' && c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(i + 1);
                out.append(switch (next) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    default -> next;
                });
                i += 2;
                continue;
            }
            if (quote == '"' && c == '"') {
                break;
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static JsonElement flow(String text, int[] position) {
        skip(text, position);
        char open = text.charAt(position[0]);
        if (open == '[') {
            position[0]++;
            JsonArray array = new JsonArray();
            while (true) {
                skip(text, position);
                if (position[0] >= text.length()) {
                    throw new IllegalArgumentException("unclosed '[' in '" + text + "'");
                }
                if (text.charAt(position[0]) == ']') {
                    position[0]++;
                    return array;
                }
                array.add(flowValue(text, position));
                skip(text, position);
                if (position[0] < text.length() && text.charAt(position[0]) == ',') {
                    position[0]++;
                }
            }
        }
        position[0]++;
        JsonObject object = new JsonObject();
        while (true) {
            skip(text, position);
            if (position[0] >= text.length()) {
                throw new IllegalArgumentException("unclosed '{' in '" + text + "'");
            }
            if (text.charAt(position[0]) == '}') {
                position[0]++;
                return object;
            }
            String key = flowToken(text, position, ':');
            if (position[0] < text.length() && text.charAt(position[0]) == ':') {
                position[0]++;
            }
            object.add(unquoteKey(key), flowValue(text, position));
            skip(text, position);
            if (position[0] < text.length() && text.charAt(position[0]) == ',') {
                position[0]++;
            }
        }
    }

    private static JsonElement flowValue(String text, int[] position) {
        skip(text, position);
        char c = text.charAt(position[0]);
        if (c == '[' || c == '{') {
            return flow(text, position);
        }
        return plain(flowToken(text, position, ','));
    }

    private static String flowToken(String text, int[] position, char stop) {
        skip(text, position);
        int start = position[0];
        char quote = 0;
        while (position[0] < text.length()) {
            char c = text.charAt(position[0]);
            if (quote != 0) {
                if (c == '\\' && quote == '"') {
                    position[0]++;
                } else if (c == quote) {
                    quote = 0;
                }
            } else if (c == '"' || c == '\'') {
                quote = c;
            } else if (c == stop || c == ']' || c == '}' || stop == ':' && c == ',') {
                break;
            }
            position[0]++;
        }
        return text.substring(start, Math.min(position[0], text.length())).trim();
    }

    private static void skip(String text, int[] position) {
        while (position[0] < text.length() && Character.isWhitespace(text.charAt(position[0]))) {
            position[0]++;
        }
    }

    private static void emitMap(JsonObject object, int indent, StringBuilder out) {
        String pad = " ".repeat(indent);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = key(entry.getKey());
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                if (value.getAsJsonObject().size() == 0) {
                    out.append(pad).append(key).append(": {}\n");
                } else {
                    out.append(pad).append(key).append(":\n");
                    emitMap(value.getAsJsonObject(), indent + 2, out);
                }
            } else if (value.isJsonArray()) {
                if (value.getAsJsonArray().size() == 0) {
                    out.append(pad).append(key).append(": []\n");
                } else {
                    out.append(pad).append(key).append(":\n");
                    emitList(value.getAsJsonArray(), indent + 2, out);
                }
            } else {
                out.append(pad).append(key).append(": ").append(primitive(value)).append('\n');
            }
        }
    }

    private static void emitList(JsonArray array, int indent, StringBuilder out) {
        String pad = " ".repeat(indent);
        for (JsonElement element : array) {
            if (element.isJsonObject()) {
                out.append(pad).append("-\n");
                emitMap(element.getAsJsonObject(), indent + 2, out);
            } else if (element.isJsonArray()) {
                out.append(pad).append("- ").append(inline(element)).append('\n');
            } else {
                out.append(pad).append("- ").append(primitive(element)).append('\n');
            }
        }
    }

    private static String inline(JsonElement element) {
        if (element.isJsonArray()) {
            StringBuilder text = new StringBuilder("[");
            boolean first = true;
            for (JsonElement entry : element.getAsJsonArray()) {
                if (!first) {
                    text.append(", ");
                }
                first = false;
                text.append(inline(entry));
            }
            return text.append(']').toString();
        }
        return primitive(element);
    }

    private static String primitive(JsonElement element) {
        if (element.isJsonNull()) {
            return "null";
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isString()) {
            return "'" + primitive.getAsString().replace("'", "''") + "'";
        }
        return primitive.getAsString();
    }

    private static String key(String key) {
        if (PLAIN_KEY.matcher(key).matches() && !key.equals("true") && !key.equals("false") && !key.equals("null")
                && !INTEGER.matcher(key).matches()) {
            return key;
        }
        return "'" + key.replace("'", "''") + "'";
    }
}
