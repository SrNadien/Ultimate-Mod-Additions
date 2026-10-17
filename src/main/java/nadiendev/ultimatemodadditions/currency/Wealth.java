package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;

import java.math.BigInteger;

public final class Wealth {

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger CENTS_PER_MANGO = BigInteger.valueOf(Denomination.CENTS_PER_MANGO);

    private Wealth() {
    }

    public static BigInteger centsOfMangos(BigInteger mangos) {
        return mangos.multiply(CENTS_PER_MANGO);
    }

    public static BigInteger walletCapacity() {
        return capacity(UMAConfig.COMMON.walletMaxBalance.get());
    }

    public static BigInteger bankCapacity() {
        return capacity(UMAConfig.COMMON.bankMaxBalance.get());
    }

    private static BigInteger capacity(String mangos) {
        try {
            return centsOfMangos(new BigInteger(mangos));
        } catch (NumberFormatException exception) {
            return centsOfMangos(new BigInteger(UMAConfig.DEFAULT_MAX_BALANCE_MANGOS));
        }
    }

    public static long clamp(BigInteger value) {
        return value.compareTo(LONG_MAX) > 0 ? Long.MAX_VALUE : value.longValue();
    }

    public static BigInteger parse(String cents) {
        try {
            BigInteger value = new BigInteger(cents);
            return value.signum() < 0 ? BigInteger.ZERO : value;
        } catch (NumberFormatException exception) {
            return BigInteger.ZERO;
        }
    }
}
