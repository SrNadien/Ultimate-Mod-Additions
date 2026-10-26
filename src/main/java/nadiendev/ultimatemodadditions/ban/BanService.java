package nadiendev.ultimatemodadditions.ban;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import nadiendev.ultimatemodadditions.config.JsonValues;
import nadiendev.ultimatemodadditions.config.ScriptedConfigs;
import nadiendev.ultimatemodadditions.event.EventDefaults;
import nadiendev.ultimatemodadditions.event.ItemSpec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BanService {

    public record Denial(BanRule rule, long remaining, List<String> messages) {
    }

    public record Check(ServerPlayer player, ItemStack stack, BanAction action, ItemStack other, String entity) {

        public Check(ServerPlayer player, ItemStack stack, BanAction action) {
            this(player, stack, action, ItemStack.EMPTY, null);
        }
    }

    private record Scope(Selector dimensions, RuleData data, Set<BanAction> ignored, List<BanRule> allowed) {
    }

    private record Snapshot(List<BanRule> all, Map<Item, List<BanRule>> exact, List<BanRule> general, List<Scope> whitelists) {
    }

    private static Snapshot current = new Snapshot(List.of(), Map.of(), List.of(), List.of());
    private static JsonObject lastConfig;
    private static int lastScripted = -1;
    private static int lastLive = -1;
    private static MinecraftServer lastServer;
    private static JsonObject appliedConfig;
    private static int appliedScripted = -1;
    private static int appliedLive = -1;
    private static final Map<String, Long> COOLDOWNS = new HashMap<>();

    private BanService() {
    }

    public static JsonObject config() {
        return EventConfigs.get(EventDefaults.BANS);
    }

    public static boolean enabled() {
        return JsonValues.bool(config(), "enabled", true);
    }

    public static synchronized void markApplied(MinecraftServer server) {
        appliedConfig = config();
        appliedScripted = ScriptedConfigs.version();
        appliedLive = BanState.get(server).version();
    }

    public static synchronized boolean consumeChange(MinecraftServer server) {
        JsonObject config = config();
        int live = BanState.get(server).version();
        boolean changed = config != appliedConfig || ScriptedConfigs.version() != appliedScripted || live != appliedLive;
        appliedConfig = config;
        appliedScripted = ScriptedConfigs.version();
        appliedLive = live;
        return changed;
    }

    public static synchronized void invalidate() {
        lastConfig = null;
    }

    private static synchronized Snapshot snapshot(MinecraftServer server) {
        JsonObject config = config();
        BanState state = BanState.get(server);
        if (config != lastConfig || ScriptedConfigs.version() != lastScripted || state.version() != lastLive
                || server != lastServer) {
            current = build(server.registryAccess(), config, state);
            lastConfig = config;
            lastScripted = ScriptedConfigs.version();
            lastLive = state.version();
            lastServer = server;
        }
        return current;
    }

    public static List<BanRule> rules(MinecraftServer server) {
        return snapshot(server).all();
    }

    public static Map<Item, Boolean> listedItems() {
        Map<Item, Boolean> listed = new HashMap<>();
        if (!enabled()) {
            return listed;
        }
        JsonObject config = config();
        for (String id : JsonValues.strings(config, "disabled")) {
            Item item = ItemSpec.item(id);
            if (item != null) {
                listed.put(item, true);
            }
        }
        for (String id : JsonValues.strings(config, "banned")) {
            Item item = ItemSpec.item(id);
            if (item != null) {
                listed.put(item, false);
            }
        }
        return listed;
    }

    public static boolean banned(Item item) {
        return enabled() && current.exact().containsKey(item);
    }

    public static List<BanRule> rulesFor(MinecraftServer server, ItemStack stack) {
        Snapshot snapshot = snapshot(server);
        List<BanRule> found = new ArrayList<>();
        for (BanRule rule : candidates(snapshot, stack.getItem())) {
            if (rule.target().matches(stack)) {
                found.add(rule);
            }
        }
        return found;
    }

    private static List<BanRule> candidates(Snapshot snapshot, Item item) {
        List<BanRule> exact = snapshot.exact().get(item);
        if (exact == null) {
            return snapshot.general();
        }
        if (snapshot.general().isEmpty()) {
            return exact;
        }
        List<BanRule> both = new ArrayList<>(exact);
        both.addAll(snapshot.general());
        return both;
    }

    public static Denial deny(Check check) {
        ServerPlayer player = check.player();
        ItemStack stack = check.stack();
        if (stack.isEmpty() || !enabled()) {
            return null;
        }
        Snapshot snapshot = snapshot(player.server);
        if (snapshot.all().isEmpty() && snapshot.whitelists().isEmpty()) {
            return null;
        }
        String dimension = player.level().dimension().location().toString();
        String mode = player.gameMode.getGameModeForPlayer().getName();
        Denial denial = null;
        for (BanRule rule : candidates(snapshot, stack.getItem())) {
            if (!rule.applies(check.action(), dimension) || !rule.target().matches(stack) || !dataMatches(rule.data(), check, mode)) {
                continue;
            }
            if (rule.data().bypassLevel() >= 0 && player.hasPermissions(rule.data().bypassLevel())) {
                continue;
            }
            long remaining = cooldown(player, rule);
            if (remaining < 0L) {
                continue;
            }
            denial = new Denial(rule, remaining, rule.data().messages());
            break;
        }
        if (denial == null) {
            for (Scope scope : snapshot.whitelists()) {
                if (!scope.dimensions().matches(dimension) || scope.ignored().contains(check.action())) {
                    continue;
                }
                boolean allowed = false;
                for (BanRule rule : scope.allowed()) {
                    if (rule.actions().contains(check.action()) && rule.target().matches(stack)) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) {
                    BanRule synthetic = new BanRule(ItemTarget.exact(stack.getItem()), EnumSet.of(check.action()), scope.dimensions(),
                            scope.data(), false, false, "whitelist");
                    denial = new Denial(synthetic, 0L, scope.data().messages());
                    break;
                }
            }
        }
        if (denial == null || BanPermissions.bypasses(player, check.action())) {
            return null;
        }
        return denial;
    }

    private static boolean dataMatches(RuleData data, Check check, String gameMode) {
        if (!data.gamemodes().isEmpty() && !data.gamemodes().contains(gameMode)) {
            return false;
        }
        if (!data.entities().isEmpty() && (check.entity() == null || !data.entities().contains(check.entity()))) {
            return false;
        }
        if (data.materials() != null) {
            if (check.other().isEmpty()) {
                return false;
            }
            return data.materials().matches(idOf(check.other()));
        }
        return true;
    }

    public static boolean deniesDrop(MinecraftServer server, String dimension, ItemStack stack, String entity) {
        if (!enabled() || stack.isEmpty()) {
            return false;
        }
        for (BanRule rule : candidates(snapshot(server), stack.getItem())) {
            Set<String> entities = rule.data().entities();
            if (rule.applies(BanAction.ENTITY_DROP, dimension) && rule.target().matches(stack)
                    && (entities.isEmpty() || entities.contains(entity))) {
                return true;
            }
        }
        return false;
    }

    public static String idOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static synchronized long cooldown(ServerPlayer player, BanRule rule) {
        long wait = rule.data().cooldown();
        if (wait <= 0L) {
            return 0L;
        }
        String key = player.getUUID() + ":" + System.identityHashCode(rule);
        long now = System.currentTimeMillis();
        Long last = COOLDOWNS.get(key);
        if (last == null || now - last >= wait) {
            COOLDOWNS.put(key, now);
            return -1L;
        }
        return wait - (now - last);
    }

    public static synchronized void forget(UUID player) {
        COOLDOWNS.keySet().removeIf(key -> key.startsWith(player.toString()));
    }

    private static Snapshot build(HolderLookup.Provider registries, JsonObject config, BanState state) {
        List<BanRule> all = new ArrayList<>();
        List<Scope> whitelists = new ArrayList<>();
        if (!JsonValues.bool(config, "enabled", true)) {
            return new Snapshot(all, Map.of(), List.of(), whitelists);
        }
        Map<String, CustomItem> customs = CustomItem.parseAll(JsonValues.object(config, "custom_items"), registries);
        Selector everywhere = Selector.everything();
        for (String id : JsonValues.strings(config, "disabled")) {
            all.add(new BanRule(ItemTarget.parse(id, customs, registries), EnumSet.allOf(BanAction.class), everywhere,
                    RuleData.EMPTY, true, false, "file"));
        }
        for (String id : JsonValues.strings(config, "banned")) {
            all.add(new BanRule(ItemTarget.parse(id, customs, registries), EnumSet.allOf(BanAction.class), everywhere,
                    RuleData.EMPTY, false, false, "file"));
        }
        for (JsonObject rule : JsonValues.objects(config, "rules")) {
            List<String> names = new ArrayList<>(JsonValues.strings(rule, "actions"));
            names.addAll(JsonValues.strings(rule, "modes"));
            Set<BanAction> actions = names.isEmpty() ? EnumSet.allOf(BanAction.class) : BanAction.parse(String.join(",", names));
            if (actions == null || actions.isEmpty()) {
                continue;
            }
            String dimensions = String.join(",", JsonValues.strings(rule, "dimensions"));
            String message = JsonValues.string(rule, "message", "");
            all.add(new BanRule(ItemTarget.parse(JsonValues.string(rule, "item", ""), customs, registries), actions,
                    dimensions.isEmpty() ? everywhere : Selector.parse(dimensions, true),
                    message.isEmpty() ? RuleData.EMPTY : RuleData.of(List.of(message)), false, false, "file"));
        }
        blacklist(all, JsonValues.object(config, "blacklist"), customs, registries);
        whitelist(whitelists, JsonValues.object(config, "whitelist"), customs, registries);
        for (Map.Entry<String, List<String>> scripted : ScriptedConfigs.bans().entrySet()) {
            Set<BanAction> actions = BanAction.parse(scripted.getKey());
            if (actions == null) {
                continue;
            }
            for (String entry : scripted.getValue()) {
                int split = entry.indexOf('|');
                String id = split < 0 ? entry : entry.substring(0, split);
                String message = split < 0 ? "" : entry.substring(split + 1);
                all.add(new BanRule(ItemTarget.parse(id, customs, registries), actions, everywhere,
                        message.isEmpty() ? RuleData.EMPTY : RuleData.of(List.of(message)), false, false, "kubejs"));
            }
        }
        for (BanState.Entry entry : state.entries()) {
            all.add(new BanRule(ItemTarget.parse(entry.item(), customs, registries), BanAction.fromMask(entry.actions()),
                    entry.dimensions().isBlank() ? everywhere : Selector.parse(entry.dimensions(), true),
                    entry.message().isEmpty() ? RuleData.EMPTY : RuleData.of(List.of(entry.message())), entry.disabled(), true, "live"));
        }
        Map<Item, List<BanRule>> exact = new HashMap<>();
        List<BanRule> general = new ArrayList<>();
        for (BanRule rule : all) {
            Set<Item> items = rule.target().exactItems();
            if (items == null) {
                general.add(rule);
            } else {
                items.forEach(item -> exact.computeIfAbsent(item, key -> new ArrayList<>()).add(rule));
            }
        }
        return new Snapshot(all, exact, general, whitelists);
    }

    private static void blacklist(List<BanRule> target, JsonObject tree, Map<String, CustomItem> customs,
                                  HolderLookup.Provider registries) {
        for (Map.Entry<String, JsonElement> scope : tree.entrySet()) {
            if (!scope.getValue().isJsonObject()) {
                continue;
            }
            Selector dimensions = Selector.parse(scope.getKey(), true);
            for (Map.Entry<String, JsonElement> item : scope.getValue().getAsJsonObject().entrySet()) {
                ItemTarget itemTarget = ItemTarget.parse(item.getKey(), customs, registries);
                JsonElement value = item.getValue();
                if (value.isJsonPrimitive()) {
                    Set<BanAction> actions = BanAction.parse(value.getAsString());
                    if (actions != null) {
                        target.add(new BanRule(itemTarget, actions, dimensions, RuleData.EMPTY, false, false, "file"));
                    }
                } else if (value.isJsonObject()) {
                    for (Map.Entry<String, JsonElement> action : value.getAsJsonObject().entrySet()) {
                        Set<BanAction> actions = BanAction.parse(action.getKey());
                        if (actions != null) {
                            target.add(new BanRule(itemTarget, actions, dimensions, RuleData.parse(action.getValue()), false,
                                    false, "file"));
                        }
                    }
                }
            }
        }
    }

    private static void whitelist(List<Scope> target, JsonObject tree, Map<String, CustomItem> customs,
                                  HolderLookup.Provider registries) {
        for (Map.Entry<String, JsonElement> scope : tree.entrySet()) {
            if (!scope.getValue().isJsonObject()) {
                continue;
            }
            JsonObject body = scope.getValue().getAsJsonObject();
            Set<BanAction> ignored = EnumSet.noneOf(BanAction.class);
            JsonElement ignoredElement = body.get("ignored");
            if (ignoredElement != null) {
                List<String> names = new ArrayList<>();
                if (ignoredElement.isJsonArray()) {
                    ignoredElement.getAsJsonArray().forEach(entry -> names.add(entry.getAsString()));
                } else {
                    names.add(ignoredElement.getAsString());
                }
                Set<BanAction> parsed = BanAction.parse(String.join(",", names));
                if (parsed != null) {
                    ignored.addAll(parsed);
                }
            }
            List<BanRule> allowed = new ArrayList<>();
            Selector dimensions = Selector.parse(scope.getKey(), true);
            for (Map.Entry<String, JsonElement> item : body.entrySet()) {
                if (item.getKey().equals("message") || item.getKey().equals("ignored")) {
                    continue;
                }
                Set<BanAction> actions = new HashSet<>();
                JsonElement value = item.getValue();
                if (value.isJsonPrimitive()) {
                    Set<BanAction> parsed = BanAction.parse(value.getAsString());
                    if (parsed != null) {
                        actions.addAll(parsed);
                    }
                } else if (value.isJsonObject()) {
                    for (String key : value.getAsJsonObject().keySet()) {
                        Set<BanAction> parsed = BanAction.parse(key);
                        if (parsed != null) {
                            actions.addAll(parsed);
                        }
                    }
                }
                if (!actions.isEmpty()) {
                    allowed.add(new BanRule(ItemTarget.parse(item.getKey(), customs, registries), actions, dimensions,
                            RuleData.EMPTY, false, false, "file"));
                }
            }
            target.add(new Scope(dimensions, RuleData.parse(body.get("message")), ignored, allowed));
        }
    }

    public static String describe(BanRule rule) {
        return BanAction.describe(rule.actions()) + " (" + rule.origin().toLowerCase(Locale.ROOT) + ")";
    }
}
