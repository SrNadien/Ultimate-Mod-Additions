package nadiendev.ultimatemodadditions.data;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class RuntimeDataSink {

    private boolean mobRulesDirty;

    public RuntimeDataSink addDifficulty(ResourceLocation id, DifficultyProfile profile) {
        UMAData.DIFFICULTIES.values().put(id, profile);
        return this;
    }

    public RuntimeDataSink addMobRule(ResourceLocation id, MobRule rule) {
        UMAData.MOB_RULES.values().put(id, rule);
        mobRulesDirty = true;
        return this;
    }

    public RuntimeDataSink addLootBag(ResourceLocation id, LootBagDefinition bag) {
        UMAData.LOOT_BAGS.values().put(id, bag);
        return this;
    }

    public RuntimeDataSink addBagAddition(ResourceLocation id, LootBagAddition addition) {
        Map<ResourceLocation, LootBagDefinition> bags = UMAData.LOOT_BAGS.values();
        for (Map.Entry<ResourceLocation, LootBagDefinition> entry : bags.entrySet()) {
            if (addition.targets(entry.getKey(), entry.getValue().tier())) {
                entry.setValue(addition.applyTo(entry.getValue()));
            }
        }
        return this;
    }

    public RuntimeDataSink addTrade(ResourceLocation id, TradeOffer offer) {
        UMAData.TRADES.values().put(id, offer);
        return this;
    }

    public RuntimeDataSink removeMobRule(ResourceLocation id) {
        if (UMAData.MOB_RULES.values().remove(id) != null) {
            mobRulesDirty = true;
        }
        return this;
    }

    boolean mobRulesDirty() {
        return mobRulesDirty;
    }
}
