package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
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
public final class ShopPermissions {

    public static final String ADD_NODE = "uma.shop.add";
    public static final String REMOVE_NODE = "uma.shop.remove";

    public static final PermissionNode<Boolean> ADD = new PermissionNode<>("uma", "shop.add", PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.hasPermissions(2))
            .setInformation(Component.literal("Shop: add zones and items"),
                    Component.literal("Lets a player add zones and items to the shop from the shop screen."));

    public static final PermissionNode<Boolean> RESET = new PermissionNode<>("uma", "shop.reset", PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.hasPermissions(2))
            .setInformation(Component.literal("Shop: restore"),
                    Component.literal("Lets a player undo every shop edit with /uma shop reset."));

    public static final PermissionNode<Boolean> REMOVE = new PermissionNode<>("uma", "shop.remove", PermissionTypes.BOOLEAN,
            (player, uuid, context) -> player != null && player.hasPermissions(2))
            .setInformation(Component.literal("Shop: remove zones and items"),
                    Component.literal("Lets a player remove zones and items from the shop from the shop screen."));

    private ShopPermissions() {
    }

    @SubscribeEvent
    public static void onGather(PermissionGatherEvent.Nodes event) {
        event.addNodes(ADD, REMOVE, RESET);
    }

    public static boolean canAdd(ServerPlayer player) {
        return UMAConfig.COMMON.shopAdminGui.get() && PermissionAPI.getPermission(player, ADD);
    }

    public static boolean canRemove(ServerPlayer player) {
        return UMAConfig.COMMON.shopAdminGui.get() && PermissionAPI.getPermission(player, REMOVE);
    }

    public static boolean canReset(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null ? source.hasPermission(2) : PermissionAPI.getPermission(player, RESET);
    }

    public static boolean canEdit(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null ? source.hasPermission(2) : canEdit(player);
    }

    public static boolean canEdit(ServerPlayer player) {
        return canAdd(player) || canRemove(player);
    }
}
