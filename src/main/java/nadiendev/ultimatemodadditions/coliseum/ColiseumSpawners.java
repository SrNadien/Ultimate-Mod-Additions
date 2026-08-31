package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public final class ColiseumSpawners {

    private ColiseumSpawners() {
    }

    public static boolean enabled() {
        return UMAConfig.COMMON.coliseumSpawners.get();
    }

    public static List<EntityType<?>> types() {
        List<EntityType<?>> types = new ArrayList<>();
        for (Object entry : UMAConfig.COMMON.coliseumSpawnerEntities.get()) {
            ResourceLocation id = ResourceLocation.tryParse(String.valueOf(entry));
            if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                UMA.LOGGER.warn("Coliseum spawner entity {} does not exist, skipping it.", entry);
                continue;
            }
            types.add(BuiltInRegistries.ENTITY_TYPE.get(id));
        }
        return types;
    }

    public static int place(ServerLevel level) {
        clear(level);
        List<EntityType<?>> types = types();
        int wanted = UMAConfig.COMMON.coliseumSpawnerCount.get();
        if (types.isEmpty() || wanted <= 0) {
            return 0;
        }

        int floor = ColiseumArena.floorY();
        int radius = Math.max(3, ColiseumArena.arenaRadius() - 5);
        BlockPos avoid = ColiseumArena.spawnPos();
        int placed = 0;

        for (int index = 0; index < wanted; index++) {
            double angle = (index + 0.5D) * (Math.PI * 2.0D / wanted);
            int x = (int) Math.round(Math.cos(angle) * radius);
            int z = (int) Math.round(Math.sin(angle) * radius);
            BlockPos pos = new BlockPos(x, floor, z);
            if (pos.distSqr(avoid) < 9) {
                continue;
            }
            if (place(level, pos, types.get(index % types.size()))) {
                placed++;
            }
        }
        return placed;
    }

    private static boolean place(ServerLevel level, BlockPos pos, EntityType<?> type) {
        level.setBlock(pos.below(), Blocks.CHISELED_SANDSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
        if (!(level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)) {
            UMA.LOGGER.warn("Could not configure the Coliseum spawner at {}", pos);
            return false;
        }

        CompoundTag entity = new CompoundTag();
        entity.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        CompoundTag spawnData = new CompoundTag();
        spawnData.put("entity", entity);

        UMAConfig.Common config = UMAConfig.COMMON;
        int min = config.coliseumSpawnerMinDelay.get();
        int max = Math.max(min, config.coliseumSpawnerMaxDelay.get());

        CompoundTag tag = new CompoundTag();
        tag.put("SpawnData", spawnData);
        tag.putShort("Delay", (short) min);
        tag.putShort("MinSpawnDelay", (short) min);
        tag.putShort("MaxSpawnDelay", (short) max);
        tag.putShort("SpawnCount", (short) (int) config.coliseumSpawnerAmount.get());
        tag.putShort("MaxNearbyEntities", (short) (int) config.coliseumSpawnerMaxNearby.get());
        tag.putShort("RequiredPlayerRange", (short) (int) config.coliseumSpawnerPlayerRange.get());
        tag.putShort("SpawnRange", (short) (int) config.coliseumSpawnerRange.get());

        spawner.getSpawner().load(level, pos, tag);
        spawner.setChanged();
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        return true;
    }

    public static int clear(ServerLevel level) {
        int removed = 0;
        for (BlockPos pos : region()) {
            if (level.getBlockState(pos).is(Blocks.SPAWNER)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                removed++;
            }
        }
        return removed;
    }

    public static int count(ServerLevel level) {
        int found = 0;
        for (BlockPos pos : region()) {
            if (level.getBlockState(pos).is(Blocks.SPAWNER)) {
                found++;
            }
        }
        return found;
    }

    private static Iterable<BlockPos> region() {
        int radius = ColiseumArena.structureRadius() + 4;
        int floor = ColiseumArena.floorY();
        return BlockPos.betweenClosed(new BlockPos(-radius, floor - 6, -radius),
                new BlockPos(radius, floor + 26, radius));
    }
}
