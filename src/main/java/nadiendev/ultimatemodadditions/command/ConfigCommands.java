package nadiendev.ultimatemodadditions.command;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.DeveloperConfig;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConfigCommands {

    private static final int MAX_TEXT = 160;

    private ConfigCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("config")
                .requires(source -> source.hasPermission(2))
                .executes(ConfigCommands::status)
                .then(Commands.literal("list")
                        .executes(context -> list(context, null))
                        .then(Commands.argument("section", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(sections().keySet(), builder))
                                .executes(context -> list(context, StringArgumentType.getString(context, "section")))))
                .then(Commands.literal("get")
                        .then(keyArgument().executes(ConfigCommands::get)))
                .then(Commands.literal("set")
                        .then(keyArgument()
                                .then(Commands.argument("value", StringArgumentType.greedyString())
                                        .executes(ConfigCommands::set))))
                .then(Commands.literal("reset")
                        .then(keyArgument().executes(ConfigCommands::reset)));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> keyArgument() {
        return Commands.argument("key", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(entries().keySet(), builder));
    }

    private static Map<String, ModConfigSpec.ConfigValue<?>> entries() {
        Map<String, ModConfigSpec.ConfigValue<?>> found = new LinkedHashMap<>();
        walk(UMAConfig.COMMON_SPEC.getValues(), found);
        return found;
    }

    private static void walk(UnmodifiableConfig config, Map<String, ModConfigSpec.ConfigValue<?>> found) {
        for (Object raw : config.valueMap().values()) {
            if (raw instanceof UnmodifiableConfig nested) {
                walk(nested, found);
            } else if (raw instanceof ModConfigSpec.ConfigValue<?> value) {
                found.put(String.join(".", value.getPath()), value);
            }
        }
    }

    private static Map<String, Integer> sections() {
        Map<String, Integer> sections = new LinkedHashMap<>();
        for (String key : entries().keySet()) {
            String section = key.contains(".") ? key.substring(0, key.indexOf('.')) : key;
            sections.merge(section, 1, Integer::sum);
        }
        return sections;
    }

    private static String show(Object value) {
        String text = String.valueOf(value);
        return text.length() > MAX_TEXT ? text.substring(0, MAX_TEXT) + "..." : text;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        String denial = DeveloperConfig.denial(context.getSource());
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.config_status",
                DeveloperConfig.developerMode(), DeveloperConfig.broadcastLogs(), DeveloperConfig.developers().size(),
                denial == null ? Component.translatable("command.ultimatemodadditions.config_can_edit")
                        : Component.translatable(denial)), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context, String section) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (section == null) {
            String text = sections().entrySet().stream()
                    .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                    .collect(Collectors.joining(", "));
            source.sendSuccess(() -> Component.translatable("command.ultimatemodadditions.config_sections", text), false);
            return 1;
        }
        int shown = 0;
        for (Map.Entry<String, ModConfigSpec.ConfigValue<?>> entry : entries().entrySet()) {
            if (entry.getKey().startsWith(section.toLowerCase(Locale.ROOT) + ".")) {
                String line = entry.getKey() + " = " + show(entry.getValue().get());
                source.sendSuccess(() -> Component.literal(line), false);
                shown++;
            }
        }
        if (shown == 0) {
            throw unknown(section);
        }
        return shown;
    }

    private static CommandSyntaxException unknown(String key) {
        return new SimpleCommandExceptionType(Component.translatable("command.ultimatemodadditions.config_unknown", key)).create();
    }

    private static ModConfigSpec.ConfigValue<?> find(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String key = StringArgumentType.getString(context, "key");
        ModConfigSpec.ConfigValue<?> value = entries().get(key);
        if (value == null) {
            throw unknown(key);
        }
        return value;
    }

    private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModConfigSpec.ConfigValue<?> value = find(context);
        String key = String.join(".", value.getPath());
        String comment = value.getSpec().getComment();
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.config_value", key,
                show(value.get()), show(value.getDefault())), false);
        if (comment != null && !comment.isBlank()) {
            context.getSource().sendSuccess(() -> Component.literal(comment.strip()), false);
        }
        return 1;
    }

    private static boolean allowed(CommandContext<CommandSourceStack> context) {
        String denial = DeveloperConfig.denial(context.getSource());
        if (denial != null) {
            context.getSource().sendFailure(Component.translatable(denial));
        }
        return denial == null;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModConfigSpec.ConfigValue<?> value = find(context);
        if (!allowed(context)) {
            return 0;
        }
        String key = String.join(".", value.getPath());
        String input = StringArgumentType.getString(context, "value");
        Object parsed;
        try {
            parsed = parse(value, input);
        } catch (RuntimeException exception) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.config_invalid", key, input));
            return 0;
        }
        if (!value.getSpec().test(parsed)) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.config_invalid", key, input));
            if (value.getSpec().getComment() != null) {
                context.getSource().sendFailure(Component.literal(value.getSpec().getComment().strip()));
            }
            return 0;
        }
        apply(context.getSource(), key, value, parsed);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ModConfigSpec.ConfigValue<?> value = find(context);
        if (!allowed(context)) {
            return 0;
        }
        apply(context.getSource(), String.join(".", value.getPath()), value, value.getDefault());
        return 1;
    }

    @SuppressWarnings("unchecked")
    private static void apply(CommandSourceStack source, String key, ModConfigSpec.ConfigValue<?> value, Object next) {
        Object before = value.get();
        ((ModConfigSpec.ConfigValue<Object>) value).set(next);
        value.save();
        String from = show(before);
        String to = show(next);
        source.sendSuccess(() -> Component.translatable("command.ultimatemodadditions.config_set", key, from, to), false);
        UMA.LOGGER.info("[config] {} changed {}: {} -> {}", source.getTextName(), key, from, to);
        if (!DeveloperConfig.broadcastLogs()) {
            return;
        }
        Component line = Component.translatable("command.ultimatemodadditions.config_log", source.getTextName(), key, from, to);
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (player != source.getEntity() && player.hasPermissions(2) && DeveloperConfig.listed(player.getUUID())) {
                player.sendSystemMessage(line);
            }
        }
    }

    private static Object parse(ModConfigSpec.ConfigValue<?> value, String input) {
        Object sample = value.getDefault();
        String text = input.trim();
        if (sample instanceof Boolean) {
            if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException(text);
            }
            return Boolean.parseBoolean(text);
        }
        if (sample instanceof Integer) {
            return Integer.parseInt(text);
        }
        if (sample instanceof Long) {
            return Long.parseLong(text);
        }
        if (sample instanceof Double) {
            return Double.parseDouble(text);
        }
        if (sample instanceof Enum<?> constant) {
            return Arrays.stream(constant.getDeclaringClass().getEnumConstants())
                    .filter(candidate -> candidate.name().equalsIgnoreCase(text))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(text));
        }
        if (sample instanceof List<?>) {
            String body = text.startsWith("[") && text.endsWith("]") ? text.substring(1, text.length() - 1) : text;
            List<String> items = new ArrayList<>();
            for (String part : body.split(",")) {
                String item = part.trim();
                if (!item.isEmpty()) {
                    items.add(item);
                }
            }
            return items;
        }
        if (sample instanceof String) {
            return text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"") ? text.substring(1, text.length() - 1) : text;
        }
        throw new IllegalArgumentException(text);
    }
}
