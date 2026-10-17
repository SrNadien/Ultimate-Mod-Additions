package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.data.JobDefinition;
import nadiendev.ultimatemodadditions.data.ShopCategory;
import nadiendev.ultimatemodadditions.data.ShopItem;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.registry.UMATags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.GameMasterBlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ModShops {

    private static final int MAX_DEPTH = 10;
    private static final String VANILLA = "minecraft";

    private static final Set<String> EXCLUDED = Set.of("air", "barrier", "command_block", "chain_command_block",
            "repeating_command_block", "command_block_minecart", "structure_block", "structure_void", "jigsaw", "light",
            "debug_stick", "knowledge_book", "spawner", "trial_spawner", "vault", "reinforced_deepslate", "bedrock",
            "end_portal_frame", "written_book", "filled_map", "potion", "splash_potion", "lingering_potion",
            "tipped_arrow", "enchanted_book", "ominous_bottle", "test_block", "test_instance_block", "petrified_oak_slab",
            "farmland", "dirt_path", "frogspawn", "budding_amethyst", "infested_stone", "infested_cobblestone",
            "infested_stone_bricks", "infested_mossy_stone_bricks", "infested_cracked_stone_bricks",
            "infested_chiseled_stone_bricks", "infested_deepslate", "player_head", "crafter_placeholder");

    private static final Pattern ORE = Pattern.compile(
            "(_ore|^raw_.+|^ancient_debris|^(iron|gold|copper|diamond|emerald|lapis|redstone|coal)_block)$");
    private static final Pattern NETHERITE = Pattern.compile("^(netherite_.+|crying_obsidian)$");
    private static final Pattern DROPS = Pattern.compile(
            "(_skull|_head|scute|membrane|ink_sac|_horn|_egg|honeycomb|rabbit_.+|ghast_tear|blaze_.+|breeze_rod|wind_charge|"
                    + "magma_cream|nether_star|dragon_breath|shulker_shell|phantom_.+|spider_eye|rotten_flesh|slime_ball)$");
    private static final Pattern WORKSTATION = Pattern.compile(
            "^(crafting_table|furnace|blast_furnace|smoker|smithing_table|cartography_table|fletching_table|loom|"
                    + "stonecutter|grindstone|(chipped_|damaged_)?anvil|enchanting_table|brewing_stand|cauldron|composter|"
                    + "lectern|barrel|campfire|soul_campfire|beacon|respawn_anchor|lodestone|bell|chest|trapped_chest|"
                    + "ender_chest|(.+_)?shulker_box|jukebox|conduit|end_crystal|beehive|bee_nest|crafter)$");
    private static final Pattern REDSTONE = Pattern.compile(
            "(button|pressure_plate|repeater|comparator|piston|observer|hopper|dropper|dispenser|lever|redstone|^tnt|"
                    + "note_block|rail|daylight_detector|^target|lightning_rod|sculk_sensor|tripwire|copper_bulb|minecart|"
                    + "slime_block|honey_block|iron_door|iron_trapdoor|bulb)");
    private static final Pattern DECORATION = Pattern.compile(
            "(banner|bed$|carpet|candle|sign$|hanging_sign|flower|tulip|orchid|allium|bluet|poppy|dandelion|cornflower|"
                    + "lily|rose|peony|lilac|sunflower|torchflower|petals|sapling|leaves|vine|fern|grass|bush|azalea|"
                    + "mushroom|coral|pot$|decorated_pot|frame|painting|lantern|torch$|^chain$|iron_bars|pane|glass$|"
                    + "armor_stand|music_disc|_dye$|sherd|smithing_template|moss|sea_pickle|^kelp|hanging_roots|spore|"
                    + "cobweb|bookshelf|boat|raft|firework|dragon_egg|ladder|scaffolding|^map$|^compass$|^clock$|"
                    + "spyglass|ice$|snow|pumpkin$|carved_pumpkin|jack_o_lantern|head$|skull$)");
    private static final Pattern MATERIAL = Pattern.compile(
            "(ingot|nugget|^diamond$|^emerald$|lapis_lazuli|^quartz$|amethyst_shard|^coal$|charcoal|clay_ball|^brick$|"
                    + "nether_brick$|glowstone_dust|prismarine_.+|dust|flint|^paper$|^book$|^string$|^stick$|^bone$|"
                    + "^leather$|^feather$|gunpowder|^sugar$|^bone_meal$|^glass_bottle$|^bucket$|water_bucket|"
                    + "lava_bucket|milk_bucket|powder_snow_bucket|.+_bucket$|^echo_shard$|^heart_of_the_sea$|"
                    + "^nautilus_shell$|^ender_.+|^experience_bottle$|^name_tag$|^lead$|^saddle$|^totem_of_undying$)");
    private static final Pattern BASICS = Pattern.compile(
            "(seeds$|^wheat$|^beetroot$|^carrot$|^potato$|^sugar_cane$|^bamboo$|^cocoa_beans$|^nether_wart$|_log$|_stem$|"
                    + "_hyphae$|^stick$|^dirt$|^sand$|^red_sand$|^gravel$|^cobblestone$|^netherrack$|^snowball$|"
                    + "^egg$|^cactus$|^sweet_berries$|^glow_berries$|^chorus_fruit$|^pitcher_pod$|^torchflower_seeds$)");

    private static MinecraftServer cachedFor;
    private static Map<ResourceLocation, ShopCategory> zones = Map.of();
    private static Map<ResourceLocation, List<ShopItem>> fill = Map.of();
    private static Map<DailyReward.Period, Map<ResourceLocation, List<Item>>> rewardCandidates = Map.of();

    private record Source(Recipe<?> recipe, int count) {
    }

    private final Map<Item, Double> base = new HashMap<>();
    private final Map<Item, Double> memo = new HashMap<>();
    private final Set<Item> visiting = new HashSet<>();
    private final Map<Item, List<Source>> sources = new HashMap<>();

    private ModShops() {
    }

    public static void invalidate() {
        cachedFor = null;
        zones = Map.of();
        fill = Map.of();
        rewardCandidates = Map.of();
    }

    private static void ensure(MinecraftServer server) {
        if (cachedFor == server) {
            return;
        }
        try {
            ModShops builder = new ModShops();
            builder.prepare(server);
            zones = UMAConfig.COMMON.shopModZones.get() ? builder.buildZones() : Map.of();
            fill = UMAConfig.COMMON.shopVanillaFill.get() ? builder.buildFill() : Map.of();
            rewardCandidates = UMAConfig.COMMON.dailyModEntries.get() ? builder.buildRewardCandidates(zones) : Map.of();
        } catch (RuntimeException exception) {
            UMA.LOGGER.error("Could not build the generated parts of the shop", exception);
            zones = Map.of();
            fill = Map.of();
            rewardCandidates = Map.of();
        }
        cachedFor = server;
    }

    public static Map<ResourceLocation, ShopCategory> get(MinecraftServer server) {
        ensure(server);
        return zones;
    }

    public static List<DailyReward.Entry> rewardEntries(MinecraftServer server, DailyReward.Period period, long seed) {
        ensure(server);
        List<DailyReward.Entry> entries = new ArrayList<>();
        rewardCandidates.getOrDefault(period, Map.of()).forEach((zone, candidates) -> {
            Item pick = candidates.get(Math.floorMod((zone + ":" + period + ":" + seed).hashCode(), candidates.size()));
            entries.add(new DailyReward.Entry(0.0D, List.of(new JobDefinition.RewardItem(pick, 1)), List.of()));
        });
        return entries;
    }

    public static Map<ResourceLocation, List<ShopItem>> fill(MinecraftServer server) {
        ensure(server);
        return fill;
    }

    private void prepare(MinecraftServer server) {
        for (ShopCategory category : UMAData.shop().values()) {
            for (ShopItem item : category.items()) {
                double price = item.buyable() ? item.buy() : item.sell() * 8.0D;
                if (price > 0.0D) {
                    base.merge(item.stack().getItem(), price, Math::min);
                }
            }
        }
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            try {
                Recipe<?> recipe = holder.value();
                ItemStack result = recipe.getResultItem(server.registryAccess());
                if (!result.isEmpty()) {
                    sources.computeIfAbsent(result.getItem(), item -> new ArrayList<>())
                            .add(new Source(recipe, Math.max(1, result.getCount())));
                }
            } catch (RuntimeException ignored) {
            }
        }
    }

    private Map<ResourceLocation, ShopCategory> buildZones() {
        Set<String> skipped = new HashSet<>();
        UMAConfig.COMMON.shopModZoneBlacklist.get().forEach(id -> skipped.add(id.toLowerCase(Locale.ROOT)));
        double markup = UMAConfig.COMMON.shopModZoneMarkup.get();
        double ratio = UMAConfig.COMMON.shopModZoneSellRatio.get();
        int limit = UMAConfig.COMMON.shopModZoneMaxItems.get();
        Map<String, Pattern> filters = new HashMap<>();
        for (String entry : UMAConfig.COMMON.shopModZoneFilters.get()) {
            int split = entry.indexOf('=');
            try {
                filters.put(entry.substring(0, split).trim().toLowerCase(Locale.ROOT),
                        Pattern.compile(entry.substring(split + 1).trim()));
            } catch (RuntimeException exception) {
                UMA.LOGGER.error("Bad shop mod zone filter {}", entry);
            }
        }

        Map<String, List<Item>> byMod = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            ItemStack stack = new ItemStack(item);
            Pattern filter = filters.get(key.getNamespace());
            if (skipped.contains(key.getNamespace()) || stack.isEmpty() || stack.is(UMATags.NOT_PURCHASABLE)
                    || filter != null && !filter.matcher(key.getPath()).find()) {
                continue;
            }
            byMod.computeIfAbsent(key.getNamespace(), mod -> new ArrayList<>()).add(item);
        }

        List<Map.Entry<String, List<Item>>> mods = new ArrayList<>(byMod.entrySet());
        mods.sort(Comparator.comparing(entry -> displayName(entry.getKey()).toLowerCase(Locale.ROOT)));
        Map<ResourceLocation, ShopCategory> result = new LinkedHashMap<>();
        int order = 100;
        for (Map.Entry<String, List<Item>> mod : mods) {
            List<Item> found = new ArrayList<>(mod.getValue());
            found.sort(Comparator.comparingDouble(this::price));
            List<ShopItem> items = new ArrayList<>();
            for (Item item : found.subList(0, Math.min(limit, found.size()))) {
                items.add(priced(item, markup, ratio));
            }
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(UMA.MODID, "mod_" + mod.getKey());
            result.put(id, new ShopCategory(displayName(mod.getKey()),
                    items.get(items.size() / 2).stack().getItem(), order++, items));
        }
        return result;
    }

    private static boolean rewardExcluded(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return item instanceof GameMasterBlockItem || item instanceof SpawnEggItem || EXCLUDED.contains(key.getPath())
                || new ItemStack(item).is(UMATags.NOT_PURCHASABLE);
    }

    private Map<DailyReward.Period, Map<ResourceLocation, List<Item>>> buildRewardCandidates(
            Map<ResourceLocation, ShopCategory> modZones) {
        Map<DailyReward.Period, Map<ResourceLocation, List<Item>>> result = new EnumMap<>(DailyReward.Period.class);
        for (DailyReward.Period period : DailyReward.Period.values()) {
            double budget = switch (period) {
                case DAILY -> UMAConfig.COMMON.dailyModBonusDaily.get();
                case WEEKLY -> UMAConfig.COMMON.dailyModBonusWeekly.get();
                case MONTHLY -> UMAConfig.COMMON.dailyModBonusMonthly.get();
            };
            Map<ResourceLocation, List<Item>> perZone = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, ShopCategory> zone : modZones.entrySet()) {
                List<Item> candidates = new ArrayList<>();
                for (ShopItem shopItem : zone.getValue().items()) {
                    Item item = shopItem.stack().getItem();
                    if (shopItem.buyable() && shopItem.buy() <= budget && sources.containsKey(item)
                            && !rewardExcluded(item)) {
                        candidates.add(item);
                    }
                }
                if (candidates.isEmpty()) {
                    continue;
                }
                candidates.sort(Comparator.comparing(item -> BuiltInRegistries.ITEM.getKey(item).toString()));
                perZone.put(zone.getKey(), candidates);
            }
            result.put(period, perZone);
        }
        return result;
    }

    private Map<ResourceLocation, List<ShopItem>> buildFill() {
        Set<Item> listed = new HashSet<>();
        for (ShopCategory category : UMAData.shop().values()) {
            for (ShopItem item : category.items()) {
                listed.add(item.stack().getItem());
            }
        }
        double markup = UMAConfig.COMMON.shopVanillaFillMarkup.get();
        double ratio = UMAConfig.COMMON.shopVanillaFillSellRatio.get();
        Map<ResourceLocation, List<Item>> grouped = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            ItemStack stack = new ItemStack(item);
            String path = key.getPath();
            if (!VANILLA.equals(key.getNamespace()) || listed.contains(item) || stack.isEmpty()
                    || EXCLUDED.contains(path) || path.endsWith("_spawn_egg") || stack.is(UMATags.NOT_PURCHASABLE)) {
                continue;
            }
            grouped.computeIfAbsent(classify(item, stack, path), id -> new ArrayList<>()).add(item);
        }
        Map<ResourceLocation, List<ShopItem>> result = new LinkedHashMap<>();
        grouped.forEach((category, items) -> {
            items.sort(Comparator.comparingDouble(this::price));
            List<ShopItem> priced = new ArrayList<>();
            for (Item item : items) {
                priced.add(priced(item, markup, ratio));
            }
            result.put(category, priced);
        });
        return result;
    }

    private static ResourceLocation category(String name) {
        return ResourceLocation.fromNamespaceAndPath(UMA.MODID, name);
    }

    private static ResourceLocation classify(Item item, ItemStack stack, String path) {
        if (ORE.matcher(path).find()) {
            return category("ores");
        }
        if (item instanceof SwordItem || item instanceof AxeItem || item instanceof PickaxeItem
                || item instanceof ShovelItem || item instanceof HoeItem || item instanceof ArmorItem
                || item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem
                || item instanceof ShieldItem || item instanceof MaceItem || item instanceof ElytraItem
                || item instanceof ArrowItem || item instanceof FishingRodItem || item instanceof ShearsItem
                || path.endsWith("_horse_armor") || path.equals("flint_and_steel") || path.equals("carrot_on_a_stick")
                || path.equals("warped_fungus_on_a_stick") || path.equals("wolf_armor")) {
            return category("combat");
        }
        if (NETHERITE.matcher(path).find()) {
            return item instanceof BlockItem ? category("blocks") : category("materials");
        }
        if (stack.has(DataComponents.FOOD)) {
            return category("food");
        }
        if (DROPS.matcher(path).find() || MATERIAL.matcher(path).find()) {
            return category("materials");
        }
        if (WORKSTATION.matcher(path).find()) {
            return category("workstations");
        }
        if (REDSTONE.matcher(path).find()) {
            return category("technology");
        }
        if (DECORATION.matcher(path).find()) {
            return category("decoration");
        }
        if (BASICS.matcher(path).find()) {
            return category("basics");
        }
        return item instanceof BlockItem ? category("blocks") : category("materials");
    }

    private ShopItem priced(Item item, double markup, double ratio) {
        double buy = Math.max(1.0D, Math.round(price(item) * markup));
        double sell = Math.max(0.1D, Math.round(buy * ratio * 100.0D) / 100.0D);
        return new ShopItem(new ItemStack(item), buy, sell, true);
    }

    private static String displayName(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo())
                .map(IModInfo::getDisplayName)
                .orElse(modId);
    }

    private double price(Item item) {
        return price(item, 0);
    }

    private double price(Item item, int depth) {
        Double known = base.get(item);
        if (known != null) {
            return known;
        }
        Double cached = memo.get(item);
        if (cached != null) {
            return cached;
        }
        if (depth >= MAX_DEPTH || !visiting.add(item)) {
            return Double.POSITIVE_INFINITY;
        }
        double best = Double.POSITIVE_INFINITY;
        for (Source source : sources.getOrDefault(item, List.of())) {
            double total = 0.0D;
            boolean valid = true;
            int ingredients = 0;
            for (Ingredient ingredient : source.recipe().getIngredients()) {
                if (ingredient.isEmpty()) {
                    continue;
                }
                double cheapest = Double.POSITIVE_INFINITY;
                for (ItemStack candidate : ingredient.getItems()) {
                    cheapest = Math.min(cheapest, price(candidate.getItem(), depth + 1));
                }
                if (Double.isInfinite(cheapest)) {
                    valid = false;
                    break;
                }
                total += cheapest;
                ingredients++;
            }
            if (valid && ingredients > 0) {
                best = Math.min(best, total / source.count());
            }
        }
        visiting.remove(item);
        double result = Double.isInfinite(best) ? fallback(item) : Math.min(best, 1.0E9D);
        memo.put(item, result);
        return result;
    }

    private static double fallback(Item item) {
        return switch (new ItemStack(item).getRarity()) {
            case COMMON -> 12.0D;
            case UNCOMMON -> 60.0D;
            case RARE -> 250.0D;
            case EPIC -> 1200.0D;
        };
    }
}
