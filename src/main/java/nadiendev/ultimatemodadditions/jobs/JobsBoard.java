package nadiendev.ultimatemodadditions.jobs;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class JobsBoard extends SavedData {

    private static final String NAME = "ultimatemodadditions_jobs_board";
    private static final String ENTRIES = "entries";

    public record Entry(UUID player, String name, int level, double xp, double earned) {
    }

    private final Map<ResourceLocation, Map<UUID, Entry>> board = new HashMap<>();

    public static JobsBoard get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(JobsBoard::new, JobsBoard::load, null), NAME);
    }

    private static JobsBoard load(CompoundTag tag, HolderLookup.Provider registries) {
        JobsBoard loaded = new JobsBoard();
        ListTag entries = tag.getList(ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            ResourceLocation job = ResourceLocation.tryParse(entry.getString("job"));
            if (job == null) {
                continue;
            }
            UUID player = NbtUtils.loadUUID(entry.get("player"));
            loaded.board.computeIfAbsent(job, key -> new HashMap<>()).put(player, new Entry(player,
                    entry.getString("name"), entry.getInt("level"), entry.getDouble("xp"), entry.getDouble("earned")));
        }
        return loaded;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        board.forEach((job, players) -> players.values().forEach(value -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("job", job.toString());
            entry.put("player", NbtUtils.createUUID(value.player()));
            entry.putString("name", value.name());
            entry.putInt("level", value.level());
            entry.putDouble("xp", value.xp());
            entry.putDouble("earned", value.earned());
            entries.add(entry);
        }));
        tag.put(ENTRIES, entries);
        return tag;
    }

    public void update(ResourceLocation job, UUID player, String name, JobProgress progress) {
        board.computeIfAbsent(job, key -> new HashMap<>()).put(player,
                new Entry(player, name, progress.level(), progress.xp(), progress.earned()));
        setDirty();
    }

    public void remove(ResourceLocation job, UUID player) {
        Map<UUID, Entry> players = board.get(job);
        if (players != null && players.remove(player) != null) {
            setDirty();
        }
    }

    public List<Entry> top(ResourceLocation job, int limit) {
        List<Entry> entries = new ArrayList<>(board.getOrDefault(job, Map.of()).values());
        entries.sort(Comparator.comparingInt(Entry::level).thenComparingDouble(Entry::xp).reversed());
        return entries.subList(0, Math.min(limit, entries.size()));
    }
}
