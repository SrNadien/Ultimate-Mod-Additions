package nadiendev.ultimatemodadditions.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.ban.PackBans;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import nadiendev.ultimatemodadditions.config.JsonValues;

import java.util.List;
import java.util.function.Supplier;

public final class EventDefaults {

    private static final String HEADER = """
            // Ultimate Mod Additions - JSON5 file. Comments, unquoted keys and trailing commas are allowed.
            // The file is read again every time it changes, no restart needed. KubeJS can override any value with UMA.config('%s', {...}).
            // Archivo JSON5 de Ultimate Mod Additions: se admiten comentarios, claves sin comillas y comas finales.
            // Se vuelve a leer solo cuando cambia. KubeJS puede pisar cualquier valor con UMA.config('%s', {...}).
            %s
            """;

    public static final EventConfigs.Spec BEE = spec("bee", """
            // wait_ticks: ticks between two drops. rounds_min/rounds_max: how many drops a bee makes.
            // items: pool of every drop ("2x mod:item" drops two, "mod:item[components]" keeps components, repeats weigh more).
            // rare_items: pool used with probability rare_chance (0 to 1). excluded: ids that never drop.
            // coins: when the bee leaves it drops between min and max of these items. manual_drops: extra drops, one roll each.
            // only_manual: true makes the bee drop only manual_drops and coins.
            // spawn: the bee also shows up by itself near players; cooldown is per player.
            """, EventDefaults::bee);

    public static final EventConfigs.Spec BOSS = spec("periodic_boss", """
            // A boss (vanilla or modded) appears every so often near a random player, boosted x5, x10 or x20, with unique abilities.
            // chance: probability per check (check_ticks). min_days_between: in-game days between two bosses.
            // boss_tags and extra_bosses build the pool of entities; excluded_bosses never appear.
            // abilities_per_level: how many abilities a boss of that level gets. Each ability has enabled and every_ticks.
            // rewards works like the charged bee, with levels in manual_drops (["5","10","20"], empty means all).
            """, EventDefaults::boss);

    public static final EventConfigs.Spec NPC = spec("npc_missions", """
            // NPCs with delivery missions appear in front of players. Each level has a weight (chance), the items they can ask for
            // (deliveries: [item, min, max]), the prizes (prizes: [item, min, max], prize_count different ones) and experience.
            // Items of mods that are not installed are skipped. mobs: entity ids used as the NPC body (missing ones are skipped).
            """, EventDefaults::npc);

    public static final EventConfigs.Spec WELCOME = spec("welcome", """
            // starting_money: mangos paid into the bank account the first time a player joins (change key to pay everyone again).
            // quest_notice: reminder to open the quest book. Empty texts use the translated defaults of the mod.
            """, EventDefaults::welcome);

    public static final EventConfigs.Spec BANS = new EventConfigs.Spec("bans", """
            // Ultimate Mod Additions - item bans (YAML). Read again every time the file changes, no restart needed.
            // Archivo de baneo de items (YAML). Se vuelve a leer solo cuando cambia, sin reiniciar.
            // KubeJS can add bans with UMA.ban(action, item, message) and override values with UMA.config('bans', {...}).
            //
            // banned / disabled: simple lists, every action in every dimension. Shown as BANNED / DISABLED.
            // Ids of mods that are not installed are skipped without writing anything to the console.
            //
            // blacklist: per dimension, item and action (same idea as the BanItem plugin):
            //   blacklist:
            //     '*, !minecraft:the_end':             dimensions: one, 'a, b', '*' or '*, !x' ('#regex' too)
            //       'minecraft:ender_pearl, #_bed$':   items: ids, '*', '*, !x', '#regex' or names from custom_items
            //         use: 'Only in the end'           action: message (text or list; & color codes; {player} {item} {dimension} {time})
            //         'place, break':                  actions: one, 'a, b', '*' or '*, !x'
            //           message: ['line 1', 'line 2']
            //           gamemode: creative             only for these game modes
            //           cooldown: 30000                milliseconds: allowed once, banned until the time passes
            //           bypass_level: 2                players with this permission level or more are not affected
            //           log: true                      staff using /uma ban watch get a notice
            //           run: ['say {player} tried {item}']   commands run by the server
            //           entity: ['minecraft:pig']      attack, entityinteract and entitydrop: only for these entities
            //           material: ['minecraft:diamond_pickaxe']   break, interact, click and use: the other item involved
            //
            // whitelist: everything is banned in these dimensions except what is listed:
            //   whitelist:
            //     'minecraft:the_end':
            //       message: 'Only dirt can be placed here'
            //       ignored: interact                  actions that are never banned here
            //       'minecraft:dirt': place
            //
            // custom_items: named items to use in the lists above:
            //   custom_items:
            //     sharp_sword:
            //       item: 'minecraft:diamond_sword'    (it can carry components: 'minecraft:potion[...]')
            //       enchantment_contains: 'minecraft:sharpness:5'     (id, id:level or id:min:max)
            //       display_name_contains: 'Excalibur'                (display_name_equals too)
            //       lore_contains: ['Damage']                         (lore_equals, lore_contains_regex too)
            //       durability: '0-300'
            //
            // Actions: craft smelt smith pickup drop equip place break use click interact consume attack
            //          entityinteract entitydrop hold swap delete rename armorstand
            // Who is not affected: operators when ops_bypass is true, players with the permission uma.ban.bypass
            // or uma.ban.bypass.<action>, and players with a rule's bypass_level.
            // Operators also add rules while playing with /uma ban add; those live in the world, not in this file.
            """, EventDefaults::bans, true);

