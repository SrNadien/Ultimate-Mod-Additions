package nadiendev.ultimatemodadditions.shop;

import java.util.HashMap;
import java.util.Map;

public final class ShopSections {

    private static final Map<String, String> SECTIONS = new HashMap<>();

    static {
        add("resources",
                "amethyst_block amethyst_cluster amethyst_shard ancient_debris coal coal_block coal_ore copper_block "
                + "copper_ingot copper_ore diamond diamond_block diamond_ore emerald emerald_block emerald_ore "
                + "gold_block gold_ingot gold_ore iron_block iron_ingot iron_ore lapis_block lapis_lazuli lapis_ore "
                + "nether_quartz_ore netherite_block netherite_ingot quartz quartz_block redstone redstone_block "
                + "redstone_ore resin_block resin_brick resin_clump");
        add("food",
                "apple baked_potato beef beetroot_soup bread cake chicken cod cooked_beef cooked_chicken cooked_cod "
                + "cooked_mutton cooked_porkchop cooked_rabbit cooked_salmon cookie glistering_melon_slice golden_apple "
                + "golden_carrot honey_bottle melon_slice mushroom_stew mutton porkchop pumpkin_pie rabbit rabbit_stew "
                + "salmon");
        add("weapons",
                "arrow bow copper_spear copper_sword crossbow diamond_spear diamond_sword golden_spear golden_sword "
                + "iron_spear iron_sword mace netherite_spear netherite_sword shield spectral_arrow stone_spear "
                + "stone_sword trident wooden_spear wooden_sword");
        add("armor",
                "bolt_armor_trim_smithing_template chainmail_boots chainmail_chestplate chainmail_helmet "
                + "chainmail_leggings coast_armor_trim_smithing_template copper_boots copper_chestplate copper_helmet "
                + "copper_horse_armor copper_leggings copper_nautilus_armor diamond_boots diamond_chestplate "
                + "diamond_helmet diamond_horse_armor diamond_leggings diamond_nautilus_armor "
                + "dune_armor_trim_smithing_template eye_armor_trim_smithing_template flow_armor_trim_smithing_template "
                + "golden_boots golden_chestplate golden_helmet golden_horse_armor golden_leggings "
                + "golden_nautilus_armor host_armor_trim_smithing_template iron_boots iron_chestplate iron_helmet "
                + "iron_horse_armor iron_leggings iron_nautilus_armor leather_boots leather_chestplate leather_helmet "
                + "leather_horse_armor leather_leggings netherite_boots netherite_chestplate netherite_helmet "
                + "netherite_horse_armor netherite_leggings netherite_nautilus_armor "
                + "netherite_upgrade_smithing_template raiser_armor_trim_smithing_template "
                + "rib_armor_trim_smithing_template sentry_armor_trim_smithing_template "
                + "shaper_armor_trim_smithing_template silence_armor_trim_smithing_template "
                + "snout_armor_trim_smithing_template spire_armor_trim_smithing_template "
                + "tide_armor_trim_smithing_template turtle_helmet vex_armor_trim_smithing_template "
                + "ward_armor_trim_smithing_template wayfinder_armor_trim_smithing_template "
                + "wild_armor_trim_smithing_template wolf_armor");
        add("tools",
                "copper_axe copper_hoe copper_pickaxe copper_shovel diamond_axe diamond_hoe diamond_pickaxe "
                + "diamond_shovel golden_axe golden_hoe golden_pickaxe golden_shovel iron_axe iron_hoe iron_pickaxe "
                + "iron_shovel netherite_axe netherite_hoe netherite_pickaxe netherite_shovel stone_axe stone_hoe "
                + "stone_pickaxe stone_shovel wooden_axe wooden_hoe wooden_pickaxe wooden_shovel");
        add("music",
                "disc_fragment goat_horn jukebox music_disc_11 music_disc_13 music_disc_5 music_disc_blocks "
                + "music_disc_cat music_disc_chirp music_disc_creator music_disc_creator_music_box music_disc_far "
                + "music_disc_mall music_disc_mellohi music_disc_otherside music_disc_pigstep music_disc_precipice "
                + "music_disc_relic music_disc_stal music_disc_strad music_disc_wait music_disc_ward note_block");
        add("dyes",
                "allium azure_bluet black_dye blue_dye blue_orchid brown_dye cornflower cyan_dye dandelion "
                + "glow_ink_sac gray_dye green_dye ink_sac light_blue_dye light_gray_dye lilac lily_of_the_valley "
                + "lily_pad lime_dye magenta_dye orange_dye orange_tulip oxeye_daisy peony pink_dye pink_petals "
                + "pink_tulip pitcher_plant poppy purple_dye red_dye red_tulip rose_bush sunflower torchflower "
                + "white_dye white_tulip wither_rose yellow_dye");
        add("crops",
                "bamboo bee_nest beehive beetroot beetroot_seeds bone_meal brown_mushroom cactus carrot chorus_flower "
                + "chorus_fruit cocoa_beans crimson_fungus dried_kelp dried_kelp_block egg glow_berries hay_block "
                + "honeycomb kelp melon melon_seeds nether_wart pitcher_pod potato pumpkin pumpkin_seeds red_mushroom "
                + "sugar sugar_cane sweet_berries torchflower_seeds warped_fungus wheat wheat_seeds");
        add("potions",
                "blaze_powder glass_bottle glowstone_dust lingering_potion potion splash_potion tipped_arrow");
        add("workstations",
                "anvil blast_furnace brewing_stand cartography_table composter crafting_table enchanting_table "
                + "fletching_table furnace grindstone lectern loom smithing_table smoker stonecutter");
        add("storage",
                "barrel black_bundle black_shulker_box blue_bundle blue_shulker_box brown_bundle brown_shulker_box "
                + "bundle cauldron chest chiseled_bookshelf copper_chest cyan_bundle cyan_shulker_box decorated_pot "
                + "ender_chest gray_bundle gray_shulker_box green_bundle green_shulker_box light_blue_bundle "
                + "light_blue_shulker_box light_gray_bundle light_gray_shulker_box lime_bundle lime_shulker_box "
                + "magenta_bundle magenta_shulker_box orange_bundle orange_shulker_box pink_bundle pink_shulker_box "
                + "purple_bundle purple_shulker_box red_bundle red_shulker_box white_bundle white_shulker_box "
                + "yellow_bundle yellow_shulker_box");
        add("redstone",
                "activator_rail calibrated_sculk_sensor cherry_button cherry_pressure_plate cherry_trapdoor "
                + "comparator crafter daylight_detector detector_rail dispenser dropper heavy_weighted_pressure_plate "
                + "hopper iron_door iron_trapdoor lever light_weighted_pressure_plate lightning_rod oak_button "
                + "oak_pressure_plate oak_trapdoor observer piston powered_rail rail redstone_lamp redstone_torch "
                + "repeater sculk_sensor sticky_piston stone_button stone_pressure_plate target tnt tripwire_hook");
        add("mob_drops",
                "armadillo_scute blaze_rod bone breeze_rod creeper_head ender_pearl feather fermented_spider_eye "
                + "ghast_tear gunpowder leather magma_cream phantom_membrane rabbit_foot rabbit_hide rotten_flesh scute "
                + "shulker_shell skeleton_skull slime_ball spider_eye string totem_of_undying wind_charge "
                + "wither_skeleton_skull zombie_head");
        add("wood",
                "acacia_fence acacia_fence_gate acacia_log acacia_planks acacia_slab acacia_stairs acacia_wood "
                + "bamboo_block bamboo_fence bamboo_fence_gate bamboo_mosaic bamboo_planks bamboo_slab bamboo_stairs "
                + "birch_fence birch_fence_gate birch_log birch_planks birch_slab birch_stairs birch_wood cherry_fence "
                + "cherry_fence_gate cherry_log cherry_planks cherry_slab cherry_stairs cherry_wood crimson_fence "
                + "crimson_fence_gate crimson_hyphae crimson_planks crimson_slab crimson_stairs crimson_stem "
                + "dark_oak_fence dark_oak_fence_gate dark_oak_log dark_oak_planks dark_oak_slab dark_oak_stairs "
                + "dark_oak_wood jungle_fence jungle_fence_gate jungle_log jungle_planks jungle_slab jungle_stairs "
                + "jungle_wood mangrove_fence mangrove_fence_gate mangrove_log mangrove_planks mangrove_slab "
                + "mangrove_stairs mangrove_wood oak_fence oak_fence_gate oak_log oak_planks oak_slab oak_stairs "
                + "oak_wood pale_oak_fence pale_oak_fence_gate pale_oak_log pale_oak_planks pale_oak_slab "
                + "pale_oak_stairs pale_oak_wood spruce_fence spruce_fence_gate spruce_log spruce_planks spruce_slab "
                + "spruce_stairs spruce_wood warped_fence warped_fence_gate warped_hyphae warped_planks warped_slab "
                + "warped_stairs warped_stem");
        add("stone",
                "andesite andesite_slab andesite_stairs basalt blackstone blackstone_slab blackstone_stairs "
                + "brick_slab brick_stairs bricks calcite chiseled_deepslate chiseled_nether_bricks "
                + "chiseled_polished_blackstone chiseled_stone_bricks chiseled_tuff chiseled_tuff_bricks "
                + "cobbled_deepslate cobbled_deepslate_slab cobbled_deepslate_stairs cobblestone cobblestone_slab "
                + "cobblestone_stairs cracked_deepslate_bricks cracked_deepslate_tiles cracked_nether_bricks "
                + "cracked_polished_blackstone_bricks cracked_stone_bricks deepslate deepslate_brick_slab "
                + "deepslate_brick_stairs deepslate_bricks deepslate_tile_slab deepslate_tile_stairs deepslate_tiles "
                + "diorite diorite_slab diorite_stairs end_stone end_stone_brick_slab end_stone_brick_stairs "
                + "end_stone_bricks gilded_blackstone granite granite_slab granite_stairs mossy_cobblestone "
                + "mossy_cobblestone_slab mossy_cobblestone_stairs mossy_stone_brick_slab mossy_stone_brick_stairs "
                + "mossy_stone_bricks nether_brick_slab nether_brick_stairs nether_bricks polished_andesite "
                + "polished_andesite_slab polished_andesite_stairs polished_basalt polished_blackstone "
                + "polished_blackstone_brick_slab polished_blackstone_brick_stairs polished_blackstone_bricks "
                + "polished_blackstone_slab polished_blackstone_stairs polished_diorite polished_diorite_slab "
                + "polished_diorite_stairs polished_granite polished_granite_slab polished_granite_stairs polished_tuff "
                + "polished_tuff_slab polished_tuff_stairs red_nether_brick_slab red_nether_brick_stairs "
                + "red_nether_bricks smooth_basalt smooth_stone smooth_stone_slab stone stone_brick_slab "
                + "stone_brick_stairs stone_bricks stone_slab stone_stairs tuff tuff_brick_slab tuff_brick_stairs "
                + "tuff_bricks tuff_slab tuff_stairs");
        add("natural",
                "acacia_leaves azalea_leaves birch_leaves blue_ice cherry_leaves chiseled_red_sandstone "
                + "chiseled_sandstone clay coarse_dirt crimson_nylium crying_obsidian dark_oak_leaves dark_prismarine "
                + "dark_prismarine_slab dark_prismarine_stairs dirt dripstone_block flowering_azalea_leaves grass_block "
                + "gravel honey_block honeycomb_block ice jungle_leaves magma_block mangrove_leaves mangrove_roots "
                + "moss_block mud mud_brick_slab mud_brick_stairs mud_bricks muddy_mangrove_roots mycelium netherrack "
                + "oak_leaves obsidian ochre_froglight packed_ice packed_mud pearlescent_froglight podzol prismarine "
                + "prismarine_brick_slab prismarine_brick_stairs prismarine_bricks prismarine_slab prismarine_stairs "
                + "red_sand red_sandstone red_sandstone_slab red_sandstone_stairs resin_brick_slab resin_brick_stairs "
                + "resin_bricks rooted_dirt sand sandstone sandstone_slab sandstone_stairs sea_lantern slime_block "
                + "smooth_red_sandstone smooth_red_sandstone_slab smooth_red_sandstone_stairs smooth_sandstone "
                + "smooth_sandstone_slab smooth_sandstone_stairs snow_block soul_sand soul_soil sponge spruce_leaves "
                + "verdant_froglight warped_nylium");
        add("decorative_blocks",
                "black_concrete black_concrete_powder black_glazed_terracotta black_stained_glass "
                + "black_stained_glass_pane black_terracotta black_wool blue_concrete blue_concrete_powder "
                + "blue_glazed_terracotta blue_stained_glass blue_stained_glass_pane blue_terracotta blue_wool "
                + "bookshelf brain_coral_block brown_concrete brown_concrete_powder brown_glazed_terracotta "
                + "brown_stained_glass brown_stained_glass_pane brown_terracotta brown_wool bubble_coral_block "
                + "chiseled_quartz_block cut_copper cyan_concrete cyan_concrete_powder cyan_glazed_terracotta "
                + "cyan_stained_glass cyan_stained_glass_pane cyan_terracotta cyan_wool dead_brain_coral_block "
                + "dead_bubble_coral_block dead_fire_coral_block dead_horn_coral_block dead_tube_coral_block "
                + "fire_coral_block glass glass_pane glowstone gray_concrete gray_concrete_powder "
                + "gray_glazed_terracotta gray_stained_glass gray_stained_glass_pane gray_terracotta gray_wool "
                + "green_concrete green_concrete_powder green_glazed_terracotta green_stained_glass "
                + "green_stained_glass_pane green_terracotta green_wool horn_coral_block jack_o_lantern "
                + "light_blue_concrete light_blue_concrete_powder light_blue_glazed_terracotta light_blue_stained_glass "
                + "light_blue_stained_glass_pane light_blue_terracotta light_blue_wool light_gray_concrete "
                + "light_gray_concrete_powder light_gray_glazed_terracotta light_gray_stained_glass "
                + "light_gray_stained_glass_pane light_gray_terracotta light_gray_wool lime_concrete "
                + "lime_concrete_powder lime_glazed_terracotta lime_stained_glass lime_stained_glass_pane "
                + "lime_terracotta lime_wool magenta_concrete magenta_concrete_powder magenta_glazed_terracotta "
                + "magenta_stained_glass magenta_stained_glass_pane magenta_terracotta magenta_wool orange_concrete "
                + "orange_concrete_powder orange_glazed_terracotta orange_stained_glass orange_stained_glass_pane "
                + "orange_terracotta orange_wool pink_concrete pink_concrete_powder pink_glazed_terracotta "
                + "pink_stained_glass pink_stained_glass_pane pink_terracotta pink_wool purple_concrete "
                + "purple_concrete_powder purple_glazed_terracotta purple_stained_glass purple_stained_glass_pane "
                + "purple_terracotta purple_wool purpur_block purpur_pillar purpur_slab purpur_stairs quartz_bricks "
                + "quartz_pillar quartz_slab quartz_stairs red_concrete red_concrete_powder red_glazed_terracotta "
                + "red_stained_glass red_stained_glass_pane red_terracotta red_wool shroomlight smooth_quartz "
                + "smooth_quartz_slab smooth_quartz_stairs tinted_glass tube_coral_block waxed_copper_block "
                + "waxed_cut_copper white_concrete white_concrete_powder white_glazed_terracotta white_stained_glass "
                + "white_stained_glass_pane white_terracotta white_wool yellow_concrete yellow_concrete_powder "
                + "yellow_glazed_terracotta yellow_stained_glass yellow_stained_glass_pane yellow_terracotta "
                + "yellow_wool");
        add("decoration",
                "acacia_sapling angler_pottery_sherd archer_pottery_sherd arms_up_pottery_sherd azalea big_dripleaf "
                + "birch_sapling black_candle blade_pottery_sherd blue_candle brain_coral brain_coral_fan "
                + "brewer_pottery_sherd brown_candle bubble_coral bubble_coral_fan burn_pottery_sherd candle "
                + "cherry_sapling creeper_banner_pattern cyan_candle danger_pottery_sherd dark_oak_sapling "
                + "dead_brain_coral dead_brain_coral_fan dead_bubble_coral dead_bubble_coral_fan dead_fire_coral "
                + "dead_fire_coral_fan dead_horn_coral dead_horn_coral_fan dead_tube_coral dead_tube_coral_fan "
                + "explorer_pottery_sherd fire_coral fire_coral_fan flow_banner_pattern flow_pottery_sherd "
                + "flower_banner_pattern flower_pot flowering_azalea friend_pottery_sherd globe_banner_pattern "
                + "glow_lichen gray_candle green_candle guster_banner_pattern guster_pottery_sherd heart_pottery_sherd "
                + "heartbreak_pottery_sherd horn_coral horn_coral_fan howl_pottery_sherd jungle_sapling "
                + "light_blue_candle light_gray_candle lime_candle magenta_candle mangrove_propagule "
                + "miner_pottery_sherd mojang_banner_pattern mourner_pottery_sherd oak_sapling orange_candle "
                + "piglin_banner_pattern pink_candle plenty_pottery_sherd prize_pottery_sherd purple_candle red_candle "
                + "scrape_pottery_sherd sheaf_pottery_sherd shelter_pottery_sherd skull_banner_pattern "
                + "skull_pottery_sherd small_dripleaf snort_pottery_sherd spore_blossom spruce_sapling tube_coral "
                + "tube_coral_fan twisting_vines vine weeping_vines white_candle yellow_candle");
        add("misc",
                "axolotl_bucket beacon bell brush bucket campfire carrot_on_a_stick chain clock cobweb cod_bucket "
                + "compass dragon_breath dragon_egg echo_shard elytra end_crystal end_rod ender_eye experience_bottle "
                + "firework fishing_rod flint_and_steel heart_of_the_sea iron_bars lantern lava_bucket lead lodestone "
                + "map milk_bucket minecart name_tag nautilus_shell nether_star ominous_bottle pointed_dripstone "
                + "powder_snow_bucket pufferfish_bucket recovery_compass red_bed respawn_anchor saddle salmon_bucket "
                + "scaffolding sculk sculk_catalyst sculk_shrieker shears sniffer_egg snow snowball soul_campfire "
                + "soul_lantern soul_torch spyglass tadpole_bucket torch tropical_fish_bucket turtle_egg "
                + "warped_fungus_on_a_stick water_bucket writable_book");
    }

    private ShopSections() {
    }

    private static void add(String category, String items) {
        for (String item : items.split(" ")) {
            if (!item.isEmpty()) {
                SECTIONS.put(item, category);
            }
        }
    }

    public static String of(String path) {
        return SECTIONS.get(path);
    }
}
