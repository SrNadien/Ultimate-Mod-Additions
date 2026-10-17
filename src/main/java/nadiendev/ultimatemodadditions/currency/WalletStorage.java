package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.registry.UMADataComponents;
import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;

public final class WalletStorage {

    private WalletStorage() {
    }

    public static BigInteger balance(ItemStack wallet) {
        return Wealth.parse(wallet.getOrDefault(UMADataComponents.WALLET_BALANCE, "0"));
    }

    public static void set(ItemStack wallet, BigInteger cents) {
        wallet.set(UMADataComponents.WALLET_BALANCE, cents.max(BigInteger.ZERO).toString());
    }

    public static BigInteger deposit(ItemStack wallet, BigInteger cents) {
        BigInteger current = balance(wallet);
        BigInteger room = Wealth.walletCapacity().subtract(current).max(BigInteger.ZERO);
        BigInteger added = cents.min(room);
        set(wallet, current.add(added));
        return added;
    }

    public static BigInteger withdraw(ItemStack wallet, BigInteger cents) {
        BigInteger current = balance(wallet);
        BigInteger taken = cents.min(current);
        set(wallet, current.subtract(taken));
        return taken;
    }
}
