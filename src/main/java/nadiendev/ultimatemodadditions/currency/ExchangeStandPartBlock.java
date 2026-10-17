package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.registry.UMABlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ExchangeStandPartBlock extends Block {

    public static final IntegerProperty OFFSET_X = IntegerProperty.create("offset_x", 0, 4);
    public static final IntegerProperty OFFSET_Y = IntegerProperty.create("offset_y", 0, 2);
    public static final IntegerProperty OFFSET_Z = IntegerProperty.create("offset_z", 0, 4);

    private static final int CENTER = 2;

    public ExchangeStandPartBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OFFSET_X, CENTER).setValue(OFFSET_Y, 0)
                .setValue(OFFSET_Z, CENTER));
    }

    public static BlockState stateFor(int dx, int dy, int dz) {
        return UMABlocks.EXCHANGE_STAND_PART.get().defaultBlockState()
                .setValue(OFFSET_X, dx + CENTER).setValue(OFFSET_Y, dy).setValue(OFFSET_Z, dz + CENTER);
    }

    public static BlockPos mainOf(BlockState state, BlockPos pos) {
        return pos.offset(CENTER - state.getValue(OFFSET_X), -state.getValue(OFFSET_Y),
                CENTER - state.getValue(OFFSET_Z));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OFFSET_X, OFFSET_Y, OFFSET_Z);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int dx = state.getValue(OFFSET_X) - CENTER;
        int dz = state.getValue(OFFSET_Z) - CENTER;
        double minX = dx == -2 ? 8.0D : 0.0D;
        double maxX = dx == 2 ? 8.0D : 16.0D;
        double minZ = dz == -2 ? 8.0D : 0.0D;
        double maxZ = dz == 2 ? 8.0D : 16.0D;
        return Block.box(minX, 0.0D, minZ, maxX, 16.0D, maxZ);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        BlockPos main = mainOf(state, pos);
        BlockState mainState = level.getBlockState(main);
        if (mainState.is(UMABlocks.EXCHANGE_STAND.get())) {
            return mainState.useWithoutItem(level, player, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos main = mainOf(state, pos);
        if (!level.isClientSide && level.getBlockState(main).is(UMABlocks.EXCHANGE_STAND.get())) {
            level.destroyBlock(main, !player.isCreative(), player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            BlockPos main = mainOf(state, pos);
            if (level.getBlockState(main).is(UMABlocks.EXCHANGE_STAND.get())) {
                level.destroyBlock(main, true);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(UMABlocks.EXCHANGE_STAND_ITEM.get());
    }
}
