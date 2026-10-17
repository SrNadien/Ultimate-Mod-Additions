package nadiendev.ultimatemodadditions.datagen.providers;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;

final class UMAObjModels {

    private UMAObjModels() {
    }

    static <T extends ModelBuilder<T>> T apply(T model, ResourceLocation obj, ResourceLocation textureFolder,
                                               int textureCount, boolean palette) {
        model.customLoader(ObjModelBuilder::begin)
                .modelLocation(obj)
                .flipV(true)
                .automaticCulling(false)
                .shadeQuads(true)
                .end();
        for (int index = 0; index < textureCount; index++) {
            model.texture("t" + index, textureFolder.withSuffix("/t" + index));
        }
        if (palette) {
            model.texture("palette", textureFolder.withSuffix("/palette"));
        }
        return model;
    }
}
