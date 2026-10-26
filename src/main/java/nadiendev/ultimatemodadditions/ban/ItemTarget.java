package nadiendev.ultimatemodadditions.ban;

import nadiendev.ultimatemodadditions.event.ItemSpec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class ItemTarget {

    private boolean all;
    private String text = "";
    private final Set<String> ids = new HashSet<>();
    private final List<Pattern> regex = new ArrayList<>();
    private final List<CustomItem> customs = new ArrayList<>();
    private final List<ItemSpec> specs = new ArrayList<>();
    private final Set<String> excludedIds = new HashSet<>();
    private final List<Pattern> excludedRegex = new ArrayList<>();
    private final List<CustomItem> excludedCustoms = new ArrayList<>();

    public static ItemTarget exact(Item item) {
        ItemTarget target = new ItemTarget();
        target.text = BuiltInRegistries.ITEM.getKey(item).toString();
        target.ids.add(target.text);
        return target;
    }

    public static ItemTarget parse(String text, Map<String, CustomItem> customItems, HolderLookup.Provider registries) {
        ItemTarget target = new ItemTarget();
        target.text = text.trim();
        for (String token : Selector.split(text)) {
            boolean negate = token.startsWith("!");
            String value = negate ? token.substring(1).trim() : token;
            if (value.isEmpty()) {
                continue;
            }
            if (value.equals("*")) {
                target.all = !negate;
            } else if (value.startsWith("#")) {
                try {
                    (negate ? target.excludedRegex : target.regex).add(Pattern.compile(value.substring(1)));
                } catch (PatternSyntaxException ignored) {
                }
            } else if (customItems.containsKey(value.toLowerCase(Locale.ROOT))) {
                (negate ? target.excludedCustoms : target.customs).add(customItems.get(value.toLowerCase(Locale.ROOT)));
            } else if (value.indexOf('[') >= 0) {
                ItemSpec spec = ItemSpec.parse(value, registries);
                if (spec != null && !negate) {
                    target.specs.add(spec);
                }
            } else {
                (negate ? target.excludedIds : target.ids).add(Selector.normalize(value, false));
            }
        }
        return target;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean matches(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        boolean included = all || ids.contains(id);
        if (!included) {
            for (Pattern pattern : regex) {
                if (pattern.matcher(id).find()) {
                    included = true;
                    break;
                }
            }
        }
        if (!included) {
            for (CustomItem custom : customs) {
                if (custom.matches(stack)) {
                    included = true;
                    break;
                }
            }
        }
        if (!included) {
            for (ItemSpec spec : specs) {
                if (stack.is(spec.item()) && ComponentMatch.matches(spec.patch(), stack)) {
                    included = true;
                    break;
                }
            }
        }
        if (!included || excludedIds.contains(id)) {
            return false;
        }
        for (Pattern pattern : excludedRegex) {
            if (pattern.matcher(id).find()) {
                return false;
            }
        }
        for (CustomItem custom : excludedCustoms) {
            if (custom.matches(stack)) {
                return false;
            }
        }
        return true;
    }

    public Set<Item> exactItems() {
        if (all || !regex.isEmpty() || !customs.isEmpty() || !specs.isEmpty() || !excludedIds.isEmpty()
                || !excludedRegex.isEmpty() || !excludedCustoms.isEmpty()) {
            return null;
        }
        Set<Item> items = new HashSet<>();
        for (String id : ids) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null) {
                BuiltInRegistries.ITEM.getOptional(key).ifPresent(items::add);
            }
        }
        return items;
    }
}
