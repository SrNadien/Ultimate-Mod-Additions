package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.registry.UMABlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public record ColiseumPortalShape(Direction.Axis axis, BlockPos bottomLeft, int width, int height) {

    private static final int MIN_WIDTH = 4;
    private static final int MAX_WIDTH = 23;
    private static final int MIN_HEIGHT = 5;
    private static final int MAX_HEIGHT = 23;

    public static boolean isFrame(BlockState state) {
        return state.is(Blocks.CRYING_OBSIDIAN);
    }

    public static boolean isPortal(BlockState state) {
        return state.is(UMABlocks.COLISEUM_PORTAL.get());
    }

    public static Optional<ColiseumPortalShape> at(LevelAccessor level, BlockPos clicked) {
        if (!isFrame(level.getBlockState(clicked))) {
            return Optional.empty();
        }
        for (Direction.Axis axis : List.of(Direction.Axis.X, Direction.Axis.Z)) {
            Optional<ColiseumPortalShape> shape = onAxis(level, clicked, axis);
            if (shape.isPresent()) {
                return shape;
            }
        }
        return Optional.empty();
    }

    private static Optional<ColiseumPortalShape> onAxis(LevelAccessor level, BlockPos clicked, Direction.Axis axis) {
        BlockPos bottomLeft = walk(level, walk(level, clicked, negative(axis)), Direction.DOWN);
        int maxWidth = run(level, bottomLeft, positive(axis), MAX_WIDTH);
        int maxHeight = run(level, bottomLeft, Direction.UP, MAX_HEIGHT);
        if (maxWidth < MIN_WIDTH || maxHeight < MIN_HEIGHT) {
            return Optional.empty();
        }

        for (int height = MIN_HEIGHT; height <= maxHeight; height++) {
            for (int width = MIN_WIDTH; width <= maxWidth; width++) {
                ColiseumPortalShape shape = new ColiseumPortalShape(axis, bottomLeft, width, height);
                if (shape.valid(level) && shape.isCorner(clicked)) {
                    return Optional.of(shape);
                }
            }
        }
        return Optional.empty();
    }

    private static Direction negative(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.WEST : Direction.NORTH;
    }

    private static Direction positive(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    private static BlockPos walk(LevelAccessor level, BlockPos from, Direction direction) {
        BlockPos cursor = from;
        for (int step = 0; step < MAX_WIDTH; step++) {
            BlockPos next = cursor.relative(direction);
            if (!isFrame(level.getBlockState(next))) {
                return cursor;
            }
            cursor = next;
        }
        return cursor;
    }

    private static int run(LevelAccessor level, BlockPos from, Direction direction, int limit) {
        int length = 0;
        BlockPos cursor = from;
        while (length < limit && isFrame(level.getBlockState(cursor))) {
            length++;
            cursor = cursor.relative(direction);
        }
        return length;
    }

    private BlockPos offset(int along, int up) {
        return axis == Direction.Axis.X
                ? bottomLeft.offset(along, up, 0)
                : bottomLeft.offset(0, up, along);
    }

    public List<BlockPos> corners() {
        return List.of(offset(0, 0), offset(width - 1, 0), offset(0, height - 1), offset(width - 1, height - 1));
    }

    public boolean isCorner(BlockPos pos) {
        return corners().contains(pos);
    }

    public boolean valid(LevelAccessor level) {
        for (int along = 0; along < width; along++) {
            if (!isFrame(level.getBlockState(offset(along, 0)))
                    || !isFrame(level.getBlockState(offset(along, height - 1)))) {
                return false;
            }
        }
        for (int up = 1; up < height - 1; up++) {
            if (!isFrame(level.getBlockState(offset(0, up)))
                    || !isFrame(level.getBlockState(offset(width - 1, up)))) {
                return false;
            }
        }
        for (int along = 1; along < width - 1; along++) {
            for (int up = 1; up < height - 1; up++) {
                BlockState inside = level.getBlockState(offset(along, up));
                if (!inside.isAir() && !isPortal(inside)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean lit(LevelAccessor level) {
        return isPortal(level.getBlockState(offset(1, 1)));
    }

    public void light(LevelAccessor level) {
        BlockState portal = UMABlocks.COLISEUM_PORTAL.get().defaultBlockState()
                .setValue(ColiseumPortalBlock.AXIS, axis);
        for (int along = 1; along < width - 1; along++) {
            for (int up = 1; up < height - 1; up++) {
                level.setBlock(offset(along, up), portal, Block.UPDATE_ALL);
            }
        }
    }

    public void extinguish(LevelAccessor level) {
        for (int along = 1; along < width - 1; along++) {
            for (int up = 1; up < height - 1; up++) {
                BlockPos pos = offset(along, up);
                if (isPortal(level.getBlockState(pos))) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }
}
