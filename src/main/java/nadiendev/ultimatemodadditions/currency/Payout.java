package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.config.UMAConfig.PayTarget;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigInteger;

public final class Payout {

    private Payout() {
    }

    public static BigInteger give(ServerPlayer player, BigInteger cents, PayTarget target) {
        if (cents.signum() <= 0) {
            return BigInteger.ZERO;
        }
        if (target == PayTarget.ACCOUNT && UMAConfig.COMMON.bankEnabled.get()) {
            return BankService.deposit(player.server, player.getUUID(), cents);
        }
        long loose = Wealth.clamp(cents);
        Money.give(player, loose);
        return BigInteger.valueOf(loose);
    }
}
