package nadiendev.ultimatemodadditions.compat.ftb;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.Money;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.math.BigInteger;

public final class RentCommands {

    private static final SimpleCommandExceptionType NO_TEAM = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.rent_no_team"));
    private static final SimpleCommandExceptionType DISABLED = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.rent_disabled"));

    private RentCommands() {
    }

    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(root());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("rent")
                .executes(RentCommands::open)
                .then(Commands.literal("status").executes(RentCommands::status))
                .then(Commands.literal("pay").executes(RentCommands::pay))
                .then(Commands.literal("extend")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("days", IntegerArgumentType.integer(1, 3650))
                                        .executes(RentCommands::extend))))
                .then(Commands.literal("skip")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("days")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 3650))
                                                .executes(context -> skip(context, RentService.dayLength()))))
                                .then(Commands.literal("hours")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 87600))
                                                .executes(context -> skip(context, RentService.dayLength() / 24L))))
                                .then(Commands.literal("due")
                                        .executes(context -> skipTo(context, false)))
                                .then(Commands.literal("eviction")
                                        .executes(context -> skipTo(context, true)))))
                .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(RentCommands::reset)));
    }

    private static Team teamOf(ServerPlayer player) throws CommandSyntaxException {
        if (!UMAConfig.COMMON.rentEnabled.get()) {
            throw DISABLED.create();
        }
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElseThrow(NO_TEAM::create);
    }

    private static int open(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        teamOf(player);
        RentNetwork.sync(player, true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = teamOf(player);
        MinecraftServer server = context.getSource().getServer();
        int chunks = RentService.claimCount(team);
        if (chunks == 0) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.ultimatemodadditions.rent_status_none"), false);
            return 0;
        }
        long now = RentService.now(server);
        long paidUntil = RentService.paidUntil(team);
        long overdue = RentService.overdueSince(team);
        BigInteger cost = RentService.price(chunks);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.rent_status",
                chunks, Money.format(cost), UMAConfig.COMMON.rentPeriodDays.get()), false);
        if (overdue > 0L) {
            long grace = (long) (UMAConfig.COMMON.rentGraceDays.get() * RentService.dayLength());
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.ultimatemodadditions.rent_status_overdue",
                    RentService.duration(Math.max(0L, grace - (now - overdue)))), false);
        } else if (paidUntil > 0L) {
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.ultimatemodadditions.rent_status_next",
                    RentService.duration(Math.max(0L, paidUntil - now))), false);
        }
        return 1;
    }

    private static int pay(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = teamOf(player);
        MinecraftServer server = context.getSource().getServer();
        int chunks = RentService.claimCount(team);
        if (chunks == 0) {
            throw new SimpleCommandExceptionType(Component.translatable(
                    "command.ultimatemodadditions.rent_status_none")).create();
        }
        long now = RentService.now(server);
        if (RentService.paidUntil(team) - now > RentService.period()) {
            throw new SimpleCommandExceptionType(Component.translatable(
                    "command.ultimatemodadditions.rent_ahead")).create();
        }
        BigInteger cost = RentService.price(chunks);
        if (!RentService.payFrom(server, team, player.getUUID(), cost)) {
            throw new SimpleCommandExceptionType(Component.translatable(
                    "command.ultimatemodadditions.money_no_funds")).create();
        }
        BankService.sendOwn(player);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.rent_paid",
                Money.format(cost), RentService.duration(RentService.paidUntil(team) - RentService.now(server))), false);
        return 1;
    }

    private static int extend(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        Team team = teamOf(target);
        int days = IntegerArgumentType.getInteger(context, "days");
        RentService.extend(context.getSource().getServer(), team, days * RentService.dayLength());
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.rent_extended",
                team.getName(), days), true);
        return 1;
    }

    private static int skip(CommandContext<CommandSourceStack> context, long unit) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(context, "amount");
        return advance(context, amount * unit);
    }

    private static int skipTo(CommandContext<CommandSourceStack> context, boolean eviction) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        Team team = teamOf(target);
        MinecraftServer server = context.getSource().getServer();
        long milliseconds = eviction ? RentService.untilEviction(server, team) : RentService.untilDue(server, team);
        return advance(context, milliseconds + 1000L);
    }

    private static int advance(CommandContext<CommandSourceStack> context, long milliseconds) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        Team team = teamOf(target);
        RentService.skip(context.getSource().getServer(), team, milliseconds);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.rent_skipped",
                team.getName(), RentService.duration(milliseconds)), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "player");
        Team team = teamOf(target);
        RentService.reset(team);
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.rent_reset",
                team.getName()), true);
        return 1;
    }
}
