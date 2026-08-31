package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class ColiseumState extends SavedData {

    private static final String NAME = "uma_coliseum";

    private ResourceLocation difficulty;
    private int wave;
    private boolean arenaBuilt;

    public ColiseumState() {
    }

    public static ColiseumState get(MinecraftServer server) {
        return server.getLevel(Level.OVERWORLD).getDataStorage()
                .computeIfAbsent(new Factory<>(ColiseumState::new, ColiseumState::load), NAME);
    }

    private static ColiseumState load(CompoundTag tag, HolderLookup.Provider registries) {
        ColiseumState state = new ColiseumState();
        if (tag.contains("difficulty")) {
            state.difficulty = ResourceLocation.tryParse(tag.getString("difficulty"));
        }
        state.wave = tag.getInt("wave");
        state.arenaBuilt = tag.getBoolean("arena_built");
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        if (difficulty != null) {
            tag.putString("difficulty", difficulty.toString());
        }
        tag.putInt("wave", wave);
        tag.putBoolean("arena_built", arenaBuilt);
        return tag;
    }

    public ResourceLocation difficulty() {
        return difficulty != null ? difficulty : UMA.parse(UMAConfig.COMMON.defaultDifficulty.get());
    }

    public void setDifficulty(ResourceLocation id) {
        difficulty = id;
        setDirty();
    }

    public int wave() {
        return wave;
    }

    public void setWave(int value) {
        wave = Math.max(0, value);
        setDirty();
    }

    public boolean arenaBuilt() {
        return arenaBuilt;
    }

    public void setArenaBuilt(boolean value) {
        arenaBuilt = value;
        setDirty();
    }
}
