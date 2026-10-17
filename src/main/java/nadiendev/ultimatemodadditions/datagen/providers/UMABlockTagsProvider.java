package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.registry.UMABlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class UMABlockTagsProvider extends BlockTagsProvider {

    public UMABlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                ExistingFileHelper existingFileHelper) {
        super(output, registries, UMA.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(UMABlocks.ATM.get());
        tag(BlockTags.NEEDS_STONE_TOOL).add(UMABlocks.ATM.get());
    }
}