    private EventDefaults() {
    }

    private static EventConfigs.Spec spec(String name, String notes, Supplier<JsonObject> defaults) {
        return spec(name, notes, defaults, false);
    }

    private static EventConfigs.Spec spec(String name, String notes, Supplier<JsonObject> defaults, boolean yaml) {
        return new EventConfigs.Spec(name, HEADER.formatted(name, name, notes.stripTrailing()), defaults, yaml);
    }

    private static JsonObject bee() {
        JsonObject root = new JsonObject();
        root.addProperty("summoner_item", "minecraft:honeycomb_block");
        root.addProperty("summon_needs_sneak", true);
        root.addProperty("name", "");
        root.addProperty("wait_ticks", 60);
        root.addProperty("rounds_min", 5);
        root.addProperty("rounds_max", 12);
        root.addProperty("announce", true);
        rewards(root, 0.01D);
        JsonObject spawn = new JsonObject();
        spawn.addProperty("enabled", true);
        spawn.addProperty("check_ticks", 1200);
        spawn.addProperty("chance", 0.15D);
        spawn.addProperty("player_cooldown_ticks", 12000);
        spawn.addProperty("min_distance", 4);
        spawn.addProperty("max_distance", 10);
        spawn.addProperty("attempts", 30);
        root.add("spawn", spawn);
        return root;
    }

    private static JsonObject boss() {
        JsonObject root = new JsonObject();
        root.addProperty("check_ticks", 100);
        root.addProperty("chance", 0.001D);
        root.addProperty("min_days_between", 20);
        root.addProperty("first_day", 20);
        root.addProperty("radius_min", 120);
        root.addProperty("radius_max", 300);
        root.addProperty("max_health_cap", 1024);
        root.addProperty("attack_per_level", 4.0D);
        root.addProperty("announce_title", true);
        JsonArray levels = new JsonArray();
        levels.add(5);
        levels.add(10);
        levels.add(20);
        root.add("levels", levels);
        JsonObject perLevel = new JsonObject();
        perLevel.addProperty("5", 1);
        perLevel.addProperty("10", 2);
        perLevel.addProperty("20", 3);
        root.add("abilities_per_level", perLevel);
        root.add("boss_tags", JsonValues.array(List.of("c:bosses", "twilightforest:bosses")));
        root.add("extra_bosses", JsonValues.array(List.of("minecraft:wither", "minecraft:warden", "minecraft:elder_guardian")));
        root.add("excluded_bosses", JsonValues.array(List.of("minecraft:ender_dragon")));
        JsonObject abilities = new JsonObject();
        ability(abilities, "lightning", 160);
        ability(abilities, "minions", 300);
        ability(abilities, "shockwave", 160);
        ability(abilities, "regeneration", 100);
        ability(abilities, "shield", 400);
        ability(abilities, "fire_rain", 120);
        ability(abilities, "wither_aura", 60);
        root.add("abilities", abilities);
        JsonObject rewards = new JsonObject();
        JsonObject tries = new JsonObject();
        tries.addProperty("5", 1);
        tries.addProperty("10", 1);
        tries.addProperty("20", 1);
        rewards.add("tries", tries);
        rewards(rewards, 0.01D);
        root.add("rewards", rewards);
        return root;
    }

    private static void ability(JsonObject abilities, String name, int every) {
        JsonObject ability = new JsonObject();
        ability.addProperty("enabled", true);
        ability.addProperty("every_ticks", every);
        abilities.add(name, ability);
    }

    private static void rewards(JsonObject target, double rareChance) {
        target.addProperty("rare_chance", rareChance);
        target.add("items", JsonValues.array(PackLoot.ITEMS));
        target.add("rare_items", JsonValues.array(PackLoot.RARE));
        target.add("excluded", new JsonArray());
        JsonObject coins = new JsonObject();
        coins.add("items", JsonValues.array(PackLoot.COINS));
        coins.addProperty("min", 1);
        coins.addProperty("max", 5);
        target.add("coins", coins);
        target.add("manual_drops", new JsonArray());
        target.addProperty("only_manual", false);
    }

