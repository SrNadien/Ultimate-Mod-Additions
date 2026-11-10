package nadiendev.ultimatemodadditions.compat.ftb;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public final class EchoCommands {

    private static final SimpleCommandExceptionType UNKNOWN = new SimpleCommandExceptionType(
            Component.translatable("command.ultimatemodadditions.echo_unknown"));

    private EchoCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("echo")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("echo", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                EchoPack.shortNames(), builder))
                        .executes(context -> open(context, List.of(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> open(context, EntityArgument.getPlayers(context, "targets")))));
    }

    private static int open(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players)
            throws CommandSyntaxException {
        String id = EchoPack.resolve(StringArgumentType.getString(context, "echo"));
        if (id == null) {
            throw UNKNOWN.create();
        }
        CommandSourceStack source = context.getSource();
        for (ServerPlayer player : players) {
            source.getServer().getCommands().performPrefixedCommand(
                    source.withEntity(player).withPermission(2), "ftbechoes open " + id);
        }
        return players.size();
    }
}
