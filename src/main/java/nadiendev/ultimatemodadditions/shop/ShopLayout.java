package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShopLayout {

    public record Def(String id, int order, String icon) {
    }

    public static final List<Def> ALL = List.of(
            new Def("coins", 0, "ultimatemodadditions:100_mangos"),
            new Def("stone", 1, "minecraft:stone_bricks"),
            new Def("wood", 2, "minecraft:oak_planks"),
            new Def("natural", 3, "minecraft:grass_block"),
            new Def("decorative_blocks", 4, "minecraft:glass"),
            new Def("crops", 5, "minecraft:wheat"),
            new Def("food", 6, "minecraft:cooked_beef"),
            new Def("resources", 7, "minecraft:diamond"),
            new Def("mob_drops", 8, "minecraft:bone"),
            new Def("weapons", 9, "minecraft:iron_sword"),
            new Def("armor", 10, "minecraft:iron_chestplate"),
            new Def("tools", 11, "minecraft:iron_pickaxe"),
            new Def("redstone", 12, "minecraft:redstone"),
            new Def("workstations", 13, "minecraft:crafting_table"),
            new Def("storage", 14, "minecraft:chest"),
            new Def("decoration", 15, "minecraft:painting"),
            new Def("dyes", 16, "minecraft:red_dye"),
            new Def("potions", 17, "minecraft:brewing_stand"),
            new Def("music", 18, "minecraft:jukebox"),
            new Def("misc", 19, "minecraft:name_tag"));

    public static final Set<String> IDS = ALL.stream().map(Def::id).collect(Collectors.toSet());

    private ShopLayout() {
    }

    public static Def def(String id) {
        for (Def def : ALL) {
            if (def.id().equals(id)) {
                return def;
            }
        }
        return null;
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(UMA.MODID, id);
    }
}
