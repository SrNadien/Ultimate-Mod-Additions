package nadiendev.ultimatemodadditions.compat.ftb;

import dev.ftb.mods.ftblibrary.integration.currency.CurrencyProvider;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.config.UMAConfig.PayTarget;
import nadiendev.ultimatemodadditions.currency.Denomination;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Payout;
import nadiendev.ultimatemodadditions.currency.Wealth;
import nadiendev.ultimatemodadditions.network.ClientBank;
import nadiendev.ultimatemodadditions.shop.ShopService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.math.BigInteger;

public final class MangoCurrencyProvider implements CurrencyProvider {

    private static final BigInteger CENTS_PER_MANGO = BigInteger.valueOf(Denomination.CENTS_PER_MANGO);

    @Override
    public String getName() {
        return "Ultimate Mod Additions";
    }

    @Override
    public long getTotalCurrency(Player player) {
        BigInteger cents;
        if (player instanceof ServerPlayer serverPlayer) {
            cents = ShopService.balance(serverPlayer);
        } else {
            cents = UMAConfig.COMMON.bankEnabled.get() ? ClientBank.own() : Money.totalBig(player);
        }
        return Wealth.clamp(cents.divide(CENTS_PER_MANGO));
    }

    @Override
    public boolean takeCurrency(Player player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        return player instanceof ServerPlayer serverPlayer
                && ShopService.charge(serverPlayer, BigInteger.valueOf(amount).multiply(CENTS_PER_MANGO));
    }

    @Override
    public void giveCurrency(Player player, long amount) {
        if (amount > 0L && player instanceof ServerPlayer serverPlayer) {
            Payout.give(serverPlayer, BigInteger.valueOf(amount).multiply(CENTS_PER_MANGO), PayTarget.ACCOUNT);
        }
    }

    @Override
    public Component coinName(boolean plural) {
        return Component.translatable(plural ? "currency.ultimatemodadditions.mangos" : "currency.ultimatemodadditions.mango");
    }
}
