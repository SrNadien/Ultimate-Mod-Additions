package nadiendev.ultimatemodadditions.ban;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BanPurge {

    public record Result(int items, int players, int dropped) {
    }

    private BanPurge() {
    }

    private static boolean purgeable(List<BanRule> rules, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (BanRule rule : rules) {
            if (rule.actions().contains(BanAction.PICKUP) && rule.target().matches(stack)) {
                return true;
            }
        }
        return false;
    }

    public static Result clear(MinecraftServer server, Collection<ServerPlayer> players, boolean ground) {
        List<BanRule> rules = new ArrayList<>(BanService.rules(server));
        int items = 0;
        int touched = 0;
        for (ServerPlayer player : players) {
            int before = items;
            Inventory inventory = player.getInventory();
            items += sweep(inventory, rules);
            items += sweep(player.getEnderChestInventory(), rules);
            ItemStack carried = player.containerMenu.getCarried();
            if (purgeable(rules, carried)) {
                items += carried.getCount();
                player.containerMenu.setCarried(ItemStack.EMPTY);
            }
            if (items != before) {
                touched++;
                player.inventoryMenu.broadcastFullState();
            }
        }
        int dropped = 0;
        if (ground) {
            for (ServerLevel level : server.getAllLevels()) {
                List<Entity> found = new ArrayList<>();
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof ItemEntity item && purgeable(rules, item.getItem())) {
                        found.add(entity);
                    }
                }
                for (Entity entity : found) {
                    dropped += ((ItemEntity) entity).getItem().getCount();
                    entity.discard();
                }
            }
        }
        return new Result(items, touched, dropped);
    }

    private static int sweep(Container container, List<BanRule> rules) {
        int removed = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (purgeable(rules, stack)) {
                removed += stack.getCount();
                container.setItem(slot, ItemStack.EMPTY);
            }
        }
        return removed;
    }
}
