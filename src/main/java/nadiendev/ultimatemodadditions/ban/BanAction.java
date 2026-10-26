package nadiendev.ultimatemodadditions.ban;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum BanAction {
    CRAFT("craft"),
    SMELT("smelt"),
    SMITH("smith"),
    PICKUP("pickup"),
    DROP("drop"),
    EQUIP("equip"),
    PLACE("place"),
    BREAK("break"),
    USE("use"),
    CLICK("click"),
    INTERACT("interact"),
    CONSUME("consume"),
    ATTACK("attack"),
    ENTITY_INTERACT("entityinteract"),
    ENTITY_DROP("entitydrop"),
    HOLD("hold"),
    SWAP("swap"),
    DELETE("delete"),
    RENAME("rename"),
    ARMORSTAND("armorstand");

    private final String id;

    BanAction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static BanAction byName(String text) {
        String value = text.trim().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
        value = switch (value) {
            case "wear" -> "equip";
            case "hit" -> "attack";
            case "armorstandplace", "armorstandtake" -> "armorstand";
            default -> value;
        };
        for (BanAction action : values()) {
            if (action.id.equals(value)) {
                return action;
            }
        }
        return null;
    }

    public static Set<BanAction> parse(String text) {
        Set<BanAction> result = EnumSet.noneOf(BanAction.class);
        boolean any = false;
        for (String token : text.split(",")) {
            String value = token.trim();
            if (value.isEmpty()) {
                continue;
            }
            boolean negate = value.startsWith("!");
            if (negate) {
                value = value.substring(1).trim();
            }
            Set<BanAction> picked;
            if (value.equals("*") || value.equalsIgnoreCase("all")) {
                picked = EnumSet.allOf(BanAction.class);
            } else {
                BanAction action = byName(value);
                if (action == null) {
                    return null;
                }
                picked = EnumSet.of(action);
            }
            if (negate) {
                result.removeAll(picked);
            } else {
                result.addAll(picked);
            }
            any = true;
        }
        return any ? result : null;
    }

    public static String describe(Set<BanAction> actions) {
        if (actions.size() == values().length) {
            return "all";
        }
        StringBuilder text = new StringBuilder();
        for (BanAction action : actions) {
            if (text.length() > 0) {
                text.append(',');
            }
            text.append(action.id);
        }
        return text.toString();
    }

    public static int mask(Set<BanAction> actions) {
        int mask = 0;
        for (BanAction action : actions) {
            mask |= 1 << action.ordinal();
        }
        return mask;
    }

    public static Set<BanAction> fromMask(int mask) {
        Set<BanAction> actions = EnumSet.noneOf(BanAction.class);
        for (BanAction action : values()) {
            if ((mask & (1 << action.ordinal())) != 0) {
                actions.add(action);
            }
        }
        return actions;
    }
}
