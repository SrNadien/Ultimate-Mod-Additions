package nadiendev.ultimatemodadditions.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public record ItemMatcher(List<ResourceLocation> items,
                          List<ResourceLocation> tags,
                          List<ResourceLocation> blocks,
                          List<ResourceLocation> blockTags,
                          List<String> regex,
                          List<ResourceLocation> exclude) {

    public static final ItemMatcher NONE = new ItemMatcher(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

    public static final Codec<ItemMatcher> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(ItemMatcher::items),
            ResourceLocation.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(ItemMatcher::tags),
            ResourceLocation.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(ItemMatcher::blocks),
            ResourceLocation.CODEC.listOf().optionalFieldOf("block_tags", List.of()).forGetter(ItemMatcher::blockTags),
            Codec.STRING.listOf().optionalFieldOf("regex", List.of()).forGetter(ItemMatcher::regex),
            ResourceLocation.CODEC.listOf().optionalFieldOf("exclude", List.of()).forGetter(ItemMatcher::exclude)
    ).apply(instance, ItemMatcher::new));

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (exclude.contains(id)) {
            return false;
        }
        if (items.contains(id)) {
            return true;
        }
        for (ResourceLocation tag : tags) {
            if (stack.is(TagKey.create(Registries.ITEM, tag))) {
                return true;
            }
        }
        return matchesRegex(id.toString());
    }

    public boolean matches(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (exclude.contains(id)) {
            return false;
        }
        if (blocks.contains(id)) {
            return true;
        }
        for (ResourceLocation tag : blockTags) {
            if (state.is(TagKey.create(Registries.BLOCK, tag))) {
                return true;
            }
        }
        if (!blocks.isEmpty() || !blockTags.isEmpty()) {
            return false;
        }
        return matchesRegex(id.toString());
    }

    private boolean matchesRegex(String id) {
        for (String pattern : regex) {
            try {
                if (Pattern.compile(pattern).matcher(id).find()) {
                    return true;
                }
            } catch (PatternSyntaxException ignored) {
            }
        }
        return false;
    }
}
