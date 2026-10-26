package nadiendev.ultimatemodadditions.ban;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import nadiendev.ultimatemodadditions.config.JsonValues;
import nadiendev.ultimatemodadditions.event.ItemSpec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class CustomItem {

    private static final Pattern COLOR_CODES = Pattern.compile("[&§][0-9a-fk-orA-FK-OR]");

    private record EnchantTest(String id, int min, int max) {
        boolean matches(String enchantId, int level) {
            boolean sameId = id.indexOf(':') >= 0 ? id.equals(enchantId) : enchantId.endsWith(":" + id);
            return sameId && level >= min && level <= max;
        }
    }

    private final ItemSpec base;
    private final String nameEquals;
    private final String nameContains;
    private final List<String> loreEquals;
    private final List<String> loreContains;
    private final Pattern loreRegex;
    private final int durabilityMin;
    private final int durabilityMax;
    private final List<EnchantTest> enchantContains;
    private final List<EnchantTest> enchantEquals;

    private CustomItem(ItemSpec base, JsonObject json) {
        this.base = base;
        this.nameEquals = JsonValues.string(json, "display_name_equals", "");
        this.nameContains = JsonValues.string(json, "display_name_contains", "");
        this.loreEquals = lines(json, "lore_equals");
        List<String> contains = lines(json, "lore_contains");
        contains.addAll(lines(json, "lore_line_contains"));
        this.loreContains = contains;
        Pattern regex = null;
        String text = JsonValues.string(json, "lore_contains_regex", "");
        if (!text.isEmpty()) {
            try {
                regex = Pattern.compile(text);
            } catch (PatternSyntaxException ignored) {
            }
        }
        this.loreRegex = regex;
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        JsonElement durability = json.get("durability");
        if (durability != null && durability.isJsonPrimitive()) {
            String value = durability.getAsString().trim();
            try {
                int dash = value.indexOf('-', 1);
                if (dash > 0) {
                    min = Integer.parseInt(value.substring(0, dash).trim());
                    max = Integer.parseInt(value.substring(dash + 1).trim());
                } else {
                    min = Integer.parseInt(value);
                    max = min;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        this.durabilityMin = min;
        this.durabilityMax = max;
        this.enchantContains = enchants(json, "enchantment_contains");
        this.enchantEquals = enchants(json, "enchantment_equals");
    }

    public static Map<String, CustomItem> parseAll(JsonObject root, HolderLookup.Provider registries) {
        Map<String, CustomItem> items = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            JsonObject json = entry.getValue().getAsJsonObject();
            ItemSpec base = ItemSpec.parse(JsonValues.string(json, "item", JsonValues.string(json, "material", "")), registries);
            if (base != null) {
                items.put(entry.getKey().trim().toLowerCase(Locale.ROOT), new CustomItem(base, json));
            }
        }
        return items;
    }

    private static List<String> lines(JsonObject json, String key) {
        List<String> lines = new ArrayList<>();
        JsonElement element = json.get(key);
        if (element == null) {
            return lines;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(entry -> lines.add(strip(entry.getAsString())));
        } else if (element.isJsonPrimitive()) {
            lines.add(strip(element.getAsString()));
        }
        return lines;
    }

    private static List<EnchantTest> enchants(JsonObject json, String key) {
        List<EnchantTest> tests = new ArrayList<>();
        for (String raw : lines(json, key)) {
            for (String token : Selector.split(raw)) {
                String[] parts = token.toLowerCase(Locale.ROOT).split(":");
                try {
                    String id;
                    int index;
                    if (parts.length > 1 && !isNumber(parts[1])) {
                        id = parts[0] + ":" + parts[1];
                        index = 2;
                    } else {
                        id = parts[0];
                        index = 1;
                    }
                    int min = 1;
                    int max = Integer.MAX_VALUE;
                    if (parts.length > index) {
                        min = Integer.parseInt(parts[index]);
                        max = parts.length > index + 1 ? Integer.parseInt(parts[index + 1]) : min;
                    }
                    tests.add(new EnchantTest(id, min, max));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return tests;
    }

    private static boolean isNumber(String text) {
        return !text.isEmpty() && text.chars().allMatch(Character::isDigit);
    }

    private static String strip(String text) {
        return COLOR_CODES.matcher(text).replaceAll("");
    }

    public boolean matches(ItemStack stack) {
        if (!stack.is(base.item())) {
            return false;
        }
        String name = strip(stack.getHoverName().getString());
        if (!nameEquals.isEmpty() && !name.equals(nameEquals)) {
            return false;
        }
        if (!nameContains.isEmpty() && !name.toLowerCase(Locale.ROOT).contains(nameContains.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (!loreEquals.isEmpty() || !loreContains.isEmpty() || loreRegex != null) {
            ItemLore lore = stack.get(DataComponents.LORE);
            List<String> lines = new ArrayList<>();
            if (lore != null) {
                for (Component line : lore.lines()) {
                    lines.add(strip(line.getString()));
                }
            }
            if (!loreEquals.isEmpty() && !lines.equals(loreEquals)) {
                return false;
            }
            if (!loreContains.isEmpty() && loreContains.stream().noneMatch(part -> lines.stream().anyMatch(line -> line.contains(part)))) {
                return false;
            }
            if (loreRegex != null && lines.stream().noneMatch(line -> loreRegex.matcher(line).find())) {
                return false;
            }
        }
        if (durabilityMin != Integer.MIN_VALUE || durabilityMax != Integer.MAX_VALUE) {
            int damage = stack.getDamageValue();
            if (damage < durabilityMin || damage > durabilityMax) {
                return false;
            }
        }
        if (!enchantContains.isEmpty() || !enchantEquals.isEmpty()) {
            Map<String, Integer> found = enchantments(stack);
            if (!enchantContains.isEmpty() && enchantContains.stream().noneMatch(test ->
                    found.entrySet().stream().anyMatch(entry -> test.matches(entry.getKey(), entry.getValue())))) {
                return false;
            }
            if (!enchantEquals.isEmpty() && (found.size() != enchantEquals.size() || enchantEquals.stream().anyMatch(test ->
                    found.entrySet().stream().noneMatch(entry -> test.matches(entry.getKey(), entry.getValue()))))) {
                return false;
            }
        }
        return base.patch().isEmpty() || ComponentMatch.matches(base.patch(), stack);
    }

    private static Map<String, Integer> enchantments(ItemStack stack) {
        Map<String, Integer> found = new HashMap<>();
        for (ItemEnchantments enchantments : new ItemEnchantments[]{stack.get(DataComponents.ENCHANTMENTS),
                stack.get(DataComponents.STORED_ENCHANTMENTS)}) {
            if (enchantments == null) {
                continue;
            }
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                entry.getKey().unwrapKey().ifPresent(key -> found.put(key.location().toString(), entry.getIntValue()));
            }
        }
        return found;
    }
}
