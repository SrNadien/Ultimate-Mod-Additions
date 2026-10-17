package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.registry.UMABlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class UMABlockLootProvider extends BlockLootSubProvider {

    public UMABlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(UMABlocks.EXCHANGE_STAND.get());
        dropSelf(UMABlocks.ATM.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(UMABlocks.EXCHANGE_STAND.get(), UMABlocks.ATM.get());
    }
}
