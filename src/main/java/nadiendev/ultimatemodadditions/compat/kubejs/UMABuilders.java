package nadiendev.ultimatemodadditions.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.resources.ResourceLocation;

public final class UMABuilders {

    private UMABuilders() {
    }

    public abstract static class Base {

        protected final JsonObject json = new JsonObject();
        private final ScriptedEntries.Kind kind;
        private final ResourceLocation id;

        protected Base(ScriptedEntries.Kind kind, String id) {
            this.kind = kind;
            this.id = UMA.parse(id);
        }

        protected JsonArray array(String key) {
            if (!json.has(key)) {
                json.add(key, new JsonArray());
            }
            return json.getAsJsonArray(key);
        }

        protected void strings(String key, String[] values) {
            JsonArray list = array(key);
            for (String value : values) {
                list.add(value);
            }
        }

        protected static JsonElement count(int min, int max) {
            if (min == max) {
                return new com.google.gson.JsonPrimitive(min);
            }
            JsonObject range = new JsonObject();
            range.addProperty("min", min);
            range.addProperty("max", max);
            return range;
        }

        protected static JsonObject drop(String item, int min, int max, float chance, int weight) {
            JsonObject entry = new JsonObject();
            entry.addProperty("item", item);
            entry.add("count", count(min, max));
            if (chance < 1.0F) {
                entry.addProperty("chance", chance);
            }
            if (weight > 0) {
                entry.addProperty("weight", weight);
            }
            return entry;
        }

        protected JsonObject convert(Context cx, Object value) {
            JsonElement element = JsonUtils.of(cx, value);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();
        }

        /** Guarda la entrada. Sin esto no pasa nada. */
        public void register() {
            ScriptedEntries.add(kind, id, json);
        }
    }

    public static final class LootBag extends Base {

        public LootBag(String id) {
            super(ScriptedEntries.Kind.LOOT_BAG, id);
        }

        public LootBag name(String value) {
            json.addProperty("name", value);
            return this;
        }

        public LootBag tier(String value) {
            json.addProperty("tier", value);
            return this;
        }

        public LootBag color(int value) {
            json.addProperty("color", value);
            return this;
        }

        public LootBag rolls(int amount) {
            json.add("rolls", count(amount, amount));
            return this;
        }

        public LootBag rolls(int min, int max) {
            json.add("rolls", count(min, max));
            return this;
        }

        public LootBag uniqueRolls(boolean value) {
            json.addProperty("unique_rolls", value);
            return this;
        }

        public LootBag announce(boolean value) {
            json.addProperty("announce", value);
            return this;
        }

        public LootBag openSound(String value) {
            json.addProperty("open_sound", value);
            return this;
        }

        public LootBag sortOrder(int value) {
            json.addProperty("sort_order", value);
            return this;
        }

        public LootBag guaranteed(String item, int min, int max) {
            array("guaranteed").add(drop(item, min, max, 1.0F, 0));
            return this;
        }

        public LootBag guaranteed(String item, int amount) {
            return guaranteed(item, amount, amount);
        }

        public LootBag guaranteed(String item) {
            return guaranteed(item, 1, 1);
        }

        public LootBag entry(String item, int weight) {
            array("entries").add(drop(item, 1, 1, 1.0F, weight));
            return this;
        }

        public LootBag entry(String item, int min, int max, int weight) {
            array("entries").add(drop(item, min, max, 1.0F, weight));
            return this;
        }

        public LootBag bagEntry(String bag, int weight) {
            JsonObject entry = new JsonObject();
            entry.addProperty("bag", bag);
            entry.addProperty("weight", weight);
            array("entries").add(entry);
            return this;
        }

        public LootBag lootTableEntry(String table, float chance, int weight) {
            JsonObject entry = new JsonObject();
            entry.addProperty("loot_table", table);
            entry.addProperty("chance", chance);
            entry.addProperty("weight", weight);
            array("entries").add(entry);
            return this;
        }

        public LootBag rawEntry(Context cx, Object value) {
            array("entries").add(convert(cx, value));
            return this;
        }
    }

    public static final class BagDrop extends Base {

        public BagDrop(String id) {
            super(ScriptedEntries.Kind.BAG_ADDITION, id);
        }

        public BagDrop bag(String value) {
            json.addProperty("bag", value);
            return this;
        }

        public BagDrop bags(String... values) {
            strings("bags", values);
            return this;
        }

        public BagDrop tiers(String... values) {
            strings("tiers", values);
            return this;
        }

        public BagDrop allTiers() {
            return tiers("all");
        }

        public BagDrop replace(boolean value) {
            json.addProperty("replace", value);
            return this;
        }

        public BagDrop entry(String item, int weight) {
            array("entries").add(drop(item, 1, 1, 1.0F, weight));
            return this;
        }

        public BagDrop entry(String item, int min, int max, int weight) {
            array("entries").add(drop(item, min, max, 1.0F, weight));
            return this;
        }

        public BagDrop guaranteed(String item, int min, int max) {
            array("guaranteed").add(drop(item, min, max, 1.0F, 0));
            return this;
        }

        public BagDrop guaranteed(String item, int amount) {
            return guaranteed(item, amount, amount);
        }

        public BagDrop rawEntry(Context cx, Object value) {
            array("entries").add(convert(cx, value));
            return this;
        }
    }

    public static final class Trade extends Base {

        public Trade(String id) {
            super(ScriptedEntries.Kind.TRADE, id);
        }

        public Trade name(String value) {
            json.addProperty("name", value);
            return this;
        }

