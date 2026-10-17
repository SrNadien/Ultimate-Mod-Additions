package nadiendev.ultimatemodadditions.datagen.providers;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.currency.BagTier;
import nadiendev.ultimatemodadditions.currency.Denomination;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class UMAItemModelProvider extends ItemModelProvider {

    private static final int CARD_TEXTURES = 2;
    private static final int WALLET_TEXTURES = 1;

    public UMAItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, UMA.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (Denomination denomination : Denomination.values()) {
            flat(denomination.itemName());
        }
        flat("coliseum_key");
        withExistingParent("flashero_spawn_egg", mcLoc("item/template_spawn_egg"));
        for (BagTier tier : BagTier.values()) {
            flat(tier.itemName());
        }

        ItemModelBuilder card = UMAObjModels.apply(
                getBuilder("debit_card").parent(new ModelFile.UncheckedModelFile(mcLoc("block/block"))),
                modLoc("models/item/card.obj"), modLoc("item/card"), CARD_TEXTURES, true);
        card.guiLight(BlockModel.GuiLight.FRONT);
        display(card, ItemDisplayContext.GUI, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.001F);
        display(card, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 0.55F);
        display(card, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, 0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 0.55F);
        display(card, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0.0F, -90.0F, 25.0F, 1.13F, 3.2F, 1.13F, 0.68F);
        display(card, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0.0F, 90.0F, -25.0F, 1.13F, 3.2F, 1.13F, 0.68F);
        display(card, ItemDisplayContext.GROUND, 0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.5F);
        display(card, ItemDisplayContext.FIXED, 0.0F, 180.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);

        ItemModelBuilder wallet = UMAObjModels.apply(
                getBuilder("wallet").parent(new ModelFile.UncheckedModelFile(mcLoc("block/block"))),
                modLoc("models/item/wallet.obj"), modLoc("item/wallet"), WALLET_TEXTURES, false);
        display(wallet, ItemDisplayContext.GUI, 35.0F, 225.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.85F);
        display(wallet, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 75.0F, 45.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.45F);
        display(wallet, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, 75.0F, 45.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.45F);
        display(wallet, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 10.0F, 40.0F, 0.0F, -3.5F, 5.0F, 0.0F, 0.45F);
        display(wallet, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 10.0F, -40.0F, 0.0F, 3.5F, 5.0F, 0.0F, 0.45F);
        display(wallet, ItemDisplayContext.GROUND, 0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 0.0F, 0.4F);
        display(wallet, ItemDisplayContext.FIXED, 90.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.55F);

        ItemModelBuilder stand = getBuilder("exchange_stand")
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/exchange_stand")));
        display(stand, ItemDisplayContext.GUI, 30.0F, 225.0F, 0.0F, 0.0F, -2.0F, 0.0F, 0.19F);
        display(stand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 75.0F, 45.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.1F);
        display(stand, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, 75.0F, 45.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.1F);
        display(stand, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0.0F, 45.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.125F);
        display(stand, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0.0F, 225.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.125F);
        display(stand, ItemDisplayContext.GROUND, 0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.1F);
        display(stand, ItemDisplayContext.FIXED, 0.0F, 0.0F, 0.0F, 0.0F, -3.0F, 0.0F, 0.16F);

        ItemModelBuilder atm = getBuilder("atm").parent(new ModelFile.UncheckedModelFile(modLoc("block/atm")));
        display(atm, ItemDisplayContext.GUI, 30.0F, 225.0F, 0.0F, 0.0F, -2.5F, 0.0F, 0.34F);
        display(atm, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 75.0F, 45.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.18F);
        display(atm, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, 75.0F, 45.0F, 0.0F, 0.0F, 2.5F, 0.0F, 0.18F);
        display(atm, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0.0F, 45.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2F);
        display(atm, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0.0F, 225.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2F);
        display(atm, ItemDisplayContext.GROUND, 0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 0.0F, 0.13F);
        display(atm, ItemDisplayContext.FIXED, 0.0F, 0.0F, 0.0F, 0.0F, -3.0F, 0.0F, 0.27F);
    }

    private static void display(ItemModelBuilder model, ItemDisplayContext context, float rotationX, float rotationY,
                                float rotationZ, float x, float y, float z, float scale) {
        model.transforms().transform(context)
                .rotation(rotationX, rotationY, rotationZ)
                .translation(x, y, z)
                .scale(scale)
                .end();
    }

    private void flat(String name) {
        withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name));
    }
}
