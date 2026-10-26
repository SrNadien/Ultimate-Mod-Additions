package nadiendev.ultimatemodadditions.ban;

import java.util.Set;

public record BanRule(ItemTarget target, Set<BanAction> actions, Selector dimensions, RuleData data, boolean disabled,
                      boolean live, String origin) {

    public boolean applies(BanAction action, String dimension) {
        return actions.contains(action) && dimensions.matches(dimension);
    }

    public boolean everywhere() {
        return dimensions.isAll();
    }
}
