package nadiendev.ultimatemodadditions.coliseum;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ColiseumPortalBlock extends Block {

    public static final MapCodec<ColiseumPortalBlock> CODEC = simpleCodec(ColiseumPortalBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape X_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape Z_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

    public ColiseumPortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean alongPortal = direction.getAxis() == Direction.Axis.Y
                || direction.getAxis() == axis;
        if (!alongPortal) {
            return state;
        }
        boolean neighbourHolds = neighbour.is(this) || ColiseumPortalShape.isFrame(neighbour);
        return neighbourHolds ? state : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.isOnPortalCooldown()) {
            return;
        }
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        ColiseumPortal.travel(player);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int index = 0; index < 4; index++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();
            double dx = (random.nextFloat() - 0.5D) * 0.4D;
            double dy = (random.nextFloat() - 0.5D) * 0.4D;
            double dz = (random.nextFloat() - 0.5D) * 0.4D;
            if (state.getValue(AXIS) == Direction.Axis.X) {
                z = pos.getZ() + 0.5D + 0.25D * (random.nextBoolean() ? 1 : -1);
                dz = random.nextFloat() * 2.0F * (random.nextBoolean() ? 1 : -1);
            } else {
                x = pos.getX() + 0.5D + 0.25D * (random.nextBoolean() ? 1 : -1);
                dx = random.nextFloat() * 2.0F * (random.nextBoolean() ? 1 : -1);
            }
            level.addParticle(ParticleTypes.PORTAL, x, y, z, dx, dy, dz);
        }
    }

    public static BlockBehaviour.Properties portalProperties() {
        return BlockBehaviour.Properties.of()
                .noCollission()
                .noLootTable()
                .lightLevel(state -> 11)
                .strength(-1.0F, 3600000.0F)
                .sound(net.minecraft.world.level.block.SoundType.GLASS)
                .pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK);
    }
}
