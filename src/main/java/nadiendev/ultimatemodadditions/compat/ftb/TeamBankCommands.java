package nadiendev.ultimatemodadditions.compat.ftb;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamRank;
import nadiendev.ultimatemodadditions.command.UMACommands;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.Money;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.math.BigInteger;
import java.util.UUID;

public final class TeamBankCommands {

    private static final SimpleCommandExceptionType NO_TEAM = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.team_bank_no_team"));
    private static final SimpleCommandExceptionType DISABLED = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.team_bank_disabled"));
    private static final SimpleCommandExceptionType NO_FUNDS = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.money_no_funds"));
    private static final SimpleCommandExceptionType TEAM_NO_FUNDS = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.team_bank_no_funds"));
    private static final DynamicCommandExceptionType NO_PERMISSION = new DynamicCommandExceptionType(
            rank -> Component.translatable("command.ultimatemodadditions.team_bank_no_permission", rank));
    private static final DynamicCommandExceptionType INVALID_AMOUNT = new DynamicCommandExceptionType(
            value -> Component.translatable("command.ultimatemodadditions.money_invalid_amount", value));

    private TeamBankCommands() {
    }

    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register(root());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("money")
                .then(Commands.literal("pay")
                        .then(Commands.literal("team")
                                .then(UMACommands.amount().executes(TeamBankCommands::deposit))))
                .then(Commands.literal("team")
                        .then(Commands.literal("balance").executes(TeamBankCommands::balance))
                        .then(Commands.literal("deposit")
                                .then(UMACommands.amount().executes(TeamBankCommands::deposit)))
                        .then(Commands.literal("withdraw")
                                .then(UMACommands.amount().executes(TeamBankCommands::withdraw))));
    }

    public static UUID account(Team team) {
        return team.getId();
    }

    public static boolean usable(Team team) {
        return UMAConfig.COMMON.teamBankEnabled.get()
                && (!UMAConfig.COMMON.teamBankPartyOnly.get() || team.isPartyTeam());
    }

    private static Team teamOf(ServerPlayer player) throws CommandSyntaxException {
        UMACommands.requireBank();
        if (!UMAConfig.COMMON.teamBankEnabled.get()) {
            throw DISABLED.create();
        }
        Team team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElseThrow(NO_TEAM::create);
        if (!usable(team)) {
            throw NO_TEAM.create();
        }
        return team;
    }

    private static BigInteger positive(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BigInteger cents = UMACommands.parseAmount(context);
        if (cents.signum() <= 0) {
            throw INVALID_AMOUNT.create(StringArgumentType.getString(context, "amount"));
        }
        return cents;
    }

    private static void notifyOthers(Team team, ServerPlayer actor, Component notice) {
        for (ServerPlayer member : team.getOnlineMembers()) {
            if (!member.getUUID().equals(actor.getUUID())) {
                member.sendSystemMessage(notice);
            }
        }
    }

    private static int balance(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = teamOf(player);
        BigInteger value = BankService.balance(context.getSource().getServer(), account(team));
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.team_bank_balance",
                team.getName(), Money.format(value)), false);
        return 1;
    }

    private static int deposit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = teamOf(player);
        BigInteger cents = positive(context);
        MinecraftServer server = context.getSource().getServer();
        BigInteger taken = BankService.withdraw(server, player.getUUID(), cents);
        if (taken.compareTo(cents) < 0) {
            BankService.deposit(server, player.getUUID(), taken);
            throw NO_FUNDS.create();
        }
        BigInteger received = BankService.deposit(server, account(team), cents);
        if (received.compareTo(cents) < 0) {
            BankService.deposit(server, player.getUUID(), cents.subtract(received));
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.team_bank_deposited",
                Money.format(received), team.getName()), false);
        notifyOthers(team, player, Component.translatable("command.ultimatemodadditions.team_bank_received",
                player.getDisplayName(), Money.format(received)));
        return 1;
    }

    private static int withdraw(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Team team = teamOf(player);
        TeamRank needed = TeamRank.valueOf(UMAConfig.COMMON.teamBankWithdrawRank.get());
        if (!team.getRankForPlayer(player.getUUID()).isAtLeast(needed)) {
            throw NO_PERMISSION.create(needed.getSerializedName());
        }
        BigInteger cents = positive(context);
        MinecraftServer server = context.getSource().getServer();
        BigInteger taken = BankService.withdraw(server, account(team), cents);
        if (taken.compareTo(cents) < 0) {
            BankService.deposit(server, account(team), taken);
            throw TEAM_NO_FUNDS.create();
        }
        BigInteger received = BankService.deposit(server, player.getUUID(), cents);
        if (received.compareTo(cents) < 0) {
            BankService.deposit(server, account(team), cents.subtract(received));
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.team_bank_withdrawn",
                Money.format(received), team.getName()), false);
        notifyOthers(team, player, Component.translatable("command.ultimatemodadditions.team_bank_taken",
                player.getDisplayName(), Money.format(received)));
        return 1;
    }
}
