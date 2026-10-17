package nadiendev.ultimatemodadditions.jobs;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.Map;

public final class JobsBoosts extends SavedData {

    public static final String ALL = "all";

    private static final String NAME = "ultimatemodadditions_jobs_boosts";
    private static final String ENTRIES = "entries";

    public record Boost(double multiplier, long until) {
        public boolean active(long now) {
            return until == 0L || until > now;
        }
    }

    private final Map<String, Boost> boosts = new LinkedHashMap<>();

    public static JobsBoosts get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(JobsBoosts::new, JobsBoosts::load, null), NAME);
    }

    private static JobsBoosts load(CompoundTag tag, HolderLookup.Provider registries) {
        JobsBoosts loaded = new JobsBoosts();
        ListTag entries = tag.getList(ENTRIES, Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            loaded.boosts.put(entry.getString("key"), new Boost(entry.getDouble("multiplier"), entry.getLong("until")));
        }
        return loaded;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        boosts.forEach((key, boost) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("key", key);
            entry.putDouble("multiplier", boost.multiplier());
            entry.putLong("until", boost.until());
            entries.add(entry);
        });
        tag.put(ENTRIES, entries);
        return tag;
    }

    public void set(String key, double multiplier, long until) {
        boosts.put(key, new Boost(multiplier, until));
        setDirty();
    }

    public boolean clear(String key) {
        boolean removed = boosts.remove(key) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public Map<String, Boost> active() {
        long now = System.currentTimeMillis();
        if (boosts.values().removeIf(boost -> !boost.active(now))) {
            setDirty();
        }
        return boosts;
    }

    public double multiplier(String jobKey) {
        Map<String, Boost> current = active();
        double total = 1.0D;
        Boost everyone = current.get(ALL);
        if (everyone != null) {
            total *= everyone.multiplier();
        }
        Boost single = current.get(jobKey);
        if (single != null) {
            total *= single.multiplier();
        }
        return total;
    }
}
