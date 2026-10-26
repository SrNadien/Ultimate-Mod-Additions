package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.data.ShopCategory;
import nadiendev.ultimatemodadditions.data.ShopItem;
import nadiendev.ultimatemodadditions.shop.ModShops;
import nadiendev.ultimatemodadditions.shop.ShopLayout;
import nadiendev.ultimatemodadditions.shop.ShopSections;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class UMAShopProvider extends UMAJsonProvider {

    private static final String[] BASICS = {
            "minecraft:oak_log 6.0 1.0",
            "minecraft:spruce_log 6.0 1.0",
            "minecraft:birch_log 6.0 1.0",
            "minecraft:jungle_log 7.0 1.0",
            "minecraft:acacia_log 7.0 1.0",
            "minecraft:dark_oak_log 7.0 1.0",
            "minecraft:cherry_log 8.0 1.0",
            "minecraft:mangrove_log 8.0 1.0",
            "minecraft:cobblestone 3.0 0.5",
            "minecraft:stone 4.0 1.0",
            "minecraft:dirt 1.0 0.2",
            "minecraft:sand 3.0 0.5",
            "minecraft:gravel 3.0 0.5",
            "minecraft:coal 16.0 4.0",
            "minecraft:charcoal 12.0 3.0",
            "minecraft:stick 1.0 0.2",
            "minecraft:string 8.0 2.0",
            "minecraft:feather 5.0 1.0",
            "minecraft:flint 5.0 1.0",
            "minecraft:leather 14.0 4.0",
            "minecraft:bone 8.0 2.0",
            "minecraft:clay_ball 4.0 1.0",
            "minecraft:snowball 2.0 0.5",
            "minecraft:paper 8.0 2.0",
            "minecraft:sugar_cane 5.0 1.0",
            "minecraft:cactus 4.0 1.0",
            "minecraft:bamboo 3.0 0.5",
            "minecraft:kelp 3.0 0.5",
            "minecraft:wheat_seeds 4.0 0.5",
            "minecraft:pumpkin_seeds 6.0 1.0",
            "minecraft:melon_seeds 6.0 1.0",
            "minecraft:beetroot_seeds 5.0 0.5"
    };

    private static final String[] BLOCKS = {
            "minecraft:oak_planks 2.0 0.5",
            "minecraft:spruce_planks 2.0 0.5",
            "minecraft:birch_planks 2.0 0.5",
            "minecraft:jungle_planks 2.0 0.5",
            "minecraft:acacia_planks 2.0 0.5",
            "minecraft:dark_oak_planks 2.0 0.5",
            "minecraft:stone_bricks 6.0 1.0",
            "minecraft:bricks 24.0 4.0",
            "minecraft:sandstone 8.0 1.0",
            "minecraft:red_sandstone 8.0 1.0",
            "minecraft:glass 8.0 1.5",
            "minecraft:white_wool 24.0 4.0",
            "minecraft:terracotta 16.0 3.0",
            "minecraft:deepslate 5.0 1.0",
            "minecraft:cobbled_deepslate 5.0 1.0",
            "minecraft:andesite 5.0 1.0",
            "minecraft:diorite 5.0 1.0",
            "minecraft:granite 5.0 1.0",
            "minecraft:calcite 8.0 1.0",
            "minecraft:tuff 5.0 1.0",
            "minecraft:smooth_stone 8.0 1.5",
            "minecraft:quartz_block 140.0 18.0",
            "minecraft:prismarine 60.0 8.0",
            "minecraft:netherrack 2.0 0.3",
            "minecraft:nether_bricks 20.0 3.0",
            "minecraft:end_stone 14.0 2.0",
            "minecraft:purpur_block 40.0 5.0",
            "minecraft:obsidian 220.0 25.0",
            "minecraft:glowstone 110.0 14.0",
            "minecraft:sea_lantern 160.0 20.0",
            "minecraft:bookshelf 160.0 20.0",
            "minecraft:hay_block 70.0 8.0",
            "minecraft:crying_obsidian 250.0 30.0"
    };

    private static final String[] COINS = {
            "ultimatemodadditions:5_centavos 0.05 0.05 free",
            "ultimatemodadditions:10_centavos 0.1 0.1 free",
            "ultimatemodadditions:25_centavos 0.25 0.25 free",
            "ultimatemodadditions:50_centavos 0.5 0.5 free",
            "ultimatemodadditions:2_pe 2.0 2.0 free",
            "ultimatemodadditions:5_pe 5.0 5.0 free",
            "ultimatemodadditions:10_pe 10.0 10.0 free",
            "ultimatemodadditions:2_mangos 2.0 2.0 free",
            "ultimatemodadditions:5_mangos 5.0 5.0 free",
            "ultimatemodadditions:10_mangos 10.0 10.0 free",
            "ultimatemodadditions:20_mangos 20.0 20.0 free",
            "ultimatemodadditions:50_mangos 50.0 50.0 free",
            "ultimatemodadditions:100_mangos 100.0 100.0 free",
            "ultimatemodadditions:1_lucas 1000.0 1000.0 free",
            "ultimatemodadditions:2_lucas 2000.0 2000.0 free",
            "ultimatemodadditions:10_lucas 10000.0 10000.0 free",
            "ultimatemodadditions:20_lucas 20000.0 20000.0 free"
    };

    private static final String[] COMBAT = {
            "minecraft:arrow 4.0 0.5",
            "minecraft:bow 150.0 15.0",
            "minecraft:crossbow 260.0 26.0",
            "minecraft:shield 180.0 18.0",
            "minecraft:iron_sword 350.0 35.0",
            "minecraft:iron_axe 380.0 38.0",
            "minecraft:iron_pickaxe 400.0 40.0",
            "minecraft:iron_shovel 150.0 15.0",
            "minecraft:iron_hoe 250.0 25.0",
            "minecraft:iron_helmet 500.0 50.0",
            "minecraft:iron_chestplate 800.0 80.0",
            "minecraft:iron_leggings 700.0 70.0",
            "minecraft:iron_boots 400.0 40.0",
            "minecraft:diamond_sword 2600.0 260.0",
            "minecraft:diamond_pickaxe 3800.0 380.0",
            "minecraft:diamond_axe 3800.0 380.0",
            "minecraft:diamond_shovel 1400.0 140.0",
            "minecraft:diamond_helmet 6000.0 600.0",
            "minecraft:diamond_chestplate 9600.0 960.0",
            "minecraft:diamond_leggings 8400.0 840.0",
            "minecraft:diamond_boots 4800.0 480.0",
            "minecraft:trident 5000.0 500.0",
            "minecraft:totem_of_undying 7000.0 700.0",
            "minecraft:elytra 12000.0 1200.0",
            "minecraft:saddle 700.0 70.0",
            "minecraft:name_tag 500.0 50.0",
            "minecraft:experience_bottle 220.0 22.0",
            "minecraft:golden_helmet 1100.0 110.0",
            "minecraft:chainmail_chestplate 900.0 90.0",
            "minecraft:spectral_arrow 20.0 2.0",
            "minecraft:tipped_arrow 90.0 9.0",
            "minecraft:lead 60.0 6.0",
            "minecraft:netherite_helmet 24000.0 2400.0",
            "minecraft:netherite_chestplate 27600.0 2760.0",
            "minecraft:netherite_leggings 26400.0 2640.0",
            "minecraft:netherite_boots 22800.0 2280.0",
            "minecraft:netherite_sword 20600.0 2060.0",
            "minecraft:netherite_pickaxe 21800.0 2180.0",
            "minecraft:netherite_axe 21800.0 2180.0",
            "minecraft:netherite_shovel 19400.0 1940.0",
            "minecraft:netherite_hoe 20400.0 2040.0"
    };

    private static final String[] DECORATION = {
            "minecraft:torch 5.0 1.0",
            "minecraft:lantern 80.0 8.0",
            "minecraft:soul_lantern 90.0 9.0",
            "minecraft:flower_pot 20.0 2.0",
            "minecraft:painting 40.0 4.0",
            "minecraft:item_frame 50.0 5.0",
            "minecraft:glow_item_frame 90.0 9.0",
            "minecraft:white_bed 150.0 15.0",
            "minecraft:candle 35.0 4.0",
            "minecraft:oak_sign 20.0 2.0",
            "minecraft:ladder 10.0 1.0",
            "minecraft:jukebox 600.0 60.0",
            "minecraft:note_block 160.0 16.0",
            "minecraft:lectern 120.0 12.0",
            "minecraft:barrel 40.0 5.0",
            "minecraft:chest 40.0 6.0",
            "minecraft:ender_chest 1500.0 150.0",
            "minecraft:white_carpet 8.0 1.0",
            "minecraft:dandelion 4.0 0.5",
            "minecraft:poppy 4.0 0.5",
            "minecraft:blue_orchid 6.0 1.0",
            "minecraft:allium 6.0 1.0",
            "minecraft:azure_bluet 6.0 1.0",
            "minecraft:rose_bush 8.0 1.0",
            "minecraft:peony 8.0 1.0",
            "minecraft:sunflower 8.0 1.0",
            "minecraft:lily_of_the_valley 6.0 1.0",
            "minecraft:cornflower 6.0 1.0",
            "minecraft:oak_sapling 12.0 1.0",
            "minecraft:spruce_sapling 12.0 1.0",
            "minecraft:birch_sapling 12.0 1.0",
            "minecraft:fern 3.0 0.5"
    };

    private static final String[] FOOD = {
            "minecraft:apple 30.0 2.0",
            "minecraft:bread 40.0 3.0",
            "minecraft:cooked_beef 60.0 6.0",
            "minecraft:cooked_porkchop 60.0 6.0",
            "minecraft:cooked_chicken 50.0 5.0",
            "minecraft:cooked_mutton 55.0 5.0",
            "minecraft:cooked_rabbit 50.0 5.0",
            "minecraft:cooked_cod 40.0 4.0",
            "minecraft:cooked_salmon 45.0 4.0",
            "minecraft:baked_potato 25.0 3.0",
            "minecraft:carrot 12.0 1.0",
            "minecraft:potato 10.0 1.0",
            "minecraft:beetroot 10.0 1.0",
            "minecraft:melon_slice 6.0 0.5",
            "minecraft:sweet_berries 8.0 1.0",
            "minecraft:glow_berries 10.0 1.0",
            "minecraft:cookie 15.0 1.0",
            "minecraft:pumpkin_pie 90.0 9.0",
            "minecraft:cake 200.0 19.0",
            "minecraft:golden_apple 1500.0 100.0",
            "minecraft:golden_carrot 300.0 20.0",
            "minecraft:mushroom_stew 70.0 6.0",
            "minecraft:rabbit_stew 110.0 10.0",
            "minecraft:honey_bottle 80.0 6.0",
            "minecraft:milk_bucket 60.0 4.0",
            "minecraft:egg 8.0 1.0",
            "minecraft:wheat 8.0 1.0",
            "minecraft:sugar 6.0 1.0",
            "minecraft:cocoa_beans 10.0 1.0",
            "minecraft:bone_meal 6.0 1.0",
            "minecraft:beef 25.0 2.0",
            "minecraft:porkchop 25.0 2.0",
            "minecraft:chicken 20.0 2.0",
            "minecraft:mutton 22.0 2.0",
            "minecraft:cod 14.0 1.0",
            "minecraft:salmon 16.0 1.0"
    };

    private static final String[] MATERIALS = {
            "minecraft:copper_ingot 40.0 5.0",
            "minecraft:iron_ingot 100.0 12.0",
            "minecraft:gold_ingot 250.0 30.0",
            "minecraft:diamond 1200.0 150.0",
            "minecraft:emerald 600.0 70.0",
            "minecraft:lapis_lazuli 30.0 4.0",
            "minecraft:redstone 25.0 3.0",
            "minecraft:quartz 40.0 5.0",
            "minecraft:netherite_scrap 4000.0 400.0",
            "minecraft:netherite_ingot 18000.0 2000.0",
            "minecraft:amethyst_shard 30.0 4.0",
            "minecraft:ender_pearl 400.0 40.0",
            "minecraft:blaze_rod 150.0 15.0",
            "minecraft:slime_ball 60.0 6.0",
            "minecraft:gunpowder 50.0 5.0",
            "minecraft:glowstone_dust 30.0 4.0",
            "minecraft:nether_wart 25.0 3.0",
            "minecraft:ghast_tear 500.0 60.0",
            "minecraft:prismarine_shard 40.0 4.0",
            "minecraft:prismarine_crystals 50.0 5.0",
            "minecraft:shulker_shell 1500.0 200.0",
            "minecraft:phantom_membrane 120.0 12.0",
            "minecraft:nautilus_shell 800.0 90.0",
            "minecraft:heart_of_the_sea 6000.0 600.0",
            "minecraft:echo_shard 900.0 100.0",
            "minecraft:raw_iron 70.0 8.0",
            "minecraft:raw_copper 25.0 3.0",
            "minecraft:raw_gold 180.0 20.0",
            "minecraft:iron_nugget 12.0 1.0",
            "minecraft:gold_nugget 28.0 3.0",
            "minecraft:netherite_upgrade_smithing_template 8000.0 800.0",
            "minecraft:rotten_flesh 4.0 0.5",
            "minecraft:spider_eye 12.0 1.5",
            "minecraft:fermented_spider_eye 25.0 3.0",
            "minecraft:ender_eye 500.0 50.0",
            "minecraft:blaze_powder 75.0 8.0",
            "minecraft:magma_cream 80.0 8.0",
            "minecraft:ink_sac 20.0 2.0",
            "minecraft:glow_ink_sac 60.0 6.0",
            "minecraft:rabbit_foot 150.0 15.0",
            "minecraft:rabbit_hide 12.0 1.0",
            "minecraft:skeleton_skull 300.0 30.0",
            "minecraft:zombie_head 300.0 30.0",
            "minecraft:creeper_head 300.0 30.0",
            "minecraft:piglin_head 400.0 40.0",
            "minecraft:wither_skeleton_skull 8000.0 900.0",
            "minecraft:nether_star 15000.0 1800.0",
            "minecraft:dragon_breath 1500.0 150.0",
            "minecraft:turtle_scute 90.0 9.0",
            "minecraft:armadillo_scute 80.0 8.0",
            "minecraft:breeze_rod 300.0 30.0",
            "minecraft:wind_charge 60.0 6.0",
            "minecraft:goat_horn 400.0 40.0",
            "minecraft:honeycomb 30.0 3.0",
            "minecraft:cobweb 50.0 5.0",
            "minecraft:sponge 100.0 12.0",
            "minecraft:tropical_fish 15.0 1.5",
            "minecraft:pufferfish 25.0 2.5",
            "minecraft:totem_of_undying 7000.0 700.0",
            "minecraft:trident 5000.0 500.0",
            "minecraft:experience_bottle 220.0 22.0",
            "minecraft:egg 8.0 1.0"
    };

    private static final String[] ORES = {
            "minecraft:coal_ore 40.0 6.0",
            "minecraft:deepslate_coal_ore 45.0 7.0",
            "minecraft:iron_ore 120.0 15.0",
            "minecraft:deepslate_iron_ore 130.0 16.0",
            "minecraft:copper_ore 60.0 8.0",
            "minecraft:deepslate_copper_ore 65.0 9.0",
            "minecraft:gold_ore 300.0 38.0",
            "minecraft:deepslate_gold_ore 320.0 40.0",
            "minecraft:redstone_ore 70.0 9.0",
            "minecraft:deepslate_redstone_ore 75.0 10.0",
            "minecraft:lapis_ore 90.0 12.0",
            "minecraft:deepslate_lapis_ore 95.0 12.0",
            "minecraft:diamond_ore 1500.0 190.0",
            "minecraft:deepslate_diamond_ore 1600.0 200.0",
            "minecraft:emerald_ore 800.0 100.0",
            "minecraft:deepslate_emerald_ore 850.0 105.0",
            "minecraft:nether_gold_ore 80.0 10.0",
            "minecraft:nether_quartz_ore 70.0 9.0",
            "minecraft:ancient_debris 6000.0 700.0",
            "minecraft:raw_iron_block 650.0 75.0",
            "minecraft:raw_copper_block 230.0 28.0",
            "minecraft:raw_gold_block 1650.0 190.0",
            "minecraft:coal_block 144.0 36.0",
            "minecraft:iron_block 900.0 108.0",
            "minecraft:copper_block 360.0 45.0",
            "minecraft:gold_block 2250.0 270.0",
            "minecraft:redstone_block 225.0 27.0",
            "minecraft:lapis_block 270.0 36.0",
            "minecraft:emerald_block 5400.0 630.0",
            "minecraft:diamond_block 10800.0 1350.0",
            "minecraft:amethyst_block 120.0 16.0",
            "minecraft:netherite_block 162000.0 18000.0"
    };

    private static final String[] TECHNOLOGY = {
            "minecraft:redstone_torch 30.0 3.0",
            "minecraft:repeater 120.0 12.0",
            "minecraft:comparator 160.0 16.0",
            "minecraft:piston 150.0 15.0",
            "minecraft:sticky_piston 200.0 20.0",
            "minecraft:observer 180.0 18.0",
            "minecraft:hopper 260.0 26.0",
            "minecraft:dropper 90.0 9.0",
            "minecraft:dispenser 110.0 11.0",
            "minecraft:tnt 300.0 30.0",
            "minecraft:rail 40.0 4.0",
            "minecraft:powered_rail 200.0 20.0",
            "minecraft:detector_rail 90.0 9.0",
            "minecraft:activator_rail 90.0 9.0",
            "minecraft:daylight_detector 220.0 22.0",
            "minecraft:lever 12.0 1.0",
            "minecraft:stone_button 6.0 0.5",
            "minecraft:redstone_lamp 220.0 22.0",
            "minecraft:bucket 300.0 30.0",
            "minecraft:water_bucket 320.0 30.0",
            "minecraft:lava_bucket 380.0 35.0",
            "minecraft:shears 150.0 15.0",
            "minecraft:compass 200.0 20.0",
            "minecraft:clock 300.0 30.0",
            "minecraft:minecart 350.0 35.0",
            "minecraft:target 150.0 15.0",
            "minecraft:lightning_rod 130.0 13.0",
            "minecraft:slime_block 540.0 54.0",
            "minecraft:honey_block 330.0 33.0",
            "minecraft:iron_door 220.0 22.0",
            "minecraft:anvil 600.0 60.0",
            "minecraft:fishing_rod 80.0 8.0",
            "minecraft:redstone 25.0 3.0",
            "minecraft:redstone_block 225.0 27.0",
            "minecraft:oak_button 4.0 0.5",
            "minecraft:polished_blackstone_button 7.0 0.7",
            "minecraft:stone_pressure_plate 8.0 1.0",
            "minecraft:oak_pressure_plate 6.0 0.7",
            "minecraft:polished_blackstone_pressure_plate 9.0 1.0",
            "minecraft:light_weighted_pressure_plate 60.0 6.0",
            "minecraft:heavy_weighted_pressure_plate 90.0 10.0",
            "minecraft:tripwire_hook 30.0 3.0",
            "minecraft:sculk_sensor 400.0 40.0",
            "minecraft:calibrated_sculk_sensor 500.0 50.0",
            "minecraft:note_block 160.0 16.0",
            "minecraft:jukebox 600.0 60.0",
            "minecraft:tnt_minecart 650.0 65.0",
            "minecraft:hopper_minecart 500.0 50.0",
            "minecraft:chest_minecart 400.0 40.0",
            "minecraft:furnace_minecart 380.0 38.0",
            "minecraft:iron_trapdoor 110.0 11.0",
            "minecraft:trapped_chest 90.0 10.0",
            "minecraft:copper_bulb 250.0 25.0",
            "minecraft:copper_door 140.0 14.0",
            "minecraft:copper_trapdoor 100.0 10.0",
            "minecraft:bell 700.0 70.0",
            "minecraft:crafter 500.0 50.0"
    };

    private static final String[] WORKSTATIONS = {
            "minecraft:crafting_table 30.0 4.0",
            "minecraft:furnace 40.0 5.0",
            "minecraft:blast_furnace 300.0 30.0",
            "minecraft:smoker 120.0 12.0",
            "minecraft:smithing_table 120.0 12.0",
            "minecraft:cartography_table 80.0 8.0",
            "minecraft:fletching_table 70.0 7.0",
            "minecraft:loom 60.0 6.0",
            "minecraft:stonecutter 80.0 8.0",
            "minecraft:grindstone 90.0 9.0",
            "minecraft:anvil 600.0 60.0",
            "minecraft:enchanting_table 2500.0 250.0",
            "minecraft:brewing_stand 400.0 40.0",
            "minecraft:cauldron 300.0 30.0",
            "minecraft:composter 40.0 4.0",
            "minecraft:lectern 120.0 12.0",
            "minecraft:barrel 40.0 5.0",
            "minecraft:campfire 60.0 6.0",
            "minecraft:soul_campfire 80.0 8.0",
            "minecraft:beacon 9000.0 900.0",
            "minecraft:respawn_anchor 2000.0 200.0",
            "minecraft:lodestone 3000.0 300.0",
            "minecraft:bell 700.0 70.0",
            "minecraft:crafter 500.0 50.0",
            "minecraft:chest 40.0 6.0",
            "minecraft:trapped_chest 90.0 10.0",
            "minecraft:ender_chest 1500.0 150.0",
            "minecraft:shulker_box 2000.0 250.0",
            "minecraft:bookshelf 160.0 20.0",
            "minecraft:jukebox 600.0 60.0",
            "minecraft:conduit 4000.0 400.0",
            "minecraft:end_crystal 2500.0 250.0"
    };

    public UMAShopProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, "uma/shop", "Shop zones");
    }

    private record Group(String file, String[] rows) {
    }

    @Override
    void generate(Emitter emitter) {
        List<Group> groups = List.of(
                new Group("coins", COINS),
                new Group("basics", BASICS),
                new Group("blocks", BLOCKS),
                new Group("food", FOOD),
                new Group("materials", MATERIALS),
                new Group("ores", ORES),
                new Group("workstations", WORKSTATIONS),
                new Group("decoration", DECORATION),
                new Group("technology", TECHNOLOGY),
                new Group("combat", COMBAT));
        Set<String> placed = new HashSet<>();
        Map<String, List<String>> byCategory = new LinkedHashMap<>();
        for (ShopLayout.Def def : ShopLayout.ALL) {
            byCategory.put(def.id(), new ArrayList<>());
        }
        for (Group group : groups) {
            for (String row : group.rows()) {
                String id = row.split(" ")[0];
                if (!placed.add(id)) {
                    continue;
                }
                String path = id.substring(id.indexOf(':') + 1);
                String category = group.file().equals("coins") ? "coins" : ShopSections.of(path);
                if (category == null) {
                    category = ModShops.categoryOf(item(id));
                }
                byCategory.get(category).add(row);
            }
        }
        for (ShopLayout.Def def : ShopLayout.ALL) {
            List<String> rows = byCategory.get(def.id());
            if (!rows.isEmpty()) {
                category(emitter, def.id(), "shop.ultimatemodadditions.category." + def.id(), def.icon(), def.order(),
                        rows.toArray(new String[0]));
            }
        }
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    private static void category(Emitter emitter, String file, String name, String icon, int order, String[] rows) {
        List<ShopItem> items = new ArrayList<>();
        for (String row : rows) {
            String[] part = row.split(" ");
            items.add(new ShopItem(new ItemStack(item(part[0])), Double.parseDouble(part[1]),
                    Double.parseDouble(part[2]), part.length < 4));
        }
        emitter.accept(file, ShopCategory.CODEC, new ShopCategory(name, item(icon), order, items));
    }
}
