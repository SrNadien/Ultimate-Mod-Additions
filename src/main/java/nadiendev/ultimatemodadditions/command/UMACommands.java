package nadiendev.ultimatemodadditions.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.coliseum.ColiseumState;
import nadiendev.ultimatemodadditions.coliseum.ColiseumTravel;
import nadiendev.ultimatemodadditions.currency.LootBagItem;
import nadiendev.ultimatemodadditions.data.UMAData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Map;

@EventBusSubscriber(modid = UMA.MODID)
public final class UMACommands {

    private UMACommands() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        com.mojang.brigadier.tree.LiteralCommandNode<CommandSourceStack> node = event.getDispatcher().register(root());
        event.getDispatcher().register(Commands.literal("ums").redirect(node));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("uma")
                .then(Commands.literal("coliseum")
                        .then(Commands.literal("enter").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return ColiseumTravel.enter(player, context.getSource().getServer()) ? 1 : 0;
                        }))
                        .then(Commands.literal("leave").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return ColiseumTravel.leave(player, context.getSource().getServer()) ? 1 : 0;
                        })))
                .then(Commands.literal("difficulty")
                        .then(Commands.literal("get").executes(context -> {
                            ResourceLocation current = ColiseumState.get(context.getSource().getServer()).difficulty();
                            context.getSource().sendSuccess(() -> Component.translatable(
                                    "command.ultimatemodadditions.difficulty_current", current.toString()), false);
                            return 1;
                        }))
                        .then(Commands.literal("list").executes(context -> {
                            String joined = String.join(", ", UMAData.difficulties().keySet().stream()
                                    .map(ResourceLocation::toString).toList());
                            context.getSource().sendSuccess(() -> Component.literal(joined), false);
                            return 1;
                        }))
                        .then(Commands.literal("set")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                UMAData.difficulties().keySet(), builder))
                                        .executes(UMACommands::setDifficulty))))
                .then(Commands.literal("bag")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("bag", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                UMAData.lootBags().keySet(), builder))
                                        .executes(context -> giveBag(context, 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> giveBag(context,
                                                        IntegerArgumentType.getInteger(context, "count")))))))
                .then(tpsCommand())
                .then(pingCommand())
                .then(spawnersCommand())
                .then(rebuildCommand());
    }

    private static int setDifficulty(CommandContext<CommandSourceStack> context) {
        ResourceLocation id = ResourceLocationArgument.getId(context, "id");
        if (!UMAData.difficulties().containsKey(id)) {
            context.getSource().sendFailure(Component.translatable(
                    "command.ultimatemodadditions.unknown_difficulty", id.toString()));
            return 0;
        }
        ColiseumState.get(context.getSource().getServer()).setDifficulty(id);
        context.getSource().sendSuccess(() -> Component.translatable(
                "command.ultimatemodadditions.difficulty_set", id.toString()), true);
        return 1;
    }

    private static int giveBag(CommandContext<CommandSourceStack> context, int count)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation bag = ResourceLocationArgument.getId(context, "bag");
        for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
            ItemStack stack = LootBagItem.create(bag, count);
            if (stack.isEmpty()) {
                context.getSource().sendFailure(Component.translatable(
                        "message.ultimatemodadditions.unknown_bag", bag.toString()));
                return 0;
            }
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tpsCommand() {
        return Commands.literal("tps").executes(context -> {
            net.minecraft.server.MinecraftServer server = context.getSource().getServer();
            double mspt = server.getAverageTickTimeNanos() / 1_000_000.0D;
            float target = server.tickRateManager().tickrate();
            double tps = Math.min(target, 1000.0D / Math.max(mspt, 1.0E-4D));
            double budget = 1000.0D / Math.max(target, 1.0F);

            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.ultimatemodadditions.tps_header").withStyle(net.minecraft.ChatFormatting.GOLD), false);
            context.getSource().sendSuccess(() -> Component.translatable(
                            "command.ultimatemodadditions.tps_line",
                            Component.literal(String.format(java.util.Locale.ROOT, "%.2f", tps)).withStyle(tpsColor(tps, target)),
                            Component.literal(String.format(java.util.Locale.ROOT, "%.2f", mspt)).withStyle(msptColor(mspt, budget)),
                            String.format(java.util.Locale.ROOT, "%.0f", budget)),
                    false);

            long[] samples = server.getTickTimesNanos();
            double peak = 0.0D;
            for (long sample : samples) {
                peak = Math.max(peak, sample / 1_000_000.0D);
            }
            final double peakMs = peak;
            context.getSource().sendSuccess(() -> Component.translatable(
                    "command.ultimatemodadditions.mspt_peak",
                    String.format(java.util.Locale.ROOT, "%.2f", peakMs)).withStyle(net.minecraft.ChatFormatting.GRAY), false);

            for (net.minecraft.server.level.ServerLevel level : server.getAllLevels()) {
                long[] worldTimes = server.getTickTime(level.dimension());
                if (worldTimes == null) {
                    continue;
                }
                double sum = 0.0D;
                for (long sample : worldTimes) {
                    sum += sample;
                }
                double worldMspt = sum / worldTimes.length / 1_000_000.0D;
                String name = level.dimension().location().toString();
                context.getSource().sendSuccess(() -> Component.translatable(
                                "command.ultimatemodadditions.tps_dimension", name,
                                String.format(java.util.Locale.ROOT, "%.2f", worldMspt))
                        .withStyle(net.minecraft.ChatFormatting.DARK_AQUA), false);
            }
            return (int) Math.round(tps);
        });
    }

    private static net.minecraft.ChatFormatting tpsColor(double tps, float target) {
        if (tps >= target - 0.5D) {
            return net.minecraft.ChatFormatting.GREEN;
        }
        return tps >= target * 0.75D ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED;
    }

    private static net.minecraft.ChatFormatting msptColor(double mspt, double budget) {
        if (mspt <= budget * 0.5D) {
            return net.minecraft.ChatFormatting.GREEN;
        }
        return mspt <= budget ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pingCommand() {
        return Commands.literal("ping")
                .executes(context -> reportPing(context.getSource(),
                        java.util.List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> reportPing(context.getSource(),
                                java.util.List.copyOf(EntityArgument.getPlayers(context, "targets")))));
    }

    private static int reportPing(CommandSourceStack source, java.util.List<ServerPlayer> players) {
        int best = 0;
        for (ServerPlayer player : players) {
            int latency = player.connection.latency();
            best = Math.max(best, latency);
            source.sendSuccess(() -> Component.translatable("command.ultimatemodadditions.ping",
                    player.getGameProfile().getName(), latency).withStyle(pingColor(latency)), false);
        }
        return best;
    }

    private static net.minecraft.ChatFormatting pingColor(int latency) {
        if (latency < 100) {
            return net.minecraft.ChatFormatting.GREEN;
        }
        return latency < 250 ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> spawnersCommand() {
        return Commands.literal("spawners")
                .then(Commands.literal("list").executes(context -> spawners(context, Action.LIST)))
                .then(Commands.literal("place")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawners(context, Action.PLACE)))
                .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> spawners(context, Action.CLEAR)));
    }

    private enum Action {
        LIST,
        PLACE,
        CLEAR
    }

    private static int spawners(CommandContext<CommandSourceStack> context, Action action) {
        net.minecraft.server.level.ServerLevel level = context.getSource().getServer()
                .getLevel(nadiendev.ultimatemodadditions.coliseum.Coliseum.DIMENSION);
        if (level == null) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.no_coliseum"));
            return 0;
        }

        switch (action) {
            case LIST -> {
                int found = nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.count(level);
                context.getSource().sendSuccess(() -> Component.translatable(
                        "command.ultimatemodadditions.spawners_count", found), false);
                return found;
            }
            case CLEAR -> {
                int removed = nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.clear(level);
                context.getSource().sendSuccess(() -> Component.translatable(
                        "command.ultimatemodadditions.spawners_cleared", removed), true);
                return removed;
            }
            default -> {
                if (!nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.enabled()) {
                    context.getSource().sendFailure(
                            Component.translatable("command.ultimatemodadditions.spawners_disabled"));
                    return 0;
                }
                int placed = nadiendev.ultimatemodadditions.coliseum.ColiseumSpawners.place(level);
                if (placed == 0) {
                    context.getSource().sendFailure(
                            Component.translatable("command.ultimatemodadditions.spawners_none"));
                    return 0;
                }
                context.getSource().sendSuccess(() -> Component.translatable(
                        "command.ultimatemodadditions.spawners_placed", placed), true);
                return placed;
            }
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> rebuildCommand() {
        return Commands.literal("rebuild_arena")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    boolean built = nadiendev.ultimatemodadditions.coliseum.ColiseumStartup
                            .ensureArena(context.getSource().getServer(), true);
                    context.getSource().sendSuccess(() -> Component.translatable(built
                            ? "command.ultimatemodadditions.arena_built"
                            : "command.ultimatemodadditions.arena_failed"), true);
                    return built ? 1 : 0;
                });
    }
}
