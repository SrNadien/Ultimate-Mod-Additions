package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ColiseumArena {

    private static final int WALL_HEIGHT = 5;
    private static final int TIERS = 5;
    private static final int TIER_DEPTH = 3;
    private static final int TIER_RISE = 2;
    private static final int OUTER_THICKNESS = 3;
    private static final int GATE_HALF_WIDTH = 1;
    private static final int GATE_HEIGHT = 4;

    private ColiseumArena() {
    }

    public static int floorY() {
        return UMAConfig.COMMON.coliseumFloorY.get();
    }

    public static int arenaRadius() {
        return UMAConfig.COMMON.coliseumArenaRadius.get();
    }

    private static int seatingStart() {
        return arenaRadius() + 3;
    }

    private static int outerInner() {
        return seatingStart() + TIERS * TIER_DEPTH;
    }

    private static int outerRadius() {
        return outerInner() + OUTER_THICKNESS;
    }

    public static int structureRadius() {
        return outerRadius();
    }

    public static BlockPos gatewayPos() {
        return new BlockPos(-1, floorY() - 1, -(arenaRadius() - 1));
    }

    public static BlockPos spawnPos() {
        return new BlockPos(0, floorY(), -(arenaRadius() - 4));
    }

    public static boolean insideArena(BlockPos pos) {
        int radius = outerRadius() + 4;
        return pos.getX() * pos.getX() + pos.getZ() * pos.getZ() <= radius * radius;
    }

    public static void build(ServerLevel level) {
        long started = System.currentTimeMillis();
        int floor = floorY();
        int arena = arenaRadius();

        foundation(level, floor);
        arenaFloor(level, floor, arena);
        arenaWall(level, floor, arena);
        seating(level, floor);
        outerWall(level, floor);
        gates(level, floor, arena);
        decorate(level, floor, arena);

        ColiseumPortal.buildReturnPortal(level, gatewayPos());

        int spawners = ColiseumSpawners.enabled() ? ColiseumSpawners.place(level) : 0;

        UMA.LOGGER.info("Built the Coliseum arena in {} ms with {} spawners",
                System.currentTimeMillis() - started, spawners);
    }

    private static void foundation(ServerLevel level, int floor) {
        int radius = outerRadius();
        for (int y = floor - 6; y < floor - 1; y++) {
            BlockState state = y < floor - 3 ? Blocks.DEEPSLATE_BRICKS.defaultBlockState()
                    : Blocks.STONE_BRICKS.defaultBlockState();
            disc(level, y, radius, state);
        }
        disc(level, floor - 6, radius + 1, Blocks.BEDROCK.defaultBlockState());
    }

    private static void arenaFloor(ServerLevel level, int floor, int arena) {
        disc(level, floor - 2, arena, Blocks.SANDSTONE.defaultBlockState());
        disc(level, floor - 1, arena, Blocks.SAND.defaultBlockState());
    }

    private static void arenaWall(ServerLevel level, int floor, int arena) {
        for (int y = floor; y < floor + WALL_HEIGHT; y++) {
            BlockState state = y == floor + WALL_HEIGHT - 1
                    ? Blocks.CHISELED_SANDSTONE.defaultBlockState()
                    : Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            ring(level, y, arena, arena + 3, state);
        }
    }

    private static void seating(ServerLevel level, int floor) {
        int base = floor + WALL_HEIGHT;
        for (int tier = 0; tier < TIERS; tier++) {
            int inner = seatingStart() + tier * TIER_DEPTH;
            int outer = inner + TIER_DEPTH;
            int top = base + tier * TIER_RISE;
            for (int y = floor - 1; y <= top; y++) {
                BlockState state = y == top ? Blocks.SMOOTH_STONE.defaultBlockState()
                        : Blocks.SANDSTONE.defaultBlockState();
                ring(level, y, inner, outer, state);
            }
            ring(level, top + 1, inner, inner + 1, Blocks.SANDSTONE_SLAB.defaultBlockState());
        }
    }

    private static void outerWall(ServerLevel level, int floor) {
        int inner = outerInner();
        int outer = outerRadius();
        int top = floor + WALL_HEIGHT + TIERS * TIER_RISE + 7;
        for (int y = floor - 1; y <= top; y++) {
            BlockState state;
            if (y == top) {
                state = Blocks.CHISELED_SANDSTONE.defaultBlockState();
            } else if ((y - floor) % 6 == 0) {
                state = Blocks.CUT_SANDSTONE.defaultBlockState();
            } else {
                state = Blocks.SANDSTONE.defaultBlockState();
            }
            ring(level, y, inner, outer, state);
        }
        carveArches(level, floor, inner, outer, top);
    }

    private static void carveArches(ServerLevel level, int floor, int inner, int outer, int top) {
        int arches = 24;
        for (int index = 0; index < arches; index++) {
            double angle = index * (Math.PI * 2.0D / arches);
            for (int deck = 0; deck < 2; deck++) {
                int base = floor + 1 + deck * 9;
                if (base + 5 >= top) {
                    break;
                }
                carveArch(level, angle, base, inner, outer);
            }
        }
    }

    private static void carveArch(ServerLevel level, double angle, int base, int inner, int outer) {
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        double perpX = -dirZ;
        double perpZ = dirX;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int radius = inner; radius <= outer; radius++) {
            for (int offset = -1; offset <= 1; offset++) {
                for (int y = base; y < base + 5; y++) {
                    int width = y >= base + 4 ? 0 : 1;
                    if (Math.abs(offset) > width) {
                        continue;
                    }
                    int x = (int) Math.round(dirX * radius + perpX * offset);
                    int z = (int) Math.round(dirZ * radius + perpZ * offset);
                    cursor.set(x, y, z);
                    level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static void gates(ServerLevel level, int floor, int arena) {
        int[][] directions = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int[] direction : directions) {
            for (int distance = arena; distance <= outerRadius() + 1; distance++) {
                for (int offset = -GATE_HALF_WIDTH; offset <= GATE_HALF_WIDTH; offset++) {
                    int x = direction[0] * distance + direction[1] * offset;
                    int z = direction[1] * distance + direction[0] * offset;
                    for (int y = floor; y < floor + GATE_HEIGHT; y++) {
                        cursor.set(x, y, z);
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                    cursor.set(x, floor - 1, z);
                    level.setBlock(cursor, Blocks.SMOOTH_SANDSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                    cursor.set(x, floor + GATE_HEIGHT, z);
                    level.setBlock(cursor, Blocks.CUT_SANDSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static void decorate(ServerLevel level, int floor, int arena) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int lampRadius = arena + 1;
        for (int index = 0; index < 16; index++) {
            double angle = index * (Math.PI * 2.0D / 16);
            int x = (int) Math.round(Math.cos(angle) * lampRadius);
            int z = (int) Math.round(Math.sin(angle) * lampRadius);
            cursor.set(x, floor + WALL_HEIGHT - 1, z);
            level.setBlock(cursor, Blocks.SEA_LANTERN.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        int pillarRadius = outerRadius() - 1;
        int pillarTop = floor + WALL_HEIGHT + TIERS * TIER_RISE + 8;
        for (int index = 0; index < 12; index++) {
            double angle = index * (Math.PI * 2.0D / 12) + Math.PI / 24.0D;
            int x = (int) Math.round(Math.cos(angle) * pillarRadius);
            int z = (int) Math.round(Math.sin(angle) * pillarRadius);
            for (int y = floor - 1; y <= pillarTop; y++) {
                cursor.set(x, y, z);
                level.setBlock(cursor, Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            cursor.set(x, pillarTop + 1, z);
            level.setBlock(cursor, Blocks.SEA_LANTERN.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        for (int index = 0; index < 8; index++) {
            double angle = index * (Math.PI * 2.0D / 8) + Math.PI / 8.0D;
            int x = (int) Math.round(Math.cos(angle) * (arena - 6));
            int z = (int) Math.round(Math.sin(angle) * (arena - 6));
            cursor.set(x, floor - 1, z);
            level.setBlock(cursor, Blocks.RED_SANDSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static void disc(ServerLevel level, int y, int radius, BlockState state) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int squared = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z > squared) {
                    continue;
                }
                cursor.set(x, y, z);
                level.setBlock(cursor, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    private static void ring(ServerLevel level, int y, int innerRadius, int outerRadius, BlockState state) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int innerSquared = innerRadius * innerRadius;
        int outerSquared = outerRadius * outerRadius;
        for (int x = -outerRadius; x <= outerRadius; x++) {
            for (int z = -outerRadius; z <= outerRadius; z++) {
                int distance = x * x + z * z;
                if (distance < innerSquared || distance > outerSquared) {
                    continue;
                }
                cursor.set(x, y, z);
                level.setBlock(cursor, state, Block.UPDATE_CLIENTS);
            }
        }
    }
}
