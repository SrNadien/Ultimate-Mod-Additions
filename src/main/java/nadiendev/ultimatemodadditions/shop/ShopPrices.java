package nadiendev.ultimatemodadditions.shop;

import java.util.List;
import java.util.Map;

final class ShopPrices {

    static final double DEFAULT_START = 5900.0D;
    static final double OP_START = 100000.0D;
    static final double OP_AUTO_FROM = 50000.0D;

    static final Map<String, Double> STARTS = Map.ofEntries(
            Map.entry("stone", 12700.0D),
            Map.entry("wood", 27900.0D),
            Map.entry("natural", 80000.0D),
            Map.entry("decorative_blocks", 40000.0D),
            Map.entry("crops", 10000.0D),
            Map.entry("food", 7000.0D),
            Map.entry("resources", 45000.0D),
            Map.entry("mob_drops", 9900.0D),
            Map.entry("weapons", 300000.0D),
            Map.entry("armor", 600000.0D),
            Map.entry("tools", 600000.0D),
            Map.entry("redstone", 1200000.0D),
            Map.entry("workstations", 1500.0D),
            Map.entry("storage", 7900.0D),
            Map.entry("decoration", 40000.0D),
            Map.entry("dyes", 900.0D),
            Map.entry("flowers", 4500.0D),
            Map.entry("potions", 99000.0D),
            Map.entry("music", 10000.0D),
            Map.entry("misc", 89000.0D),
            Map.entry("mod_ftbquests", 500000.0D));

    static final Map<String, Double> FIXED = Map.of(
            "minecraft:nether_star", 12000000.0D,
            "#minecraft:skulls", 2500000.0D);

    static final List<String> OP_ITEMS = List.of("minecraft:nether_star", "minecraft:beacon", "minecraft:dragon_egg",
            "minecraft:elytra", "minecraft:totem_of_undying", "minecraft:enchanted_golden_apple",
            "minecraft:netherite_block", "minecraft:netherite_ingot", "minecraft:heart_of_the_sea", "minecraft:conduit",
            "minecraft:trident");

    private ShopPrices() {
    }
}
