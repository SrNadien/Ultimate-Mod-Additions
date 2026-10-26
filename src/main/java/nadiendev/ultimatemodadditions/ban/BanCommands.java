package nadiendev.ultimatemodadditions.ban;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class BanCommands {

    private static final int PAGE = 10;

    private BanCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("ban")
                .requires(source -> source.hasPermission(2))
                .then(addTree(Commands.literal("add"), false))
                .then(Commands.literal("in")
                        .then(Commands.argument("dimension", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        context.getSource().getServer().levelKeys().stream().map(key -> key.location()).toList(), builder))
                                .then(addTree(Commands.literal("add"), true))))
                .then(Commands.literal("disable")
                        .executes(context -> disableHeld(context, null))
                        .then(itemArgument()
                                .executes(context -> disable(context, ResourceLocationArgument.getId(context, "item"), null))
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(context -> disable(context, ResourceLocationArgument.getId(context, "item"),
                                                StringArgumentType.getString(context, "message"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", ResourceLocationArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        listedIds(context.getSource().getServer()), builder))
                                .executes(BanCommands::remove)))
                .then(Commands.literal("list")
                        .executes(context -> list(context, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> list(context, IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("check")
                        .executes(context -> check(context, null))
                        .then(itemArgument().executes(context -> check(context, ResourceLocationArgument.getId(context, "item")))))
                .then(Commands.literal("info").executes(context -> check(context, null)))
                .then(Commands.literal("log")
                        .executes(context -> log(context, null))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(context -> log(context, StringArgumentType.getString(context, "player")))))
                .then(Commands.literal("clear").executes(BanCommands::clearAll))
                .then(Commands.literal("purge")
                        .executes(context -> purge(context, null))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> purge(context, EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("watch").executes(BanCommands::watch))
                .then(Commands.literal("reload").executes(BanCommands::reload));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> itemArgument() {
        return Commands.argument("item", ResourceLocationArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet(), builder));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addTree(LiteralArgumentBuilder<CommandSourceStack> add, boolean dimension) {
        return add.then(Commands.argument("actions", StringArgumentType.string())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(actionNames(), builder))
                .executes(context -> addHeld(context, dimension))
                .then(itemArgument()
                        .executes(context -> add(context, dimension, null))
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(context -> add(context, dimension, StringArgumentType.getString(context, "message"))))));
    }

    private static List<String> actionNames() {
        List<String> names = new ArrayList<>(List.of("all"));
        for (BanAction action : BanAction.values()) {
            names.add(action.id());
        }
        return names;
    }

    private static int addHeld(CommandContext<CommandSourceStack> context, boolean dimension) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.ban_empty_hand"));
            return 0;
        }
        return register(context, dimension, BuiltInRegistries.ITEM.getKey(held.getItem()), null);
    }

    private static int disableHeld(CommandContext<CommandSourceStack> context, String message) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.ban_empty_hand"));
            return 0;
        }
        return disable(context, BuiltInRegistries.ITEM.getKey(held.getItem()), message);
    }

    private static int disable(CommandContext<CommandSourceStack> context, ResourceLocation id, String message) {
        CommandSourceStack source = context.getSource();
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            source.sendFailure(Component.translatable("command.ultimatemodadditions.ban_unknown_item", id.toString()));
            return 0;
        }
        MinecraftServer server = source.getServer();
        BanState state = BanState.get(server);
        if (state.has(id.toString(), "")) {
            source.sendFailure(Component.translatable("command.ultimatemodadditions.ban_exists", id.toString()));
            return 0;
        }
        Set<BanAction> actions = BanAction.parse("all");
        state.add(id.toString(), actions, "", message, true);
        BanApply.apply(server);
        for (BanRule rule : BanService.rules(server)) {
            if (rule.live() && rule.disabled() && rule.target().matches(new ItemStack(item))) {
                BanEvents.scrubEveryone(server, rule);
                break;
            }
        }
        source.sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_disabled", id.toString()), true);
        return 1;
    }

    private static int add(CommandContext<CommandSourceStack> context, boolean dimension, String message) {
        return register(context, dimension, ResourceLocationArgument.getId(context, "item"), message);
    }

    private static int register(CommandContext<CommandSourceStack> context, boolean dimension, ResourceLocation id, String message) {
        CommandSourceStack source = context.getSource();
        String actionText = StringArgumentType.getString(context, "actions");
        Set<BanAction> actions = BanAction.parse(actionText);
        if (actions == null || actions.isEmpty()) {
            source.sendFailure(Component.translatable("command.ultimatemodadditions.ban_unknown_action", actionText));
            return 0;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) {
            source.sendFailure(Component.translatable("command.ultimatemodadditions.ban_unknown_item", id.toString()));
            return 0;
        }
        String dimensions = dimension ? ResourceLocationArgument.getId(context, "dimension").toString() : "";
        MinecraftServer server = source.getServer();
        BanState state = BanState.get(server);
        if (state.has(id.toString(), dimensions)) {
            source.sendFailure(Component.translatable("command.ultimatemodadditions.ban_exists", id.toString()));
            return 0;
        }
        state.add(id.toString(), actions, dimensions, message, false);
        BanApply.apply(server);
        if (actions.contains(BanAction.PICKUP)) {
            for (BanRule rule : BanService.rules(server)) {
                if (rule.live() && rule.target().matches(new ItemStack(item))) {
                    BanEvents.scrubEveryone(server, rule);
                    break;
                }
            }
        }
        source.sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_added", id.toString(),
                BanAction.describe(actions)), true);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) {
        ResourceLocation id = ResourceLocationArgument.getId(context, "item");
        MinecraftServer server = context.getSource().getServer();
        int live = BanState.get(server).remove(id.toString());
        int file = BanFile.remove(id.toString());
        if (live + file == 0) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.ban_not_found", id.toString()));
            return 0;
        }
        BanApply.apply(server);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_removed",
                id.toString()), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context, int page) {
        MinecraftServer server = context.getSource().getServer();
        List<BanRule> rules = BanService.rules(server);
        int pages = Math.max(1, (rules.size() + PAGE - 1) / PAGE);
        int shown = Math.min(page, pages);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_list_title",
                rules.size(), shown, pages).withStyle(ChatFormatting.GOLD), false);
        for (int i = (shown - 1) * PAGE; i < Math.min(rules.size(), shown * PAGE); i++) {
            BanRule rule = rules.get(i);
            context.getSource().sendSuccess(() -> Component.literal("  " + describe(rule)).withStyle(ChatFormatting.GRAY), false);
        }
        return rules.size();
    }

    private static String describe(BanRule rule) {
        return rule.target() + " [" + (rule.disabled() ? "disabled" : BanAction.describe(rule.actions())) + "] " + rule.dimensions()
                + " (" + rule.origin() + ")";
    }

    private static int check(CommandContext<CommandSourceStack> context, ResourceLocation id) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack;
        if (id == null) {
            stack = player.getMainHandItem();
        } else {
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            stack = item == null ? ItemStack.EMPTY : new ItemStack(item);
        }
        if (stack.isEmpty()) {
            source.sendFailure(Component.translatable("command.ultimatemodadditions.ban_empty_hand"));
            return 0;
        }
        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        List<BanRule> rules = BanService.rulesFor(source.getServer(), stack);
        source.sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_check_title", key, rules.size())
                .withStyle(ChatFormatting.GOLD), false);
        for (BanRule rule : rules) {
            source.sendSuccess(() -> Component.literal("  " + describe(rule)).withStyle(ChatFormatting.GRAY), false);
        }
        List<String> blocked = new ArrayList<>();
        for (BanAction action : BanAction.values()) {
            if (BanService.deny(new BanService.Check(player, stack, action)) != null) {
                blocked.add(action.id());
            }
        }
        source.sendSuccess(() -> Component.translatable(blocked.isEmpty() ? "command.ultimatemodadditions.ban_check_free"
                : "command.ultimatemodadditions.ban_check_blocked", String.join(", ", blocked))
                .withStyle(blocked.isEmpty() ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        return rules.size();
    }

    private static int log(CommandContext<CommandSourceStack> context, String player) {
        List<BanLog.Entry> entries = BanLog.recent(player, 10);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_log_title", entries.size())
                .withStyle(ChatFormatting.GOLD), false);
        long now = System.currentTimeMillis();
        for (BanLog.Entry entry : entries) {
            long seconds = Math.max(0L, (now - entry.time()) / 1000L);
            context.getSource().sendSuccess(() -> Component.literal("  " + seconds + "s  " + entry.player() + "  " + entry.item()
                    + "  " + entry.action() + "  " + entry.dimension()).withStyle(ChatFormatting.GRAY), false);
        }
        return entries.size();
    }

    private static List<String> listedIds(MinecraftServer server) {
        Set<String> ids = new TreeSet<>();
        for (BanRule rule : BanService.rules(server)) {
            Set<Item> items = rule.target().exactItems();
            if (items != null) {
                items.forEach(item -> ids.add(BuiltInRegistries.ITEM.getKey(item).toString()));
            }
        }
        return new ArrayList<>(ids);
    }

    private static int clearAll(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        int file = BanFile.clear();
        int live = BanState.get(server).clearAll();
        BanApply.apply(server);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_cleared_all", file + live,
                file, live).withStyle(ChatFormatting.GREEN), true);
        return file + live;
    }

    private static int purge(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        MinecraftServer server = context.getSource().getServer();
        List<ServerPlayer> players = target == null ? server.getPlayerList().getPlayers() : List.of(target);
        BanPurge.Result result = BanPurge.clear(server, players, target == null);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_purged", result.items(),
                result.players(), result.dropped()).withStyle(ChatFormatting.GREEN), true);
        return result.items() + result.dropped();
    }

    private static int watch(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        boolean on = BanEvents.toggleWatch(player.getUUID());
        context.getSource().sendSuccess(() -> Component.translatable(on ? "command.ultimatemodadditions.ban_watch_on"
                : "command.ultimatemodadditions.ban_watch_off").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        BanApply.apply(server);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ban_reloaded",
                BanService.rules(server).size()), true);
        return 1;
    }
}
