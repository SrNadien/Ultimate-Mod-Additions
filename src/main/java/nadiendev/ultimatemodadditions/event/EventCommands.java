package nadiendev.ultimatemodadditions.event;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class EventCommands {

    private static final List<String> EVENTS = List.of(BeeEvents.ID, BossEvents.ID, NpcEvents.ID, WelcomeEvents.ID);
    private static final List<String> SPAWNABLE = List.of(BeeEvents.ID, BossEvents.ID, NpcEvents.ID);

    private EventCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("event")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                        .executes(context -> list(context, false))
                        .then(Commands.literal("active").executes(context -> list(context, true))));
        for (String id : EVENTS) {
            LiteralArgumentBuilder<CommandSourceStack> event = Commands.literal(id)
                    .then(Commands.literal("status")
                            .executes(context -> status(context, id))
                            .then(Commands.argument("enabled", BoolArgumentType.bool())
                                    .executes(context -> toggle(context, id, BoolArgumentType.getBool(context, "enabled")))));
            if (SPAWNABLE.contains(id)) {
                event.then(Commands.literal("spawn")
                        .executes(context -> spawn(context, id, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> spawn(context, id, EntityArgument.getPlayer(context, "player")))));
            }
            root.then(event);
        }
        return root;
    }

    private static int list(CommandContext<CommandSourceStack> context, boolean activeOnly) {
        EventState state = EventState.get(context.getSource().getServer());
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.event_list_title")
                .withStyle(ChatFormatting.GOLD), false);
        for (String id : EVENTS) {
            boolean on = state.enabled(id);
            if (activeOnly && !on) {
                continue;
            }
            context.getSource().sendSuccess(() -> Component.literal("  " + id + ": ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable(on ? "command.ultimatemodadditions.event_on"
                            : "command.ultimatemodadditions.event_off").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("event.ultimatemodadditions." + id).withStyle(ChatFormatting.GRAY)), false);
        }
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context, String id) {
        boolean on = EventState.get(context.getSource().getServer()).enabled(id);
        context.getSource().sendSuccess(() -> Component.translatable(on ? "command.ultimatemodadditions.event_is_on"
                : "command.ultimatemodadditions.event_is_off", id).withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        return 1;
    }

    private static int toggle(CommandContext<CommandSourceStack> context, String id, boolean enabled) {
        EventState.get(context.getSource().getServer()).setEnabled(id, enabled);
        context.getSource().sendSuccess(() -> Component.translatable(enabled ? "command.ultimatemodadditions.event_enabled"
                : "command.ultimatemodadditions.event_disabled", id).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), true);
        return 1;
    }

    private static int spawn(CommandContext<CommandSourceStack> context, String id, ServerPlayer player) {
        boolean done = switch (id) {
            case BeeEvents.ID -> BeeEvents.summon(player, player.getX(), player.getY() + 1.5D, player.getZ());
            case BossEvents.ID -> BossEvents.summon(player.server, player);
            default -> NpcEvents.spawnInFront(player, EventConfigs.get(EventDefaults.NPC));
        };
        if (!done) {
            context.getSource().sendFailure(Component.translatable("command.ultimatemodadditions.event_spawn_failed", id));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.ultimatemodadditions.event_spawned", id,
                player.getDisplayName()), true);
        return 1;
    }
}
