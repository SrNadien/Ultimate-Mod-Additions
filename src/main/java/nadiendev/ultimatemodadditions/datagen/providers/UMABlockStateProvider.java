package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.registry.UMABlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class UMABlockStateProvider extends BlockStateProvider {

    private static final int STALL_TEXTURES = 19;
    private static final int ATM_TEXTURES = 1;

    public UMABlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UMA.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        BlockModelBuilder stand = UMAObjModels.apply(
                models().getBuilder("exchange_stand").parent(blockParent()),
                modLoc("models/block/stall.obj"), modLoc("block/stall"), STALL_TEXTURES, true)
                .texture("particle", modLoc("block/stall/t0"));
        horizontalBlock(UMABlocks.EXCHANGE_STAND.get(), state -> stand, 0);
        simpleBlock(UMABlocks.EXCHANGE_STAND_PART.get(),
                models().getBuilder("exchange_stand_part").texture("particle", mcLoc("block/oak_planks")));

        BlockModelBuilder atm = UMAObjModels.apply(
                models().getBuilder("atm").parent(blockParent()),
                modLoc("models/block/atm.obj"), modLoc("block/atm"), ATM_TEXTURES, false)
                .texture("particle", modLoc("block/atm/t0"));
        horizontalBlock(UMABlocks.ATM.get(), state -> atm, 180);
    }

    private ModelFile blockParent() {
        return new ModelFile.UncheckedModelFile(mcLoc("block/block"));
    }
}
