package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.config.UMAConfig.CardMode;
import nadiendev.ultimatemodadditions.network.ClientBank;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;

public final class Payment {

    private Payment() {
    }

    private static boolean cardActive(Player player) {
        if (!UMAConfig.COMMON.bankEnabled.get() || UMAConfig.COMMON.stallCardMode.get() == CardMode.OFF) {
            return false;
        }
        return !DebitCardItem.findUsable(player).isEmpty();
    }

    private static BigInteger account(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return BankService.balance(serverPlayer.server, serverPlayer.getUUID());
        }
        return ClientBank.own();
    }

    public static BigInteger available(Player player) {
        BigInteger total = Money.totalBig(player);
        if (cardActive(player)) {
            total = total.add(account(player));
        }
        return total;
    }

    public static boolean pay(ServerPlayer player, long cents) {
        if (cents <= 0L) {
            return true;
        }
        BigInteger wanted = BigInteger.valueOf(cents);
        if (available(player).compareTo(wanted) < 0) {
            return false;
        }
        boolean useCard = cardActive(player);
        BigInteger cash = Money.totalBig(player);
        BigInteger card = useCard ? account(player) : BigInteger.ZERO;
        BigInteger fromCard;
        if (useCard && UMAConfig.COMMON.stallCardMode.get() == CardMode.FIRST) {
            fromCard = card.min(wanted);
        } else {
            fromCard = wanted.subtract(cash.min(wanted));
        }
        long fromCash = Wealth.clamp(wanted.subtract(fromCard));
        if (fromCash > 0L && !Money.take(player, fromCash)) {
            return false;
        }
        if (fromCard.signum() > 0) {
            BankService.withdraw(player.server, player.getUUID(), fromCard);
        }
        return true;
    }

    public static boolean hasUsableCard(Player player) {
        ItemStack card = DebitCardItem.findUsable(player);
        return !card.isEmpty();
    }
}