    private static JsonObject npc() {
        JsonObject root = new JsonObject();
        root.addProperty("spawn_period_ticks", 6000);
        root.addProperty("search_radius", 48);
        root.addProperty("distance_min", 6);
        root.addProperty("distance_max", 10);
        root.add("dimensions", JsonValues.array(List.of("minecraft:overworld")));
        root.add("mobs", JsonValues.array(List.of("minecraft:villager", "minecraft:witch", "minecraft:evoker",
                "minecraft:illusioner", "minecraft:vindicator", "minecraft:pillager")));
        root.add("names", JsonValues.array(List.of("Aldric", "Brienne", "Cael", "Dara", "Eron", "Fenna", "Garrick", "Hilda",
                "Ivor", "Jora", "Kellan", "Lyra", "Mordo", "Nessa", "Orin", "Petra")));
        JsonObject levels = new JsonObject();
        levels.add("easy", level("Fácil", "green", 50, 1, 50, 120,
                PackLoot.EASY_DELIVERIES, PackLoot.EASY_PRIZES));
        levels.add("hard", level("Difícil", "gold", 30, 1, 150, 300,
                PackLoot.HARD_DELIVERIES, PackLoot.HARD_PRIZES));
        levels.add("ultra", level("Ultra difícil", "red", 15, 2, 400, 700,
                PackLoot.ULTRA_DELIVERIES, PackLoot.ULTRA_PRIZES));
        levels.add("hardcore", level("Hardcore", "dark_red", 5, 3, 1000, 1500,
                PackLoot.HARDCORE_DELIVERIES, PackLoot.HARDCORE_PRIZES));
        root.add("levels", levels);
        return root;
    }

    private static JsonObject level(String name, String color, int weight, int prizeCount, int xpMin, int xpMax,
                                    Object[][] deliveries, Object[][] prizes) {
        JsonObject level = new JsonObject();
        level.addProperty("name", name);
        level.addProperty("color", color);
        level.addProperty("weight", weight);
        level.addProperty("prize_count", prizeCount);
        level.add("xp", JsonValues.range(xpMin, xpMax));
        level.add("deliveries", rows(deliveries));
        level.add("prizes", rows(prizes));
        return level;
    }

    private static JsonArray rows(Object[][] values) {
        JsonArray rows = new JsonArray();
        for (Object[] value : values) {
            JsonArray row = new JsonArray();
            row.add((String) value[0]);
            row.add((Integer) value[1]);
            row.add((Integer) value[2]);
            rows.add(row);
        }
        return rows;
    }

    private static JsonObject welcome() {
        JsonObject root = new JsonObject();
        JsonObject money = new JsonObject();
        money.addProperty("enabled", true);
        money.addProperty("mangos", 2500);
        money.addProperty("key", "starting_money_1");
        money.addProperty("delay_ticks", 160);
        money.addProperty("message", "");
        root.add("starting_money", money);
        JsonObject quests = new JsonObject();
        quests.addProperty("enabled", true);
        quests.addProperty("only_with_ftb_quests", true);
        quests.addProperty("key", "quest_notice_1");
        quests.addProperty("delay_ticks", 200);
        quests.addProperty("chat_message", "");
        quests.addProperty("title", "");
        quests.addProperty("subtitle", "");
        quests.addProperty("title_first_join_only", true);
        root.add("quest_notice", quests);
        return root;
    }

    private static JsonObject bans() {
        JsonObject root = new JsonObject();
        root.addProperty("enabled", true);
        root.addProperty("ops_bypass", true);
        root.addProperty("remove_recipes", true);
        root.addProperty("scan_interval_ticks", 10);
        root.addProperty("notify_cooldown_ticks", 40);
        root.addProperty("log_all", false);
        JsonObject feedback = new JsonObject();
        feedback.addProperty("sound", "minecraft:entity.villager.no");
        feedback.addProperty("volume", 1.0D);
        feedback.addProperty("pitch", 1.0D);
        feedback.addProperty("particle", "");
        feedback.addProperty("particle_count", 0);
        root.add("feedback", feedback);
        root.add("banned", JsonValues.array(PackBans.BANNED));
        root.add("disabled", JsonValues.array(PackBans.DISABLED));
        root.add("rules", new JsonArray());
        root.add("blacklist", new JsonObject());
        root.add("whitelist", new JsonObject());
        root.add("custom_items", new JsonObject());
        return root;
    }
}
