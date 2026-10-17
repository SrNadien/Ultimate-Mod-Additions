package nadiendev.ultimatemodadditions.currency;

import com.mojang.serialization.MapCodec;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.registry.UMABlocks;
import nadiendev.ultimatemodadditions.shop.ShopService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class ExchangeStandBlock extends HorizontalDirectionalBlock {

    private static final int HEIGHT = 3;

    public static final MapCodec<ExchangeStandBlock> CODEC = simpleCodec(ExchangeStandBlock::new);

    public ExchangeStandBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    public static List<BlockPos> footprint(BlockPos main, Direction facing) {
        boolean wideX = facing.getAxis() == Direction.Axis.Z;
        int halfX = wideX ? 2 : 1;
        int halfZ = wideX ? 1 : 2;
        List<BlockPos> cells = new ArrayList<>();
        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dy = 0; dy < HEIGHT; dy++) {
                for (int dz = -halfZ; dz <= halfZ; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        cells.add(main.offset(dx, dy, dz));
                    }
                }
            }
        }
        return cells;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Level level = context.getLevel();
        for (BlockPos cell : footprint(context.getClickedPos(), facing)) {
            if (level.isOutsideBuildHeight(cell) || !level.getBlockState(cell).canBeReplaced()) {
                return null;
            }
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) {
            return;
        }
        for (BlockPos cell : footprint(pos, state.getValue(FACING))) {
            level.setBlock(cell, ExchangeStandPartBlock.stateFor(cell.getX() - pos.getX(), cell.getY() - pos.getY(),
                    cell.getZ() - pos.getZ()), 3);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            for (BlockPos cell : footprint(pos, state.getValue(FACING))) {
                if (level.getBlockState(cell).is(UMABlocks.EXCHANGE_STAND_PART.get())) {
                    level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (UMAConfig.COMMON.shopEnabled.get()) {
                ShopService.openStand(serverPlayer);
                return InteractionResult.CONSUME;
            }
            BankService.sendOwn(serverPlayer);
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new ExchangeMenu(id, inventory),
                    Component.translatable("container.ultimatemodadditions.exchange")));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
