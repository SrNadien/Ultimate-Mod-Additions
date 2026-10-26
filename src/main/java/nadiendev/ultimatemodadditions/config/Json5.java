package nadiendev.ultimatemodadditions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import nadiendev.ultimatemodadditions.UMA;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Json5 {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Json5() {
    }

    public static JsonObject readObject(Path path) {
        try {
            return parse(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException exception) {
            UMA.LOGGER.error("Could not read {}: {}", path, exception.getMessage());
            return null;
        }
    }

    public static JsonObject parse(String text) {
        JsonReader reader = new JsonReader(new StringReader(stripTrailingCommas(text)));
        reader.setLenient(true);
        JsonElement element = JsonParser.parseReader(reader);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("the file must hold a JSON5 object");
        }
        return element.getAsJsonObject();
    }

    public static void write(Path path, String header, JsonObject object) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, header + PRETTY.toJson(object) + "\n", StandardCharsets.UTF_8);
        } catch (IOException exception) {
            UMA.LOGGER.error("Could not write {}: {}", path, exception.getMessage());
        }
    }

    public static String toString(JsonElement element) {
        return PRETTY.toJson(element);
    }

    private static String stripTrailingCommas(String text) {
        StringBuilder out = new StringBuilder(text.length());
        char quote = 0;
        boolean lineComment = false;
        boolean blockComment = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (lineComment) {
                out.append(c);
                lineComment = c != '\n';
                continue;
            }
            if (blockComment) {
                out.append(c);
                if (c == '*' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                    out.append('/');
                    i++;
                    blockComment = false;
                }
                continue;
            }
            if (quote != 0) {
                out.append(c);
                if (c == '\\' && i + 1 < text.length()) {
                    out.append(text.charAt(++i));
                } else if (c == quote) {
                    quote = 0;
                }
                continue;
            }
            if (c == '"' || c == '\'') {
                quote = c;
                out.append(c);
            } else if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                lineComment = true;
                out.append(c);
            } else if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                blockComment = true;
                out.append(c);
            } else if (c == ',' && closesNext(text, i + 1)) {
                continue;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean closesNext(String text, int from) {
        for (int i = from; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                int end = text.indexOf('\n', i);
                if (end < 0) {
                    return false;
                }
                i = end;
                continue;
            }
            if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                int end = text.indexOf("*/", i + 2);
                if (end < 0) {
                    return false;
                }
                i = end + 1;
                continue;
            }
            return c == '}' || c == ']';
        }
        return false;
    }
}
