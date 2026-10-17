package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.coliseum.ColiseumPortalBlock;
import nadiendev.ultimatemodadditions.currency.AtmBlock;
import nadiendev.ultimatemodadditions.currency.ExchangeStandBlock;
import nadiendev.ultimatemodadditions.currency.ExchangeStandPartBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UMABlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UMA.MODID);

    public static final DeferredBlock<ColiseumPortalBlock> COLISEUM_PORTAL = BLOCKS.registerBlock("coliseum_portal",
            ColiseumPortalBlock::new, ColiseumPortalBlock.portalProperties());

    public static final DeferredBlock<ExchangeStandBlock> EXCHANGE_STAND = BLOCKS.registerBlock("exchange_stand",
            ExchangeStandBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(3.0F, 9.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final DeferredBlock<ExchangeStandPartBlock> EXCHANGE_STAND_PART = BLOCKS.registerBlock(
            "exchange_stand_part", ExchangeStandPartBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(3.0F, 9.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK));

    public static final DeferredItem<BlockItem> EXCHANGE_STAND_ITEM = UMAItems.ITEMS.registerSimpleBlockItem(EXCHANGE_STAND);

    public static final DeferredBlock<AtmBlock> ATM = BLOCKS.registerBlock("atm",
            AtmBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(4.0F, 12.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops());

    public static final DeferredItem<BlockItem> ATM_ITEM = UMAItems.ITEMS.registerSimpleBlockItem(ATM);

    private UMABlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
