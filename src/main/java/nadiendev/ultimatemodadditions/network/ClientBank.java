package nadiendev.ultimatemodadditions.network;

import java.math.BigInteger;

public final class ClientBank {

    private static BigInteger own = BigInteger.ZERO;
    private static BigInteger atm = BigInteger.ZERO;
    private static BigInteger wallet = BigInteger.ZERO;

    private ClientBank() {
    }

    public static void accept(boolean ownAccount, BigInteger cents) {
        if (ownAccount) {
            own = cents;
        } else {
            atm = cents;
        }
    }

    public static void acceptWallet(BigInteger cents) {
        wallet = cents;
    }

    public static BigInteger wallet() {
        return wallet;
    }

    public static BigInteger own() {
        return own;
    }

    public static BigInteger atm() {
        return atm;
    }
}
