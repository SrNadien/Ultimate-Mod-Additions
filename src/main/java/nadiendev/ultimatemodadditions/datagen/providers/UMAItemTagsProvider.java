package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import nadiendev.ultimatemodadditions.registry.UMATags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class UMAItemTagsProvider extends ItemTagsProvider {

    public UMAItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                               CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
                               ExistingFileHelper existingFileHelper) {
        super(output, registries, blockTags, UMA.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(UMATags.NOT_PURCHASABLE).add(UMAItems.COLISEUM_KEY.get());
    }
}
