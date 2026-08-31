package nadiendev.ultimatemodadditions.currency;

import java.util.Locale;

public enum Denomination {

    D10("10", 10),
    D20("20", 20),
    D50("50", 50),
    D100("100", 100),
    D200("200", 200),
    D500("500", 500),
    D1000("1000", 1_000),
    D10000("10000", 10_000),
    D20000("20000", 20_000);

    public static final Denomination[] DESCENDING;

    static {
        Denomination[] values = values().clone();
        java.util.Arrays.sort(values, (a, b) -> Integer.compare(b.value, a.value));
        DESCENDING = values;
    }

    private final String key;
    private final int value;

    Denomination(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public int value() {
        return value;
    }

    public String itemName() {
        return "coin_" + key;
    }

    public static Denomination byKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return D10;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (Denomination denomination : values()) {
            if (denomination.key.equals(normalized)) {
                return denomination;
            }
        }
        return switch (normalized) {
            case "bronze", "copper", "small" -> D10;
            case "silver", "medium" -> D100;
            case "gold", "big", "large" -> D1000;
            case "platinum", "huge" -> D10000;
            default -> parseNumber(normalized);
        };
    }

    private static Denomination parseNumber(String normalized) {
        try {
            int parsed = Integer.parseInt(normalized);
            Denomination best = D10;
            for (Denomination denomination : values()) {
                if (denomination.value <= parsed && denomination.value > best.value) {
                    best = denomination;
                }
            }
            return best;
        } catch (NumberFormatException ignored) {
            return D10;
        }
    }

    public Denomination lower() {
        Denomination best = null;
        for (Denomination denomination : values()) {
            if (denomination.value < value && (best == null || denomination.value > best.value)) {
                best = denomination;
            }
        }
        return best;
    }
}
