package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.network.payload.MoneyActionPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;

public final class MoneyActions {

    private MoneyActions() {
    }

    public static void handle(ServerPlayer player, MoneyActionPayload payload) {
        BigInteger cents = Wealth.parse(payload.cents());
        if (payload.target() == MoneyActionPayload.Target.WALLET) {
            if (player.containerMenu instanceof WalletMenu menu) {
                wallet(player, menu.wallet(), payload.action(), cents);
            }
        } else if (player.containerMenu instanceof AtmMenu menu && UMAConfig.COMMON.bankEnabled.get()) {
            atm(player, menu, payload.action(), cents);
        }
    }

    private static BigInteger withdrawLimit() {
        return Wealth.centsOfMangos(BigInteger.valueOf(UMAConfig.COMMON.maxWithdrawMangos.get()));
    }

    private static void wallet(ServerPlayer player, ItemStack wallet, MoneyActionPayload.Action action,
                               BigInteger cents) {
        switch (action) {
            case DEPOSIT_ALL -> {
                long loose = Money.takeAllLoose(player);
                BigInteger added = WalletStorage.deposit(wallet, BigInteger.valueOf(loose));
                Money.give(player, loose - added.longValue());
                say(player, "message.ultimatemodadditions.wallet_deposited", Money.format(added));
            }
            case WITHDRAW, WITHDRAW_ALL -> {
                BigInteger wanted = action == MoneyActionPayload.Action.WITHDRAW_ALL ? WalletStorage.balance(wallet) : cents;
                BigInteger taken = WalletStorage.withdraw(wallet, wanted.min(withdrawLimit()));
                if (taken.signum() == 0) {
                    fail(player, "message.ultimatemodadditions.wallet_empty");
                    BankService.sendWallet(player, WalletStorage.balance(wallet));
                    return;
                }
                Money.give(player, taken.longValue());
                say(player, "message.ultimatemodadditions.wallet_withdrawn", Money.format(taken));
            }
            default -> {
            }
        }
        BankService.sendWallet(player, WalletStorage.balance(wallet));
    }

    private static void atm(ServerPlayer player, AtmMenu menu, MoneyActionPayload.Action action, BigInteger cents) {
        if (action == MoneyActionPayload.Action.REQUEST_CARD) {
            requestCard(player);
            return;
        }
        ItemStack card = menu.card();
        CardOwner owner = DebitCardItem.owner(card);
        if (owner == null || !DebitCardItem.usableBy(card, player)) {
            fail(player, "message.ultimatemodadditions.atm_insert_card");
            return;
        }
        switch (action) {
            case DEPOSIT_ALL -> depositAll(player, owner);
            case WITHDRAW, WITHDRAW_ALL -> {
                BigInteger wanted = action == MoneyActionPayload.Action.WITHDRAW_ALL
                        ? BankService.balance(player.server, owner.id()) : cents;
                BigInteger taken = BankService.withdraw(player.server, owner.id(), wanted.min(withdrawLimit()));
                if (taken.signum() == 0) {
                    fail(player, "message.ultimatemodadditions.atm_no_funds");
                    return;
                }
                Money.give(player, taken.longValue());
                say(player, "message.ultimatemodadditions.atm_withdrawn", Money.format(taken));
            }
            default -> {
            }
        }
        BankService.sendAtm(player, owner.id());
    }

    private static void depositAll(ServerPlayer player, CardOwner owner) {
        long loose = Money.takeAllLoose(player);
        BigInteger walletCents = BigInteger.ZERO;
        for (ItemStack wallet : Money.wallets(player)) {
            walletCents = walletCents.add(WalletStorage.withdraw(wallet, WalletStorage.balance(wallet)));
        }
        BigInteger total = BigInteger.valueOf(loose).add(walletCents);
        BigInteger added = BankService.deposit(player.server, owner.id(), total);
        BigInteger leftover = total.subtract(added);
        if (leftover.signum() > 0) {
            for (ItemStack wallet : Money.wallets(player)) {
                leftover = leftover.subtract(WalletStorage.deposit(wallet, leftover));
                if (leftover.signum() == 0) {
                    break;
                }
            }
            if (leftover.signum() > 0) {
                Money.give(player, Wealth.clamp(leftover));
            }
        }
        if (added.signum() == 0) {
            fail(player, "message.ultimatemodadditions.atm_nothing_to_deposit");
            return;
        }
        say(player, "message.ultimatemodadditions.atm_deposited", Money.format(added));
    }

    private static void requestCard(ServerPlayer player) {
        BigInteger price = Wealth.centsOfMangos(BigInteger.valueOf(UMAConfig.COMMON.cardPriceMangos.get()));
        if (price.signum() > 0) {
            BigInteger paid = BankService.withdraw(player.server, player.getUUID(), price);
            if (paid.compareTo(price) < 0) {
                BankService.deposit(player.server, player.getUUID(), paid);
                fail(player, "message.ultimatemodadditions.atm_card_cost", Money.format(price));
                return;
            }
        }
        ItemStack card = DebitCardItem.create(player);
        if (!player.getInventory().add(card)) {
            player.drop(card, false);
        }
        say(player, "message.ultimatemodadditions.atm_card_issued");
    }

    private static void say(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(ChatFormatting.GREEN), false);
    }

    private static void fail(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(ChatFormatting.RED), false);
    }
}
