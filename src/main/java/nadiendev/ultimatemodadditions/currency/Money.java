package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public final class Money {

    private static final BigInteger CENTS_PER_MANGO = BigInteger.valueOf(Denomination.CENTS_PER_MANGO);

    private Money() {
    }

    public static long toCents(long mangos) {
        return mangos * Denomination.CENTS_PER_MANGO;
    }

    public static Component format(long cents) {
        return format(BigInteger.valueOf(cents));
    }

    public static Component format(BigInteger cents) {
        BigInteger[] parts = cents.divideAndRemainder(CENTS_PER_MANGO);
        return Component.literal(parts[0] + "," + String.format("%02d", parts[1].intValue()));
    }

    public static String group(BigInteger value) {
        String digits = value.toString();
        if (digits.length() <= 4) {
            return digits;
        }
        StringBuilder grouped = new StringBuilder();
        int first = digits.length() % 3;
        if (first > 0) {
            grouped.append(digits, 0, first);
        }
        for (int i = first; i < digits.length(); i += 3) {
            if (grouped.length() > 0) {
                grouped.append('.');
            }
            grouped.append(digits, i, i + 3);
        }
        return grouped.toString();
    }

    public static long valueOf(ItemStack stack) {
        return stack.getItem() instanceof CashItem cash ? cash.cents() * stack.getCount() : 0L;
    }

    public static long looseTotal(Player player) {
        long total = 0L;
        for (ItemStack stack : stacks(player)) {
            total += valueOf(stack);
        }
        return total;
    }

    public static BigInteger walletsTotal(Player player) {
        BigInteger total = BigInteger.ZERO;
        for (ItemStack wallet : wallets(player)) {
            total = total.add(WalletStorage.balance(wallet));
        }
        return total;
    }

    public static BigInteger totalBig(Player player) {
        return BigInteger.valueOf(looseTotal(player)).add(walletsTotal(player));
    }

    public static long total(Player player) {
        return Wealth.clamp(totalBig(player));
    }

    public static boolean take(Player player, long cents) {
        if (cents <= 0L) {
            return true;
        }
        BigInteger wanted = BigInteger.valueOf(cents);
        if (totalBig(player).compareTo(wanted) < 0) {
            return false;
        }
        long loose = looseTotal(player);
        BigInteger wallets = walletsTotal(player);
        long fromLoose;
        if (UMAConfig.COMMON.walletLooseCashFirst.get()) {
            fromLoose = Math.min(loose, cents);
        } else {
            fromLoose = cents - Wealth.clamp(wallets.min(wanted));
        }
        if (fromLoose > 0L && !payLoose(player, fromLoose)) {
            if (wallets.compareTo(wanted) < 0) {
                return false;
            }
            fromLoose = 0L;
        }
        drainWallets(player, BigInteger.valueOf(cents - fromLoose));
        return true;
    }

    private static void drainWallets(Player player, BigInteger remaining) {
        BigInteger left = remaining;
        for (ItemStack wallet : wallets(player)) {
            if (left.signum() <= 0) {
                break;
            }
            left = left.subtract(WalletStorage.withdraw(wallet, left));
        }
    }

    private static boolean payLoose(Player player, long cents) {
        List<ItemStack> pool = stacks(player);
        int[] original = new int[pool.size()];
        for (int i = 0; i < original.length; i++) {
            original[i] = pool.get(i).getCount();
        }
        long change = pay(pool, cents);
        if (change < 0L) {
            for (int i = 0; i < original.length; i++) {
                pool.get(i).setCount(original[i]);
            }
            return false;
        }
        if (change > 0L) {
            give(player, change);
        }
        return true;
    }

    private static long pay(List<ItemStack> pool, long cents) {
        long remaining = cents;
        for (Denomination denomination : Denomination.DESCENDING) {
            if (remaining <= 0L) {
                break;
            }
            for (ItemStack stack : pool) {
                if (remaining <= 0L) {
                    break;
                }
                if (!(stack.getItem() instanceof CashItem cash) || cash.denomination() != denomination) {
                    continue;
                }
                int needed = (int) Math.min(stack.getCount(), remaining / denomination.cents());
                if (needed > 0) {
                    stack.shrink(needed);
                    remaining -= (long) needed * denomination.cents();
                }
            }
        }
        if (remaining <= 0L) {
            return 0L;
        }
        return payWithSmallestCover(pool, remaining);
    }

    private static long payWithSmallestCover(List<ItemStack> pool, long remaining) {
        for (Denomination denomination : ascending()) {
            if (denomination.cents() < remaining) {
                continue;
            }
            for (ItemStack stack : pool) {
                if (stack.getItem() instanceof CashItem cash && cash.denomination() == denomination) {
                    stack.shrink(1);
                    return denomination.cents() - remaining;
                }
            }
        }
        return -1L;
    }

    public static void give(Player player, long cents) {
        for (ItemStack stack : breakDown(cents)) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
    }

    public static List<ItemStack> breakDown(long cents) {
        return breakDownBelow(cents, Long.MAX_VALUE);
    }

    public static List<ItemStack> breakDownBelow(long cents, long maxCentsExclusive) {
        List<ItemStack> stacks = new ArrayList<>();
        long remaining = cents;
        for (Denomination denomination : Denomination.DESCENDING) {
            if (denomination.cents() >= maxCentsExclusive) {
                continue;
            }
            long count = remaining / denomination.cents();
            if (count <= 0L) {
                continue;
            }
            remaining -= count * denomination.cents();
            CashItem item = UMAItems.cash(denomination);
            while (count > 0L) {
                int taken = (int) Math.min(count, item.getDefaultMaxStackSize());
                stacks.add(new ItemStack(item, taken));
                count -= taken;
            }
        }
        return stacks;
    }

    public static long consolidate(Player player) {
        long total = takeAllLoose(player);
        give(player, total);
        return total;
    }

    public static long takeAllLoose(Player player) {
        Inventory inventory = player.getInventory();
        long total = 0L;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            long value = valueOf(stack);
            if (value > 0L) {
                total += value;
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        return total;
    }

    public static boolean split(Player player, ItemStack held) {
        if (!(held.getItem() instanceof CashItem cash) || cash.denomination().lower() == null) {
            return false;
        }
        held.shrink(1);
        for (ItemStack stack : breakDownBelow(cash.cents(), cash.cents())) {
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

    public static List<ItemStack> wallets(Player player) {
        List<ItemStack> wallets = new ArrayList<>();
        int limit = UMAConfig.COMMON.walletMaxCounted.get();
        for (ItemStack stack : stacks(player)) {
            if (wallets.size() >= limit) {
                break;
            }
            if (stack.getItem() instanceof WalletItem) {
                wallets.add(stack);
            }
        }
        return wallets;
    }
}
