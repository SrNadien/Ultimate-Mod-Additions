package nadiendev.ultimatemodadditions.currency;

import net.minecraft.world.item.Rarity;

import java.util.Locale;

public enum BagTier {

    COMMON("common", Rarity.COMMON, 0xBEBEBE, true),
    UNCOMMON("uncommon", Rarity.UNCOMMON, 0x6CD66C, true),
    EPIC("epic", Rarity.RARE, 0xBA5CFF, false),
    MYTHIC("mythic", Rarity.EPIC, 0xFF5C5C, false),
    LEGENDARY("legendary", Rarity.EPIC, 0xFFB628, false),
    OMEGA("omega", Rarity.EPIC, 0x46EBFF, false),
    FOOD("food", Rarity.COMMON, 0xE49242, false),
    ENCHANTMENT("enchantment", Rarity.UNCOMMON, 0x7E8AFF, false);

    private final String key;
    private final Rarity rarity;
    private final int color;
    private final boolean purchasable;

    BagTier(String key, Rarity rarity, int color, boolean purchasable) {
        this.key = key;
        this.rarity = rarity;
        this.color = color;
        this.purchasable = purchasable;
    }

    public String key() {
        return key;
    }

    public Rarity rarity() {
        return rarity;
    }

    public int color() {
        return color;
    }

    public boolean purchasable() {
        return purchasable;
    }

    public String itemName() {
        return "loot_bag_" + key;
    }

    public static BagTier byKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return COMMON;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (BagTier tier : values()) {
            if (tier.key.equals(normalized)) {
                return tier;
            }
        }
        return switch (normalized) {
            case "rare" -> EPIC;
            case "mythical", "mitico", "mythic_rare" -> MYTHIC;
            case "comida" -> FOOD;
            case "encantamiento", "enchant", "enchanted" -> ENCHANTMENT;
            default -> COMMON;
        };
    }
}
