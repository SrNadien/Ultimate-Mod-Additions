package nadiendev.ultimatemodadditions.daily;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

@EventBusSubscriber(modid = UMA.MODID)
public final class DailyPermissions {

    public static final PermissionNode<Boolean> CONFIGURE = new PermissionNode<>("uma", "daily.config",
            PermissionTypes.BOOLEAN, (player, uuid, context) -> player != null && player.hasPermissions(2))
            .setInformation(Component.literal("Daily rewards: configure"),
                    Component.literal("Lets a player change the daily rewards and their settings with /daily config."));

    private DailyPermissions() {
    }

    @SubscribeEvent
    public static void onGather(PermissionGatherEvent.Nodes event) {
        event.addNodes(CONFIGURE);
    }

    public static boolean canConfigure(ServerPlayer player) {
        return PermissionAPI.getPermission(player, CONFIGURE);
    }

    public static boolean canConfigure(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null ? source.hasPermission(2) : canConfigure(player);
    }
}
