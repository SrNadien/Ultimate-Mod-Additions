package nadiendev.ultimatemodadditions.ban;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BanState extends SavedData {

    private static final String NAME = "ultimatemodadditions_bans";

    public record Entry(String item, int actions, String dimensions, String message, boolean disabled) {
    }

    private final List<Entry> entries = new ArrayList<>();
    private int version;

    public static BanState get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(BanState::new, BanState::load, null), NAME);
    }

    private static BanState load(CompoundTag tag, HolderLookup.Provider registries) {
        BanState state = new BanState();
        ListTag list = tag.getList("rules", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int actions = entry.contains("actions") ? entry.getInt("actions") : entry.getInt("modes");
            state.entries.add(new Entry(entry.getString("item"), actions, entry.getString("dimensions"),
                    entry.getString("message"), entry.getBoolean("disabled")));
        }
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag compound = new CompoundTag();
            compound.putString("item", entry.item());
            compound.putInt("actions", entry.actions());
            compound.putString("dimensions", entry.dimensions());
            compound.putString("message", entry.message());
            compound.putBoolean("disabled", entry.disabled());
            list.add(compound);
        }
        tag.put("rules", list);
        return tag;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public int version() {
        return version;
    }

    public boolean has(String item, String dimensions) {
        return entries.stream().anyMatch(entry -> entry.item().equals(item) && entry.dimensions().equals(dimensions));
    }

    public void add(String item, Set<BanAction> actions, String dimensions, String message, boolean disabled) {
        entries.add(new Entry(item, BanAction.mask(actions), dimensions == null ? "" : dimensions,
                message == null ? "" : message, disabled));
        changed();
    }

    public int remove(String item) {
        int before = entries.size();
        entries.removeIf(entry -> entry.item().equals(item));
        if (entries.size() != before) {
            changed();
        }
        return before - entries.size();
    }

    public int clearAll() {
        int count = entries.size();
        entries.clear();
        if (count > 0) {
            changed();
        }
        return count;
    }

    public void touch() {
        version++;
    }

    private void changed() {
        version++;
        setDirty();
    }
}
