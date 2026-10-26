package nadiendev.ultimatemodadditions.event;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ItemSpec(Item item, DataComponentPatch patch, int count) {

    private static final Pattern COUNTED = Pattern.compile("^(\\d+)x\\s+(.+)$");

    public static ItemSpec parse(String text, HolderLookup.Provider registries) {
        String id = text.trim();
        int count = 1;
        Matcher matcher = COUNTED.matcher(id);
        if (matcher.matches()) {
            count = Math.max(1, Integer.parseInt(matcher.group(1)));
            id = matcher.group(2).trim();
        }
        if (id.indexOf('[') >= 0 && registries != null) {
            try {
                ItemParser.ItemResult result = new ItemParser(registries).parse(new StringReader(id));
                return new ItemSpec(result.item().value(), result.components(), count);
            } catch (CommandSyntaxException | RuntimeException exception) {
                return null;
            }
        }
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(key).orElse(null);
        return item == null ? null : new ItemSpec(item, DataComponentPatch.EMPTY, count);
    }

    public static Item item(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id.trim());
        return key == null ? null : BuiltInRegistries.ITEM.getOptional(key).orElse(null);
    }

    public ItemStack stack() {
        return stack(count);
    }

    public ItemStack stack(int amount) {
        ItemStack stack = new ItemStack(item, Math.max(1, amount));
        stack.applyComponents(patch);
        return stack;
    }
}
