package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.registry.UMAItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class Money {

    private Money() {
    }

    public static Component format(long amount) {
        long lucas = amount / 1000L;
        long mangos = amount % 1000L;
        if (lucas > 0 && mangos > 0) {
            return Component.translatable("money.ultimatemodadditions.combined", lucas(lucas), mangos(mangos));
        }
        if (lucas > 0) {
            return lucas(lucas);
        }
        return mangos(mangos);
    }

    private static MutableComponent lucas(long amount) {
        return Component.translatable(amount == 1L
                ? "money.ultimatemodadditions.luca"
                : "money.ultimatemodadditions.lucas", amount);
    }

    private static MutableComponent mangos(long amount) {
        return Component.translatable(amount == 1L
                ? "money.ultimatemodadditions.mango"
                : "money.ultimatemodadditions.mangos", amount);
    }

    public static int valueOf(ItemStack stack) {
        return stack.getItem() instanceof CoinItem coin ? coin.value() * stack.getCount() : 0;
    }

    public static long total(Player player) {
        long total = 0L;
        for (ItemStack stack : stacks(player)) {
            total += valueOf(stack);
        }
        return total;
    }

    public static boolean take(Player player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (total(player) < amount) {
            return false;
        }
        long remaining = amount;
        List<ItemStack> stacks = stacks(player);
        for (Denomination denomination : Denomination.DESCENDING) {
            if (remaining <= 0L) {
                break;
            }
            for (ItemStack stack : stacks) {
                if (remaining <= 0L) {
                    break;
                }
                if (!(stack.getItem() instanceof CoinItem coin) || coin.denomination() != denomination) {
                    continue;
                }
                int needed = (int) Math.min(stack.getCount(), remaining / denomination.value());
                if (needed > 0) {
                    stack.shrink(needed);
                    remaining -= (long) needed * denomination.value();
                }
            }
        }
        if (remaining > 0L) {
            long change = payWithLargest(player, remaining);
            if (change < 0L) {
                return false;
            }
            give(player, change);
        }
        return true;
    }

    private static long payWithLargest(Player player, long remaining) {
        for (Denomination denomination : ascending()) {
            if (denomination.value() < remaining) {
                continue;
            }
            for (ItemStack stack : stacks(player)) {
                if (stack.getItem() instanceof CoinItem coin && coin.denomination() == denomination) {
                    stack.shrink(1);
                    return denomination.value() - remaining;
                }
            }
        }
        return -1L;
    }

    public static void give(Player player, long amount) {
        for (ItemStack stack : breakDown(amount)) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    public static List<ItemStack> breakDown(long amount) {
        return breakDownBelow(amount, Integer.MAX_VALUE);
    }

    public static List<ItemStack> breakDownBelow(long amount, int maxValueExclusive) {
        List<ItemStack> stacks = new ArrayList<>();
        long remaining = amount;
        for (Denomination denomination : Denomination.DESCENDING) {
            if (denomination.value() >= maxValueExclusive) {
                continue;
            }
            long count = remaining / denomination.value();
            if (count <= 0L) {
                continue;
            }
            remaining -= count * denomination.value();
            CoinItem item = UMAItems.coin(denomination);
            while (count > 0L) {
                int taken = (int) Math.min(count, item.getDefaultMaxStackSize());
                stacks.add(new ItemStack(item, taken));
                count -= taken;
            }
        }
        return stacks;
    }

    public static long consolidate(Player player) {
        Inventory inventory = player.getInventory();
        long total = 0L;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            int value = valueOf(stack);
            if (value > 0) {
                total += value;
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        give(player, total);
        return total;
    }

    public static boolean split(Player player, ItemStack held) {
        if (!(held.getItem() instanceof CoinItem coin) || coin.denomination().lower() == null) {
            return false;
        }
        held.shrink(1);
        for (ItemStack stack : breakDownBelow(coin.value(), coin.value())) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        return true;
    }

    private static Denomination[] ascending() {
        Denomination[] values = Denomination.DESCENDING.clone();
        for (int i = 0, j = values.length - 1; i < j; i++, j--) {
            Denomination swap = values[i];
            values[i] = values[j];
            values[j] = swap;
        }
        return values;
    }

    public static List<ItemStack> stacks(Player player) {
        List<ItemStack> stacks = new ArrayList<>(player.getInventory().items);
        stacks.addAll(player.getInventory().offhand);
        return stacks;
    }
}
