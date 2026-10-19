package nadiendev.ultimatemodadditions.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import nadiendev.ultimatemodadditions.compat.ModIds;
import nadiendev.ultimatemodadditions.daily.DailyPermissions;
import nadiendev.ultimatemodadditions.shop.ShopPermissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Predicate;

public final class UMAHelp {
    private static final String KEY = "command.ultimatemodadditions.help";

    private static final Predicate<CommandSourceStack> ANYONE = source -> true;
    private static final Predicate<CommandSourceStack> OPERATOR = source -> source.hasPermission(2);
    private static final Predicate<CommandSourceStack> FTB = source -> ModIds.loaded(ModIds.FTB_CHUNKS);
    private static final Predicate<CommandSourceStack> FTB_OPERATOR = FTB.and(OPERATOR);

    private record Section(String id, Predicate<CommandSourceStack> visible) {
    }

    private record Entry(String section, String id, String usage, Predicate<CommandSourceStack> visible) {
    }

    private static final List<Section> SECTIONS = List.of(
            new Section("money", ANYONE),
            new Section("shop", ANYONE),
            new Section("jobs", ANYONE),
            new Section("daily", ANYONE),
            new Section("rent", FTB),
            new Section("coliseum", ANYONE),
            new Section("tools", ANYONE));

    private static final List<Entry> ENTRIES = List.of(
            new Entry("money", "money_balance", "/uma money balance", ANYONE),
            new Entry("money", "money_balance_other", "/uma money balance <player>", OPERATOR),
            new Entry("money", "money_pay", "/uma money pay <player> <amount>", ANYONE),
            new Entry("money", "money_adjust", "/uma money give|take|set <players> <amount>", OPERATOR),
            new Entry("money", "money_team", "/uma money team balance|deposit|withdraw", FTB),
            new Entry("shop", "shop_open", "/uma shop", ANYONE),
            new Entry("shop", "shop_admin", "/uma shop admin", ShopPermissions::canEdit),
            new Entry("shop", "shop_reset", "/uma shop reset", ShopPermissions::canReset),
            new Entry("jobs", "jobs_open", "/uma jobs", ANYONE),
            new Entry("jobs", "jobs_join", "/uma jobs join|leave <job>", ANYONE),
            new Entry("jobs", "jobs_leaveall", "/uma jobs leaveall", ANYONE),
            new Entry("jobs", "jobs_list", "/uma jobs list", ANYONE),
            new Entry("jobs", "jobs_stats", "/uma jobs stats", ANYONE),
            new Entry("jobs", "jobs_info", "/uma jobs info <job>", ANYONE),
            new Entry("jobs", "jobs_top", "/uma jobs top <job>", ANYONE),
            new Entry("jobs", "jobs_boost", "/uma jobs boost add|clear|list", OPERATOR),
            new Entry("jobs", "jobs_admin", "/uma jobs admin employ|fire|setlevel|addxp", OPERATOR),
            new Entry("daily", "daily_open", "/uma daily", ANYONE),
            new Entry("daily", "daily_claim", "/uma daily claim [daily|weekly|monthly]", ANYONE),
            new Entry("daily", "daily_config", "/uma daily config", DailyPermissions::canConfigure),
            new Entry("daily", "daily_reset", "/uma daily reset", DailyPermissions::canConfigure),
            new Entry("daily", "daily_resetplayer", "/uma daily resetplayer <player>", OPERATOR),
            new Entry("rent", "rent_open", "/uma rent", FTB),
            new Entry("rent", "rent_status", "/uma rent status", FTB),
            new Entry("rent", "rent_pay", "/uma rent pay", FTB),
            new Entry("rent", "rent_admin", "/uma rent extend|skip|reset <player>", FTB_OPERATOR),
            new Entry("coliseum", "coliseum_enter", "/uma coliseum enter", ANYONE),
            new Entry("coliseum", "coliseum_leave", "/uma coliseum leave", ANYONE),
            new Entry("coliseum", "difficulty_get", "/uma difficulty get", ANYONE),
            new Entry("coliseum", "difficulty_list", "/uma difficulty list", ANYONE),
            new Entry("coliseum", "difficulty_set", "/uma difficulty set <id>", OPERATOR),
            new Entry("coliseum", "rebuild_arena", "/uma rebuild_arena", OPERATOR),
            new Entry("tools", "tps", "/uma tps", ANYONE),
            new Entry("tools", "ping", "/uma ping [players]", ANYONE),
            new Entry("tools", "spawners_list", "/uma spawners list", ANYONE),
            new Entry("tools", "spawners_edit", "/uma spawners place|clear", OPERATOR),
            new Entry("tools", "bag", "/uma bag <players> <bag> [count]", OPERATOR));

    private UMAHelp() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("help")
                .executes(context -> sections(context.getSource()))
                .then(Commands.argument("section", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                SECTIONS.stream().filter(section -> section.visible().test(context.getSource()))
                                        .map(Section::id).toList(), builder))
                        .executes(context -> section(context, StringArgumentType.getString(context, "section"))));
    }

    private static int sections(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable(KEY + ".title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        for (Section section : SECTIONS) {
            if (section.visible().test(source)) {
                source.sendSuccess(() -> line("/uma help " + section.id(), Component.translatable(KEY + ".section." + section.id())), false);
            }
        }
        source.sendSuccess(() -> Component.translatable(KEY + ".hint").withStyle(ChatFormatting.DARK_GRAY), false);
        return 1;
    }

    private static int section(CommandContext<CommandSourceStack> context, String id) {
        CommandSourceStack source = context.getSource();
        Section found = SECTIONS.stream().filter(section -> section.id().equals(id)).findFirst().orElse(null);
        if (found == null || !found.visible().test(source)) {
            source.sendFailure(Component.translatable(KEY + ".unknown_section", id));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable(KEY + ".section_title",
                Component.translatable(KEY + ".section." + id)).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        for (Entry entry : ENTRIES) {
            if (entry.section().equals(id) && entry.visible().test(source)) {
                source.sendSuccess(() -> line(entry.usage(), Component.translatable(KEY + "." + entry.id())), false);
            }
        }
        return 1;
    }

    private static MutableComponent line(String usage, Component description) {
        String suggestion = usage.split("[<\\[|]")[0].stripTrailing() + " ";
        MutableComponent command = Component.literal(usage).withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion)));
        return command.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                .append(description.copy().withStyle(ChatFormatting.GRAY));
    }
}
