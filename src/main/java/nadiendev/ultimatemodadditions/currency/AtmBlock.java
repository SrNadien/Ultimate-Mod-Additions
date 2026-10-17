package nadiendev.ultimatemodadditions.currency;

import com.mojang.serialization.MapCodec;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AtmBlock extends HorizontalDirectionalBlock {

    private static final VoxelShape NORTH_SOUTH = Block.box(1.05D, 0.0D, 1.95D, 14.95D, 16.0D, 14.05D);
    private static final VoxelShape EAST_WEST = Block.box(1.95D, 0.0D, 1.05D, 14.05D, 16.0D, 14.95D);

    public static final MapCodec<AtmBlock> CODEC = simpleCodec(AtmBlock::new);

    public AtmBlock(Properties properties) {
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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? EAST_WEST : NORTH_SOUTH;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (!UMAConfig.COMMON.bankEnabled.get()) {
            serverPlayer.displayClientMessage(Component.translatable("message.ultimatemodadditions.bank_disabled")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.CONSUME;
        }
        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, owner) -> new AtmMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                Component.translatable("container.ultimatemodadditions.atm")));
        BankService.sendOwn(serverPlayer);
        BankService.sendAtm(serverPlayer, null);
        return InteractionResult.CONSUME;
    }
}
