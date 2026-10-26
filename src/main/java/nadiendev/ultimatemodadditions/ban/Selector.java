package nadiendev.ultimatemodadditions.ban;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class Selector {

    private boolean all;
    private String text = "*";
    private final Set<String> include = new HashSet<>();
    private final Set<String> exclude = new HashSet<>();
    private final List<Pattern> includeRegex = new ArrayList<>();
    private final List<Pattern> excludeRegex = new ArrayList<>();

    public static Selector everything() {
        Selector selector = new Selector();
        selector.all = true;
        return selector;
    }

    public static Selector parse(String text, boolean dimensions) {
        Selector selector = new Selector();
        selector.text = text.trim();
        for (String token : split(text)) {
            boolean negate = token.startsWith("!");
            String value = negate ? token.substring(1).trim() : token;
            if (value.isEmpty()) {
                continue;
            }
            if (value.equals("*")) {
                selector.all = !negate;
            } else if (value.startsWith("#")) {
                try {
                    Pattern pattern = Pattern.compile(value.substring(1));
                    (negate ? selector.excludeRegex : selector.includeRegex).add(pattern);
                } catch (PatternSyntaxException ignored) {
                }
            } else {
                (negate ? selector.exclude : selector.include).add(normalize(value, dimensions));
            }
        }
        return selector;
    }

    public static List<String> split(String text) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (char c : text.toCharArray()) {
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth = Math.max(0, depth - 1);
            }
            if (c == ',' && depth == 0) {
                if (!current.toString().isBlank()) {
                    tokens.add(current.toString().trim());
                }
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (!current.toString().isBlank()) {
            tokens.add(current.toString().trim());
        }
        return tokens;
    }

    public static String normalize(String value, boolean dimensions) {
        String id = value.trim().toLowerCase(Locale.ROOT);
        if (dimensions) {
            switch (id) {
                case "overworld" -> {
                    return "minecraft:overworld";
                }
                case "nether", "the_nether" -> {
                    return "minecraft:the_nether";
                }
                case "end", "the_end" -> {
                    return "minecraft:the_end";
                }
                default -> {
                }
            }
        }
        return id.indexOf(':') < 0 ? "minecraft:" + id : id;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean isAll() {
        return all && exclude.isEmpty() && excludeRegex.isEmpty();
    }

    public boolean matches(String id) {
        boolean included = all || include.contains(id);
        if (!included) {
            for (Pattern pattern : includeRegex) {
                if (pattern.matcher(id).find()) {
                    included = true;
                    break;
                }
            }
        }
        if (!included || exclude.contains(id)) {
            return false;
        }
        for (Pattern pattern : excludeRegex) {
            if (pattern.matcher(id).find()) {
                return false;
            }
        }
        return true;
    }

    public Set<String> exactIds() {
        if (all || !includeRegex.isEmpty() || !exclude.isEmpty() || !excludeRegex.isEmpty()) {
            return null;
        }
        return include;
    }
}
