package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class CreativeIndex {

    private static final Set<ResourceKey<CreativeModeTab>> CREATIVE_ONLY = Set.of(CreativeModeTabs.OP_BLOCKS,
            CreativeModeTabs.SPAWN_EGGS, CreativeModeTabs.HOTBAR, CreativeModeTabs.SEARCH);

    private final Map<Item, String> firstTab = new HashMap<>();
    private final Set<Item> creativeOnly = new HashSet<>();

    private CreativeIndex() {
    }

    static CreativeIndex build(MinecraftServer server) {
        CreativeIndex index = new CreativeIndex();
        try {
            CreativeModeTabs.tryRebuildTabContents(server.getWorldData().enabledFeatures(), true, server.registryAccess());
            Set<Item> listed = new HashSet<>();
            Set<Item> regular = new HashSet<>();
            for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
                if (tab.getType() != CreativeModeTab.Type.CATEGORY) {
                    continue;
                }
                ResourceKey<CreativeModeTab> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElse(null);
                if (key == null) {
                    continue;
                }
                boolean restricted = CREATIVE_ONLY.contains(key);
                for (ItemStack stack : tab.getDisplayItems()) {
                    Item item = stack.getItem();
                    listed.add(item);
                    if (restricted) {
                        continue;
                    }
                    regular.add(item);
                    index.firstTab.putIfAbsent(item, key.location().getPath());
                }
            }
            for (Item item : listed) {
                if (!regular.contains(item)) {
                    index.creativeOnly.add(item);
                }
            }
        } catch (RuntimeException exception) {
            UMA.LOGGER.error("Could not read the creative tabs to sort the shop", exception);
            index.firstTab.clear();
            index.creativeOnly.clear();
        }
        return index;
    }

    String tab(Item item) {
        return firstTab.getOrDefault(item, "");
    }

    boolean creativeOnly(Item item) {
        return creativeOnly.contains(item);
    }
}
