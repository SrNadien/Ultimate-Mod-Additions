package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.currency.CashChestModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

public class UMALootModifierProvider extends GlobalLootModifierProvider {

    public UMALootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, UMA.MODID);
    }

    @Override
    protected void start() {
        add("cash_in_chests", new CashChestModifier(new LootItemCondition[0]));
    }
}
