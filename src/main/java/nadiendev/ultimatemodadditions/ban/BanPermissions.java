package nadiendev.ultimatemodadditions.ban;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.EnumMap;
import java.util.Map;

@EventBusSubscriber(modid = UMA.MODID)
public final class BanPermissions {

    public static final String BYPASS_NODE = "uma.ban.bypass";

    public static final PermissionNode<Boolean> BYPASS = new PermissionNode<>("uma", "ban.bypass", PermissionTypes.BOOLEAN,
            (player, uuid, context) -> false)
            .setInformation(Component.literal("Item bans: ignore every ban"),
                    Component.literal("Lets a player craft, carry, drop, equip, use and place banned items."));

    private static final Map<BanAction, PermissionNode<Boolean>> BY_ACTION = new EnumMap<>(BanAction.class);

    static {
        for (BanAction action : BanAction.values()) {
            BY_ACTION.put(action, new PermissionNode<>("uma", "ban.bypass." + action.id(), PermissionTypes.BOOLEAN,
                    (player, uuid, context) -> false)
                    .setInformation(Component.literal("Item bans: ignore the " + action.id() + " ban"),
                            Component.literal("Lets a player do the '" + action.id() + "' action with banned items.")));
        }
    }

    private BanPermissions() {
    }

    @SubscribeEvent
    public static void onGather(PermissionGatherEvent.Nodes event) {
        event.addNodes(BYPASS);
        BY_ACTION.values().forEach(event::addNodes);
    }

    public static boolean bypasses(ServerPlayer player, BanAction action) {
        if (JsonValues.bool(BanService.config(), "ops_bypass", false) && player.hasPermissions(2)) {
            return true;
        }
        return PermissionAPI.getPermission(player, BYPASS) || PermissionAPI.getPermission(player, BY_ACTION.get(action));
    }
}
