package nadiendev.ultimatemodadditions.config;

import nadiendev.ultimatemodadditions.currency.Denomination;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class UMAConfig {

    private static final Map<Denomination, Double> DEFAULT_CASH_CHANCES = new EnumMap<>(Denomination.class);

    static {
        DEFAULT_CASH_CHANCES.put(Denomination.C5, 0.70D);
        DEFAULT_CASH_CHANCES.put(Denomination.C10, 0.50D);
        DEFAULT_CASH_CHANCES.put(Denomination.C25, 0.35D);
        DEFAULT_CASH_CHANCES.put(Denomination.C50, 0.25D);
        DEFAULT_CASH_CHANCES.put(Denomination.P2, 0.15D);
        DEFAULT_CASH_CHANCES.put(Denomination.P5, 0.10D);
        DEFAULT_CASH_CHANCES.put(Denomination.P10, 0.08D);
        DEFAULT_CASH_CHANCES.put(Denomination.B2, 0.06D);
        DEFAULT_CASH_CHANCES.put(Denomination.B5, 0.05D);
        DEFAULT_CASH_CHANCES.put(Denomination.B10, 0.04D);
        DEFAULT_CASH_CHANCES.put(Denomination.B20, 0.03D);
        DEFAULT_CASH_CHANCES.put(Denomination.B50, 0.02D);
        DEFAULT_CASH_CHANCES.put(Denomination.B100, 0.02D);
        DEFAULT_CASH_CHANCES.put(Denomination.L1, 0.01D);
        DEFAULT_CASH_CHANCES.put(Denomination.L2, 0.01D);
        DEFAULT_CASH_CHANCES.put(Denomination.L10, 0.01D);
        DEFAULT_CASH_CHANCES.put(Denomination.L20, 0.01D);
    }

    private static final List<String> DEFAULT_FLASHERO_TRADES = List.of(
            "1x2_pe=1x2_mangos",
            "1x2_mangos=1x2_pe",
            "1x5_pe=1x5_mangos",
            "1x5_mangos=1x5_pe",
            "1x10_pe=1x10_mangos",
            "1x10_mangos=1x10_pe",
            "10x50_centavos=1x5_mangos",
            "20x25_centavos=1x5_mangos",
            "40x5_centavos=1x2_mangos",
            "1x5_mangos=10x50_centavos");

    private static final List<String> DEFAULT_SPAWNER_ENTITIES = List.of(
            "minecraft:zombie",
            "minecraft:skeleton",
            "minecraft:spider",
            "minecraft:creeper",
            "minecraft:husk",
            "minecraft:stray",
            "minecraft:witch",
            "minecraft:pillager",
            "minecraft:blaze",
            "minecraft:wither_skeleton",
            "minecraft:vindicator",
            "minecraft:enderman");

    public static final String DEFAULT_MAX_BALANCE_MANGOS = "100000000000000000000000000000000";

    private static final List<Long> DEFAULT_WITHDRAW_AMOUNTS = List.of(
            10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L);

    public enum PayTarget {
        ACCOUNT,
        CASH
    }

    public enum CardMode {
        OFF,
        FALLBACK,
        FIRST
    }

    private static boolean isDigits(Object value) {
        return value instanceof String text && !text.isEmpty() && text.chars().allMatch(Character::isDigit);
    }

    private static boolean isPositiveNumber(Object value) {
        return value instanceof Number number && number.longValue() > 0L;
    }

    private static boolean isResourceId(Object value) {
        return value instanceof String text && ResourceLocation.tryParse(text) != null;
    }

    public static final class Common {

        public final ModConfigSpec.BooleanValue coliseumEnabled;
        public final ModConfigSpec.BooleanValue coliseumLootOnly;
        public final ModConfigSpec.BooleanValue keepVanillaDropsInColiseum;
        public final ModConfigSpec.ConfigValue<String> defaultDifficulty;
        public final ModConfigSpec.BooleanValue scaleWithPlayerCount;
        public final ModConfigSpec.DoubleValue playerCountScaling;
        public final ModConfigSpec.BooleanValue coliseumBuildArena;
        public final ModConfigSpec.IntValue coliseumArenaRadius;
        public final ModConfigSpec.IntValue coliseumFloorY;

        public final ModConfigSpec.BooleanValue portalRequirements;
        public final ModConfigSpec.IntValue portalRequiredLevels;

        public final ModConfigSpec.BooleanValue coliseumSpawners;
        public final ModConfigSpec.ConfigValue<List<? extends String>> coliseumSpawnerEntities;
        public final ModConfigSpec.IntValue coliseumSpawnerCount;
        public final ModConfigSpec.IntValue coliseumSpawnerMinDelay;
        public final ModConfigSpec.IntValue coliseumSpawnerMaxDelay;
        public final ModConfigSpec.IntValue coliseumSpawnerAmount;
        public final ModConfigSpec.IntValue coliseumSpawnerMaxNearby;
        public final ModConfigSpec.IntValue coliseumSpawnerPlayerRange;
        public final ModConfigSpec.IntValue coliseumSpawnerRange;

        public final ModConfigSpec.BooleanValue mobScalingEnabled;
        public final ModConfigSpec.BooleanValue mobScalingColiseumOnly;
        public final ModConfigSpec.DoubleValue globalHealthMultiplier;
        public final ModConfigSpec.DoubleValue globalDamageMultiplier;
        public final ModConfigSpec.IntValue maxScaledHealth;

        public final ModConfigSpec.BooleanValue coinsEnabled;
        public final ModConfigSpec.BooleanValue coinsColiseumOnly;
        public final ModConfigSpec.BooleanValue coinsOnlyFromPlayerKills;
        public final ModConfigSpec.BooleanValue cashPreview;
        public final ModConfigSpec.DoubleValue cashChanceMultiplier;
        public final ModConfigSpec.DoubleValue cashBossChanceMultiplier;
        public final ModConfigSpec.IntValue cashMaxCount;
        public final ModConfigSpec.ConfigValue<List<? extends String>> cashDimensionBlacklist;
        private final Map<Denomination, ModConfigSpec.DoubleValue> cashChances = new EnumMap<>(Denomination.class);

        public final ModConfigSpec.BooleanValue cashChestsEnabled;
        public final ModConfigSpec.DoubleValue cashChestChance;
        public final ModConfigSpec.IntValue cashChestMinCount;
        public final ModConfigSpec.IntValue cashChestMaxCount;
        public final ModConfigSpec.ConfigValue<List<? extends String>> cashChestBlacklist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> cashChestWhitelist;

        public final ModConfigSpec.BooleanValue walletLooseCashFirst;
        public final ModConfigSpec.IntValue walletMaxCounted;
        public final ModConfigSpec.ConfigValue<String> walletMaxBalance;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> walletWithdrawAmounts;
        public final ModConfigSpec.LongValue maxWithdrawMangos;

        public final ModConfigSpec.BooleanValue bankEnabled;
        public final ModConfigSpec.ConfigValue<String> bankMaxBalance;
        public final ModConfigSpec.BooleanValue cardBoundToOwner;
        public final ModConfigSpec.LongValue cardPriceMangos;
        public final ModConfigSpec.EnumValue<CardMode> stallCardMode;
        public final ModConfigSpec.ConfigValue<List<? extends Number>> atmWithdrawAmounts;

        public final ModConfigSpec.BooleanValue rentEnabled;
        public final ModConfigSpec.IntValue rentPeriodDays;
        public final ModConfigSpec.BooleanValue rentUseGameDays;
        public final ModConfigSpec.BooleanValue rentFirstPeriodFree;
        public final ModConfigSpec.LongValue rentBasePriceMangos;
        public final ModConfigSpec.LongValue rentPricePerChunkMangos;
        public final ModConfigSpec.DoubleValue rentGraceDays;
        public final ModConfigSpec.DoubleValue rentWarnDays;
        public final ModConfigSpec.BooleanValue rentChargeMembers;
        public final ModConfigSpec.BooleanValue rentSkipServerTeams;
        public final ModConfigSpec.IntValue rentCheckSeconds;
        public final ModConfigSpec.BooleanValue rentTitles;
        public final ModConfigSpec.BooleanValue rentBroadcastNotices;
        public final ModConfigSpec.BooleanValue rentBroadcastPayments;
        public final ModConfigSpec.BooleanValue teamBankEnabled;
        public final ModConfigSpec.BooleanValue teamBankPartyOnly;
        public final ModConfigSpec.ConfigValue<String> teamBankWithdrawRank;
        public final ModConfigSpec.BooleanValue teamBankPaysRent;

        public final ModConfigSpec.BooleanValue shopEnabled;
        public final ModConfigSpec.IntValue shopPermissionLevel;
        public final ModConfigSpec.BooleanValue shopRequireStallNearby;
        public final ModConfigSpec.IntValue shopStallRadius;
        public final ModConfigSpec.DoubleValue shopSellCommission;
        public final ModConfigSpec.IntValue shopMaxAmount;
        public final ModConfigSpec.BooleanValue shopAdminGui;
        public final ModConfigSpec.DoubleValue shopInflationPercent;
        public final ModConfigSpec.BooleanValue shopInflationOnSell;
        public final ModConfigSpec.DoubleValue shopSellMultiplier;
        public final ModConfigSpec.BooleanValue shopGeneratedRequireRecipe;
        public final ModConfigSpec.BooleanValue shopGeneratedSkipCreative;
        public final ModConfigSpec.ConfigValue<List<? extends String>> shopGeneratedIgnoredNamespaces;
        public final ModConfigSpec.ConfigValue<List<? extends String>> rootCommandShortcuts;
        public final ModConfigSpec.BooleanValue shopModZones;
        public final ModConfigSpec.ConfigValue<List<? extends String>> shopModZoneBlacklist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> shopModZoneFilters;
        public final ModConfigSpec.DoubleValue shopModZoneMarkup;
        public final ModConfigSpec.DoubleValue shopModZoneSellRatio;
        public final ModConfigSpec.IntValue shopModZoneMaxItems;
        public final ModConfigSpec.BooleanValue shopVanillaFill;
        public final ModConfigSpec.DoubleValue shopVanillaFillMarkup;
        public final ModConfigSpec.DoubleValue shopVanillaFillSellRatio;

        public final ModConfigSpec.BooleanValue dailyEnabled;
        public final ModConfigSpec.IntValue dailyResetHour;
        public final ModConfigSpec.EnumValue<PayTarget> dailyPayTo;
        public final ModConfigSpec.BooleanValue dailyCommands;
        public final ModConfigSpec.BooleanValue dailyNotify;
        public final ModConfigSpec.BooleanValue dailyModEntries;
        public final ModConfigSpec.BooleanValue dailyModBonus;
        public final ModConfigSpec.DoubleValue dailyModBonusChance;
        public final ModConfigSpec.DoubleValue dailyModBonusDaily;
        public final ModConfigSpec.DoubleValue dailyModBonusWeekly;
        public final ModConfigSpec.DoubleValue dailyModBonusMonthly;
        public final ModConfigSpec.BooleanValue jobsEnabled;
        public final ModConfigSpec.IntValue jobsMaxPerPlayer;
        public final ModConfigSpec.DoubleValue jobsIncomeMultiplier;
        public final ModConfigSpec.DoubleValue jobsXpMultiplier;
        public final ModConfigSpec.DoubleValue jobsXpNeededMultiplier;
        public final ModConfigSpec.EnumValue<PayTarget> jobsPayTo;
        public final ModConfigSpec.BooleanValue jobsShowIncome;
        public final ModConfigSpec.BooleanValue jobsIgnoreCreative;
        public final ModConfigSpec.BooleanValue jobsIgnoreSpawnerMobs;
        public final ModConfigSpec.DoubleValue jobsDailyLimit;
        public final ModConfigSpec.IntValue jobsTopSize;
        public final ModConfigSpec.IntValue jobsExploreLimit;
        public final ModConfigSpec.ConfigValue<List<? extends Integer>> jobsProgressAlerts;
        public final ModConfigSpec.BooleanValue jobsRewardCommands;

        public final ModConfigSpec.BooleanValue flasheroEnabled;
        public final ModConfigSpec.ConfigValue<String> flasheroName;
        public final ModConfigSpec.DoubleValue flasheroScale;
        public final ModConfigSpec.IntValue flasheroMaxUses;
        public final ModConfigSpec.IntValue flasheroXp;
        public final ModConfigSpec.ConfigValue<List<? extends String>> flasheroTrades;

        public final ModConfigSpec.BooleanValue bagsEnabled;
        public final ModConfigSpec.DoubleValue bagDropChance;
        public final ModConfigSpec.BooleanValue bagsColiseumOnly;
        public final ModConfigSpec.BooleanValue bagsOnlyFromPlayerKills;

        public double cashDropChance(Denomination denomination) {
            return cashChances.get(denomination).get();
        }




        Common(ModConfigSpec.Builder builder) {
            builder.push("coliseum");
            coliseumEnabled = builder
                    .comment("Master switch for the Coliseum dimension and everything bound to it.")
                    .define("enabled", true);
            coliseumLootOnly = builder
                    .comment("When true, custom mob loot rules only apply inside the Coliseum dimension.")
                    .define("custom_loot_only_in_coliseum", true);
            keepVanillaDropsInColiseum = builder
                    .comment("When false, mobs killed in the Coliseum drop only the custom loot.")
                    .define("keep_vanilla_drops", false);
            defaultDifficulty = builder
                    .comment("Difficulty profile id used when no rule matches, from data/<ns>/uma/difficulty/*.json")
                    .define("default_difficulty", "ultimatemodadditions:normal");
            scaleWithPlayerCount = builder
                    .comment("Scale mob stats with the number of players inside the Coliseum.")
                    .define("scale_with_player_count", true);
            playerCountScaling = builder
                    .comment("Extra multiplier added per additional player. 0.25 means plus 25 percent each.")
                    .defineInRange("player_count_scaling", 0.25D, 0.0D, 10.0D);
            coliseumBuildArena = builder
                    .comment("Build the stone arena the first time the Coliseum dimension loads.")
                    .define("build_arena", true);
            coliseumArenaRadius = builder
                    .comment("Radius in blocks of the sand floor inside the arena.")
                    .defineInRange("arena_radius", 30, 8, 96);
            coliseumFloorY = builder
                    .comment("Height of the arena floor inside the Coliseum dimension.")
                    .defineInRange("arena_floor_y", 64, 8, 200);

            builder.push("portal");
            portalRequirements = builder
                    .comment("Check the experience requirement before the portal takes anyone through.",
                            "Turn this off and the portal works for everybody.")
                    .define("enforce_requirements", true);
            portalRequiredLevels = builder
                    .comment("Vanilla experience levels needed to go through. They are not spent.")
                    .defineInRange("required_experience_levels", 100, 0, 24000);
            builder.pop();

            builder.push("spawners");
            coliseumSpawners = builder
                    .comment("Place mob spawners on the arena floor when the Coliseum is built.",
                            "Set to false and run '/uma spawners clear' to get rid of the ones already placed.")
                    .define("enabled", true);
            coliseumSpawnerEntities = builder
                    .comment("Entity ids the arena spawners use. Each spawner gets one type,",
                            "handed out around the ring so every type on this list shows up.",
                            "Ids that do not exist are skipped with a warning in the log.")
                    .defineListAllowEmpty("entities", DEFAULT_SPAWNER_ENTITIES,
                            () -> "minecraft:zombie", UMAConfig::isResourceId);
            coliseumSpawnerCount = builder
                    .comment("How many spawners to place around the arena. 0 places none.")
                    .defineInRange("count", 12, 0, 128);
            coliseumSpawnerMinDelay = builder
                    .comment("Shortest wait between spawns, in ticks. 20 ticks is one second.")
                    .defineInRange("min_spawn_delay", 200, 1, 32767);
            coliseumSpawnerMaxDelay = builder
                    .comment("Longest wait between spawns, in ticks.")
                    .defineInRange("max_spawn_delay", 600, 1, 32767);
            coliseumSpawnerAmount = builder
                    .comment("How many mobs each spawn attempt tries to place.")
                    .defineInRange("spawn_count", 4, 1, 128);
            coliseumSpawnerMaxNearby = builder
                    .comment("The spawner stops once this many of its mobs are already alive nearby.")
                    .defineInRange("max_nearby_entities", 8, 1, 128);
            coliseumSpawnerPlayerRange = builder
                    .comment("How close a player has to be, in blocks, for the spawner to run.")
                    .defineInRange("required_player_range", 24, 1, 128);
            coliseumSpawnerRange = builder
                    .comment("How far from the spawner, in blocks, the mobs can appear.")
                    .defineInRange("spawn_range", 6, 1, 64);
            builder.pop();
            builder.pop();

            builder.push("mob_scaling");
            mobScalingEnabled = builder.define("enabled", true);
            mobScalingColiseumOnly = builder
                    .comment("When true, stat scaling is applied only inside the Coliseum dimension.")
                    .define("only_in_coliseum", true);
            globalHealthMultiplier = builder
                    .comment("Applied on top of the difficulty profile.")
                    .defineInRange("global_health_multiplier", 1.0D, 0.01D, 1000.0D);
            globalDamageMultiplier = builder
                    .defineInRange("global_damage_multiplier", 1.0D, 0.01D, 1000.0D);
            maxScaledHealth = builder
                    .comment("Hard cap for scaled max health. 0 disables the cap.")
                    .defineInRange("max_scaled_health", 0, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("coins");
            coinsEnabled = builder
                    .comment("Mobs drop coins and bills.")
                    .define("enabled", true);
            coinsColiseumOnly = builder
                    .comment("When true, coins and bills only drop inside the Coliseum dimension.")
                    .define("only_in_coliseum", false);
            coinsOnlyFromPlayerKills = builder
                    .comment("When true, coins and bills only drop from mobs killed by a player.")
                    .define("only_from_player_kills", false);
            cashPreview = builder
                    .comment("Show a large picture of the bill, coin or wallet when hovering over it.")
                    .define("show_preview_tooltip", true);
            cashChanceMultiplier = builder
                    .comment("Multiplies every per-denomination chance below. The result is capped at 1.0.")
                    .defineInRange("chance_multiplier", 1.0D, 0.0D, 100.0D);
            cashBossChanceMultiplier = builder
                    .comment("Extra multiplier for bosses, applied on top of chance_multiplier.")
                    .defineInRange("boss_chance_multiplier", 1.0D, 0.0D, 100.0D);
            cashMaxCount = builder
                    .comment("How many of one denomination a single successful roll drops.")
                    .defineInRange("max_count_per_denomination", 1, 1, 64);
            cashDimensionBlacklist = builder
                    .comment("Dimension ids where mobs never drop coins or bills.")
                    .defineListAllowEmpty("dimension_blacklist", List.of(), () -> "minecraft:the_end",
                            UMAConfig::isResourceId);
            builder.push("chances");
            for (Denomination denomination : Denomination.values()) {
                cashChances.put(denomination, builder
                        .comment("Chance one kill drops a " + denomination.key() + ".")
                        .defineInRange(denomination.key(), DEFAULT_CASH_CHANCES.get(denomination), 0.0D, 1.0D));
            }
            builder.pop();
            builder.push("chests");
            cashChestsEnabled = builder
                    .comment("Coins and bills can show up in loot chests of any mod, Lootr included.")
                    .define("enabled", true);
            cashChestChance = builder
                    .comment("Chance per chest roll. 0.0001 is 0.01 percent.")
                    .defineInRange("chance", 0.0001D, 0.0D, 1.0D);
            cashChestMinCount = builder.defineInRange("min_count", 1, 1, 64);
            cashChestMaxCount = builder.defineInRange("max_count", 1, 1, 64);
            cashChestBlacklist = builder
                    .comment("Regular expressions matched against the loot table id. A match skips the table.")
                    .defineListAllowEmpty("table_blacklist", List.of(), () -> "minecraft:chests/spawn_bonus_chest",
                            value -> value instanceof String);
            cashChestWhitelist = builder
                    .comment("Regular expressions matched against the loot table id.",
                            "When this list has entries, only matching tables can drop cash.")
                    .defineListAllowEmpty("table_whitelist", List.of(), () -> "minecraft:chests/.*",
                            value -> value instanceof String);
            builder.pop();
            builder.pop();

            builder.push("wallet");
            walletMaxBalance = builder
                    .comment("Most money one wallet can hold, in mangos. Digits only.",
                            "The default is 100 quintillion in the Spanish long scale (100 x 10^30).")
                    .define("max_balance_mangos", DEFAULT_MAX_BALANCE_MANGOS, UMAConfig::isDigits);
            walletWithdrawAmounts = builder
                    .comment("Withdraw buttons shown in the wallet screen, in mangos.")
                    .defineListAllowEmpty("withdraw_amounts_mangos", DEFAULT_WITHDRAW_AMOUNTS,
                            () -> 100L, UMAConfig::isPositiveNumber);
            maxWithdrawMangos = builder
                    .comment("Most money a single withdraw action hands out as loose cash, in mangos.",
                            "It keeps one click from flooding the inventory with stacks.")
                    .defineInRange("max_withdraw_mangos", 100_000_000L, 1L, Long.MAX_VALUE / 200L);
            walletLooseCashFirst = builder
                    .comment("Pay with cash from the inventory first and use wallet balances afterwards.")
                    .define("loose_cash_first", true);
            walletMaxCounted = builder
                    .comment("How many wallets in one inventory count toward the balance.")
                    .defineInRange("max_wallets_counted", 4, 1, 64);
            builder.pop();

            builder.push("bank");
            bankEnabled = builder
                    .comment("Enable ATMs, debit cards and bank accounts.")
                    .define("enabled", true);
            bankMaxBalance = builder
                    .comment("Most money one account can hold, in mangos. Digits only.")
                    .define("max_balance_mangos", DEFAULT_MAX_BALANCE_MANGOS, UMAConfig::isDigits);
            cardBoundToOwner = builder
                    .comment("A debit card only works for the player it was issued to.")
                    .define("card_bound_to_owner", true);
            cardPriceMangos = builder
                    .comment("What a new debit card costs, taken from the account, in mangos.")
                    .defineInRange("card_price_mangos", 0L, 0L, Long.MAX_VALUE / 200L);
            stallCardMode = builder
                    .comment("How the exchange stand uses the debit card in your inventory.",
                            "OFF never charges it, FALLBACK charges it for what cash cannot cover,",
                            "FIRST charges it before any cash.")
                    .defineEnum("stall_card_mode", CardMode.FALLBACK);
            atmWithdrawAmounts = builder
                    .comment("Withdraw buttons shown in the ATM screen, in mangos.")
                    .defineListAllowEmpty("withdraw_amounts_mangos", DEFAULT_WITHDRAW_AMOUNTS,
                            () -> 100L, UMAConfig::isPositiveNumber);
            builder.pop();

            builder.push("rent");
            rentEnabled = builder
                    .comment("Charge claim rent. Needs FTB Chunks and FTB Teams.",
                            "A team that does not pay loses every claim.")
                    .define("enabled", true);
            rentPeriodDays = builder
                    .comment("Days between two rent payments.")
                    .defineInRange("period_days", 7, 1, 365);
            rentUseGameDays = builder
                    .comment("Count game days (20 minutes each) instead of real days.")
                    .define("use_game_days", false);
            rentFirstPeriodFree = builder
                    .comment("A team that has never paid gets its first period for free.")
                    .define("first_period_free", true);
            rentBasePriceMangos = builder
                    .comment("Flat rent per period, in mangos.")
                    .defineInRange("base_price_mangos", 0L, 0L, Long.MAX_VALUE / 200L);
            rentPricePerChunkMangos = builder
                    .comment("Rent per claimed chunk and period, in mangos.")
                    .defineInRange("price_per_chunk_mangos", 100L, 0L, Long.MAX_VALUE / 200L);
            rentGraceDays = builder
                    .comment("Days a team keeps its claims after a failed payment before they are removed.")
                    .defineInRange("grace_days", 1.0D, 0.0D, 365.0D);
            rentWarnDays = builder
                    .comment("Warn the team this many days before the rent is due.")
                    .defineInRange("warn_days", 1.0D, 0.0D, 365.0D);
            rentChargeMembers = builder
                    .comment("When the owner cannot pay alone, charge the accounts of the other members too.")
                    .define("charge_members", true);
            rentSkipServerTeams = builder
                    .comment("Never charge server teams.")
                    .define("skip_server_teams", true);
            rentCheckSeconds = builder
                    .comment("Seconds between rent checks.")
                    .defineInRange("check_interval_seconds", 60, 5, 3600);
            rentTitles = builder
                    .comment("Also show rent warnings, payments and evictions as a title on screen, not only in chat.")
                    .define("show_titles", true);
            rentBroadcastNotices = builder
                    .comment("Everyone online sees a team's rent warnings, failed payments and evictions, not only its members.")
                    .define("broadcast_notices", false);
            rentBroadcastPayments = builder
                    .comment("Everyone online sees when a team pays its rent, not only its members.")
                    .define("broadcast_payments", false);
            builder.pop();

            builder.push("team_bank");
            teamBankEnabled = builder
                    .comment("Give every FTB team a shared bank account, only its members can see and use it.")
                    .define("enabled", true);
            teamBankPartyOnly = builder
                    .comment("Only party teams get an account, not the personal team every player has.")
                    .define("party_only", true);
            teamBankWithdrawRank = builder
                    .comment("Lowest team rank allowed to take money out of the team account: MEMBER, OFFICER or OWNER.")
                    .define("withdraw_rank", "OFFICER", value -> value instanceof String text
                            && (text.equals("MEMBER") || text.equals("OFFICER") || text.equals("OWNER")));
            teamBankPaysRent = builder
                    .comment("Claim rent is charged to the team account first, then to the members.")
                    .define("pays_rent", true);
            builder.pop();

            builder.push("commands");
            rootCommandShortcuts = builder
                    .comment("Subcommands of /uma that also work without the /uma prefix (shop, jobs, money and daily). A name is skipped when another mod already has that command.",
                            "Leave it empty to keep every command under /uma and avoid clashes with other mods.")
                    .defineListAllowEmpty("root_shortcuts", List.of("shop", "jobs"), () -> "shop", value -> value instanceof String);
            builder.pop();

            builder.push("shop");
            shopEnabled = builder
                    .comment("Enable the shop screen. The exchange stand and the /uma shop command open the same one.")
                    .define("enabled", true);
            shopPermissionLevel = builder
                    .comment("Permission level needed to use /uma shop. 0 is everyone.")
                    .defineInRange("permission_level", 0, 0, 4);
            shopRequireStallNearby = builder
                    .comment("Only allow /uma shop within reach of an exchange stand.")
                    .define("require_stall_nearby", false);
            shopStallRadius = builder
                    .comment("How close an exchange stand has to be, in blocks.")
                    .defineInRange("stall_radius", 8, 1, 16);
            shopSellCommission = builder
                    .comment("Percent taken from every sale that is not marked as tax free.")
                    .defineInRange("sell_commission_percent", 3.0D, 0.0D, 100.0D);
            shopMaxAmount = builder
                    .comment("Most items one click can buy or sell.")
                    .defineInRange("max_amount", 2304, 1, 100000);
            shopAdminGui = builder
                    .comment("Players with the uma.shop.add or uma.shop.remove permission (LuckPerms, FTB Ranks or op level 2)",
                            "can add and remove zones and items from the shop screen.")
                    .define("admin_gui", true);
            shopInflationPercent = builder
                    .comment("Argentine inflation: every buy price of the shop (files, trades and generated items, but not coins and bills)",
                            "goes up by this percent over its base price. 200 means base price x 3. 0 turns it off.",
                            "Prices set by hand in the shop screen are not touched.")
                    .defineInRange("inflation_percent", 200.0D, 0.0D, 1000000.0D);
            shopInflationOnSell = builder
                    .comment("Also apply the inflation to the prices the shop pays when a player sells.")
                    .define("inflation_applies_to_sell", false);
            shopSellMultiplier = builder
                    .comment("Every sell price of the shop (but not coins and bills) is multiplied by this.")
                    .defineInRange("sell_multiplier", 1.0D, 0.0D, 100000.0D);
            shopGeneratedRequireRecipe = builder
                    .comment("Items added automatically (mod zones and vanilla fill) must have a recipe.")
                    .define("generated_require_recipe", true);
            shopGeneratedSkipCreative = builder
                    .comment("Items added automatically skip the ones that only exist in creative mode (operator blocks, spawn eggs).")
                    .define("generated_skip_creative", true);
            shopGeneratedIgnoredNamespaces = builder
                    .comment("Recipes whose id belongs to one of these namespaces do not count as a recipe, and the items of these namespaces",
                            "are not added automatically. The default leaves out everything that only exists because of KubeJS scripts.")
                    .defineListAllowEmpty("generated_ignored_namespaces", List.of("kubejs"),
                            () -> "kubejs", value -> value instanceof String);
            shopModZones = builder
                    .comment("Add one zone to the shop for every installed mod, priced from what its items cost to craft.",
                            "Off by default.")
                    .define("mod_zones", false);
            shopModZoneBlacklist = builder
                    .comment("Mod ids that never get a generated zone.")
                    .defineListAllowEmpty("mod_zone_blacklist", List.of("minecraft", "neoforge", "ultimatemodadditions", "ftblibrary"),
                            () -> "minecraft", value -> value instanceof String);
            shopModZoneFilters = builder
                    .comment("Limit a mod zone to the items whose id matches a regex. Format: modid=regex.",
                            "The default keeps FTB Quests to its loot crates, task screens and the quest book.")
                    .defineListAllowEmpty("mod_zone_filters", List.of("ftbquests=(loot|screen|book)"),
                            () -> "ftbquests=(loot|screen|book)", value -> value instanceof String text && text.contains("="));
            shopModZoneMarkup = builder
                    .comment("Buy price of a generated item is its crafting cost times this.")
                    .defineInRange("mod_zone_markup", 1.3D, 0.1D, 1000.0D);
            shopModZoneSellRatio = builder
                    .comment("Sell price of a generated item as a fraction of its buy price.")
                    .defineInRange("mod_zone_sell_ratio", 0.1D, 0.0D, 1.0D);
            shopModZoneMaxItems = builder
                    .comment("Most items one generated mod zone lists.")
                    .defineInRange("mod_zone_max_items", 400, 1, 5000);
            shopVanillaFill = builder
                    .comment("Every vanilla item that no shop file lists is added to the zone that fits it, priced from its recipe.")
                    .define("vanilla_fill", true);
            shopVanillaFillMarkup = builder
                    .comment("Buy price of an auto added vanilla item is its crafting cost times this.")
                    .defineInRange("vanilla_fill_markup", 1.2D, 0.1D, 1000.0D);
            shopVanillaFillSellRatio = builder
                    .comment("Sell price of an auto added vanilla item as a fraction of its buy price.")
                    .defineInRange("vanilla_fill_sell_ratio", 0.1D, 0.0D, 1.0D);
            builder.pop();

            builder.push("daily");
            dailyEnabled = builder
                    .comment("Enable the daily, weekly and monthly rewards and the /uma daily command.")
                    .define("enabled", true);
            dailyResetHour = builder
                    .comment("Hour of the real day (server time) at which the daily rewards reset.")
                    .defineInRange("reset_hour", 0, 0, 23);
            dailyPayTo = builder
                    .comment("Where the money of the rewards goes: ACCOUNT or CASH.")
                    .defineEnum("pay_to", PayTarget.ACCOUNT);
            dailyCommands = builder
                    .comment("Allow reward entries to run commands. %player% becomes the player name.")
                    .define("reward_commands", true);
            dailyNotify = builder
                    .comment("Tell players on login when they have a reward to claim.")
                    .define("notify_on_login", true);
            dailyModEntries = builder
                    .comment("Add one reward entry per installed mod to the daily, weekly and monthly lists.",
                            "Only items that have a recipe, are not creative or operator items, and cost less than the cap of the period are used.")
                    .define("mod_entries", true);
            dailyModBonus = builder
                    .comment("Every claim also gives a random item from an installed mod, picked from the shop mod zones.")
                    .define("mod_bonus", true);
            dailyModBonusChance = builder
                    .comment("Percent chance, per installed mod and per claim, of getting one of that mod's items.")
                    .defineInRange("mod_bonus_chance_percent", 1.0D, 0.0D, 100.0D);
            dailyModBonusDaily = builder
                    .comment("Highest shop buy price, in mangos, of the mod item a daily claim can give.")
                    .defineInRange("mod_bonus_daily_mangos", 30.0D, 0.0D, 1.0E9D);
            dailyModBonusWeekly = builder
                    .comment("Highest shop buy price, in mangos, of the mod item a weekly claim can give.")
                    .defineInRange("mod_bonus_weekly_mangos", 200.0D, 0.0D, 1.0E9D);
            dailyModBonusMonthly = builder
                    .comment("Highest shop buy price, in mangos, of the mod item a monthly claim can give.")
                    .defineInRange("mod_bonus_monthly_mangos", 1000.0D, 0.0D, 1.0E9D);
            builder.pop();

            builder.push("jobs");
            jobsEnabled = builder
                    .comment("Enable jobs and the /uma jobs command.")
                    .define("enabled", true);
            jobsMaxPerPlayer = builder
                    .comment("How many jobs one player can hold at once.")
                    .defineInRange("max_per_player", 1, 1, 64);
            jobsIncomeMultiplier = builder
                    .comment("Multiplies every job payment.")
                    .defineInRange("income_multiplier", 1.0D, 0.0D, 1000.0D);
            jobsXpMultiplier = builder
                    .comment("Multiplies every job experience gain.")
                    .defineInRange("xp_multiplier", 1.0D, 0.0D, 1000.0D);
            jobsXpNeededMultiplier = builder
                    .comment("Multiplies the experience every job level needs. Higher makes leveling slower.")
                    .defineInRange("xp_needed_multiplier", 1.0D, 0.01D, 1000.0D);
            jobsPayTo = builder
                    .comment("ACCOUNT pays into the bank account, CASH hands out loose bills and coins.")
                    .defineEnum("pay_to", PayTarget.ACCOUNT);
            jobsShowIncome = builder
                    .comment("Show each payment above the hotbar.")
                    .define("show_income", true);
            jobsIgnoreCreative = builder
                    .comment("Creative players earn nothing.")
                    .define("ignore_creative", true);
            jobsIgnoreSpawnerMobs = builder
                    .comment("Mobs that came out of a spawner pay nothing.")
                    .define("ignore_spawner_mobs", false);
            jobsDailyLimit = builder
                    .comment("Most mangos one job can pay a player per real day. 0 keeps it unlimited.")
                    .defineInRange("daily_income_limit_mangos", 0.0D, 0.0D, 1.0E12D);
            jobsTopSize = builder
                    .comment("How many players /uma jobs top lists.")
                    .defineInRange("top_size", 10, 3, 50);
            jobsExploreLimit = builder
                    .comment("Most chunks one player can be paid for exploring.")
                    .defineInRange("explore_chunk_limit", 20000, 0, 1000000);
            jobsProgressAlerts = builder
                    .comment("Percent of the current level at which the player gets a progress alert. Empty turns it off.")
                    .defineListAllowEmpty("progress_alerts", List.of(25, 50, 75), () -> 50,
                            value -> value instanceof Integer number && number > 0 && number < 100);
            jobsRewardCommands = builder
                    .comment("Allow the level rewards of a job to run commands. %player% becomes the player name.")
                    .define("reward_commands", true);
            builder.pop();

            builder.push("villager");
            flasheroEnabled = builder
                    .comment("Enable the money changer villager profession.")
                    .define("enabled", true);
            flasheroName = builder
                    .comment("Name given to every villager with this profession.")
                    .define("name", "El Tío To Flashero");
            flasheroScale = builder
                    .comment("Size of the money changer villager. 1.0 is a normal villager.")
                    .defineInRange("scale", 1.3D, 0.25D, 4.0D);
            flasheroMaxUses = builder.defineInRange("max_uses", 16, 1, 9999);
            flasheroXp = builder.defineInRange("villager_xp", 2, 0, 1000);
            flasheroTrades = builder
                    .comment("Trades as '<count>x<id>=<count>x<id>': the villager takes the left side and gives the right.",
                            "Ids are denomination ids such as 5_pe or 50_centavos. Invalid entries are skipped with a warning.")
                    .defineListAllowEmpty("trades", DEFAULT_FLASHERO_TRADES, () -> "1x5_pe=1x5_mangos",
                            value -> value instanceof String);
            builder.pop();

            builder.push("loot_bags");
            bagsEnabled = builder.define("enabled", true);
            bagsColiseumOnly = builder.define("only_in_coliseum", true);
            bagsOnlyFromPlayerKills = builder.define("only_from_player_kills", true);
            bagDropChance = builder
                    .comment("Chance a mob drops a loot bag directly, on top of buying one with coins.")
                    .defineInRange("drop_chance", 0.05D, 0.0D, 1.0D);
            builder.pop();


        }
    }

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    private UMAConfig() {
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }
}