        public Trade category(String value) {
            json.addProperty("category", value);
            return this;
        }

        public Trade price(int value) {
            json.addProperty("price", value);
            return this;
        }

        public Trade cost(String item, int amount) {
            JsonObject entry = new JsonObject();
            entry.addProperty("item", item);
            entry.addProperty("count", amount);
            array("cost").add(entry);
            return this;
        }

        public Trade result(String item, int amount) {
            JsonObject result = new JsonObject();
            result.addProperty("item", item);
            result.addProperty("count", amount);
            json.add("result", result);
            return this;
        }

        public Trade result(String item) {
            return result(item, 1);
        }

        public Trade resultBag(String bag, int amount) {
            JsonObject result = new JsonObject();
            result.addProperty("bag", bag);
            result.addProperty("count", amount);
            json.add("result", result);
            return this;
        }

        public Trade resultBag(String bag) {
            return resultBag(bag, 1);
        }

        public Trade sortOrder(int value) {
            json.addProperty("sort_order", value);
            return this;
        }

        public Trade enabled(boolean value) {
            json.addProperty("enabled", value);
            return this;
        }
    }

    public static final class MobRule extends Base {

        public MobRule(String id) {
            super(ScriptedEntries.Kind.MOB_RULE, id);
        }

        private JsonObject match() {
            if (!json.has("match")) {
                json.add("match", new JsonObject());
            }
            return json.getAsJsonObject("match");
        }

        public MobRule priority(int value) {
            json.addProperty("priority", value);
            return this;
        }

        public MobRule entities(String... values) {
            JsonArray list = new JsonArray();
            for (String value : values) {
                list.add(value);
            }
            match().add("entities", list);
            return this;
        }

        public MobRule categories(String... values) {
            JsonArray list = new JsonArray();
            for (String value : values) {
                list.add(value);
            }
            match().add("categories", list);
            return this;
        }

        public MobRule boss(boolean value) {
            match().addProperty("boss", value);
            return this;
        }

        public MobRule rawMatch(Context cx, Object value) {
            json.add("match", convert(cx, value));
            return this;
        }

        public MobRule dimensions(String... values) {
            strings("dimensions", values);
            return this;
        }

        public MobRule difficulty(String value) {
            json.addProperty("difficulty", value);
            return this;
        }

        public MobRule replaceVanillaDrops(boolean value) {
            json.addProperty("replace_vanilla_drops", value);
            return this;
        }

        public MobRule experienceMultiplier(double value) {
            json.addProperty("experience_multiplier", value);
            return this;
        }

        public MobRule drop(String item, int min, int max, float chance) {
            array("drops").add(drop(item, min, max, chance, 0));
            return this;
        }

        public MobRule coin(String tier, int min, int max, float chance) {
            JsonObject entry = new JsonObject();
            entry.addProperty("tier", tier);
            entry.add("count", count(min, max));
            entry.addProperty("chance", chance);
            array("coins").add(entry);
            return this;
        }

        public MobRule bag(String bag, float chance) {
            JsonObject entry = new JsonObject();
            entry.addProperty("bag", bag);
            entry.addProperty("chance", chance);
            array("bags").add(entry);
            return this;
        }
    }

    public static final class Difficulty extends Base {

        public Difficulty(String id) {
            super(ScriptedEntries.Kind.DIFFICULTY, id);
        }

        public Difficulty name(String value) {
            json.addProperty("name", value);
            return this;
        }

        public Difficulty health(double value) {
            json.addProperty("health_multiplier", value);
            return this;
        }

        public Difficulty damage(double value) {
            json.addProperty("damage_multiplier", value);
            return this;
        }

        public Difficulty speed(double value) {
            json.addProperty("speed_multiplier", value);
            return this;
        }

        public Difficulty armor(double value) {
            json.addProperty("armor_bonus", value);
            return this;
        }

        public Difficulty armorToughness(double value) {
            json.addProperty("armor_toughness_bonus", value);
            return this;
        }

        public Difficulty knockbackResistance(double value) {
            json.addProperty("knockback_resistance", value);
            return this;
        }

        public Difficulty followRange(double value) {
            json.addProperty("follow_range_bonus", value);
            return this;
        }

        public Difficulty experience(double value) {
            json.addProperty("experience_multiplier", value);
            return this;
        }

        public Difficulty coins(double value) {
            json.addProperty("coin_multiplier", value);
            return this;
        }

        public Difficulty bagChance(double value) {
            json.addProperty("bag_chance_bonus", value);
            return this;
        }

        public Difficulty extraRolls(int value) {
            json.addProperty("extra_loot_rolls", value);
            return this;
        }

        public Difficulty glowing(boolean value) {
            json.addProperty("glowing", value);
            return this;
        }

        public Difficulty effect(String effect, int amplifier, int duration, float chance) {
            JsonObject entry = new JsonObject();
            entry.addProperty("effect", effect);
            entry.addProperty("amplifier", amplifier);
            entry.addProperty("duration", duration);
            entry.addProperty("chance", chance);
            array("effects").add(entry);
            return this;
        }

        public Difficulty equipment(String slot, String item, float chance, float dropChance) {
            if (!json.has("equipment")) {
                json.add("equipment", new JsonObject());
            }
            JsonObject entry = new JsonObject();
            entry.addProperty("item", item);
            entry.addProperty("chance", chance);
            entry.addProperty("drop_chance", dropChance);
            json.getAsJsonObject("equipment").add(slot, entry);
            return this;
        }
    }

}
