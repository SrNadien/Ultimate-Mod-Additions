package nadiendev.ultimatemodadditions.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public final class UMAConfig {

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
        public final ModConfigSpec.DoubleValue coinDropChance;
        public final ModConfigSpec.IntValue coinMin;
        public final ModConfigSpec.IntValue coinMax;
        public final ModConfigSpec.BooleanValue coinsOnlyFromPlayerKills;

        public final ModConfigSpec.BooleanValue bagsEnabled;
        public final ModConfigSpec.DoubleValue bagDropChance;
        public final ModConfigSpec.BooleanValue bagsColiseumOnly;




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
            coinsEnabled = builder.define("enabled", true);
            coinsColiseumOnly = builder.define("only_in_coliseum", true);
            coinDropChance = builder
                    .comment("Base chance a mob drops coins, before per-mob and difficulty modifiers.")
                    .defineInRange("drop_chance", 0.35D, 0.0D, 1.0D);
            coinMin = builder.defineInRange("min_per_kill", 1, 0, 4096);
            coinMax = builder.defineInRange("max_per_kill", 3, 0, 4096);
            coinsOnlyFromPlayerKills = builder.define("only_from_player_kills", true);
            builder.pop();

            builder.push("loot_bags");
            bagsEnabled = builder.define("enabled", true);
            bagsColiseumOnly = builder.define("only_in_coliseum", true);
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
