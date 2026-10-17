package nadiendev.ultimatemodadditions.currency;

import java.util.Arrays;
import java.util.Locale;

public enum Denomination {

    C5("5_centavos", 5L, Kind.COIN),
    C10("10_centavos", 10L, Kind.COIN),
    C25("25_centavos", 25L, Kind.COIN),
    C50("50_centavos", 50L, Kind.COIN),
    P2("2_pe", 200L, Kind.COIN),
    P5("5_pe", 500L, Kind.COIN),
    P10("10_pe", 1_000L, Kind.COIN),
    B2("2_mangos", 200L, Kind.BILL),
    B5("5_mangos", 500L, Kind.BILL),
    B10("10_mangos", 1_000L, Kind.BILL),
    B20("20_mangos", 2_000L, Kind.BILL),
    B50("50_mangos", 5_000L, Kind.BILL),
    B100("100_mangos", 10_000L, Kind.BILL),
    L1("1_lucas", 100_000L, Kind.BILL),
    L2("2_lucas", 200_000L, Kind.BILL),
    L10("10_lucas", 1_000_000L, Kind.BILL),
    L20("20_lucas", 2_000_000L, Kind.BILL);

    public static final long CENTS_PER_MANGO = 100L;

    public static final Denomination[] DESCENDING;

    static {
        Denomination[] values = values().clone();
        Arrays.sort(values, (a, b) -> {
            int byValue = Long.compare(b.cents, a.cents);
            return byValue != 0 ? byValue : b.kind.compareTo(a.kind);
        });
        DESCENDING = values;
    }

    public enum Kind {
        COIN,
        BILL
    }

    private final String key;
    private final long cents;
    private final Kind kind;

    Denomination(String key, long cents, Kind kind) {
        this.key = key;
        this.cents = cents;
        this.kind = kind;
    }

    public String key() {
        return key;
    }

    public long cents() {
        return cents;
    }

    public Kind kind() {
        return kind;
    }

    public String itemName() {
        return key;
    }

    public static Denomination byKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return C5;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (Denomination denomination : values()) {
            if (denomination.key.equals(normalized)) {
                return denomination;
            }
        }
        return switch (normalized) {
            case "bronze", "copper", "small" -> B10;
            case "silver", "medium" -> B100;
            case "gold", "big", "large" -> L1;
            case "platinum", "huge" -> L10;
            default -> parseNumber(normalized);
        };
    }

    private static Denomination parseNumber(String normalized) {
        try {
            return atMostMangos(Long.parseLong(normalized));
        } catch (NumberFormatException ignored) {
            return C5;
        }
    }

    public static Denomination atMostMangos(long mangos) {
        long cents = mangos * CENTS_PER_MANGO;
        Denomination best = C5;
        for (Denomination denomination : values()) {
            if (denomination.cents <= cents && denomination.cents > best.cents) {
                best = denomination;
            }
        }
        return best;
    }

    public static Denomination nearestToMangos(long mangos) {
        long cents = mangos * CENTS_PER_MANGO;
        Denomination best = C5;
        long bestDistance = Long.MAX_VALUE;
        for (Denomination denomination : DESCENDING) {
            long distance = Math.abs(denomination.cents - cents);
            if (distance < bestDistance) {
                best = denomination;
                bestDistance = distance;
            }
        }
        return best;
    }

    public Denomination lower() {
        Denomination best = null;
        for (Denomination denomination : values()) {
            if (denomination.cents < cents && (best == null || denomination.cents > best.cents)) {
                best = denomination;
            }
        }
        return best;
    }
}
