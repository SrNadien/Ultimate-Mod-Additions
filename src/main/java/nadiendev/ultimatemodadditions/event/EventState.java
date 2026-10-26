package nadiendev.ultimatemodadditions.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EventState extends SavedData {

    private static final String NAME = "ultimatemodadditions_events";

    private final Set<String> disabled = new HashSet<>();
    private final Map<UUID, Set<String>> flags = new HashMap<>();
    private final Map<String, Long> timers = new HashMap<>();
    private final Map<String, String> progress = new HashMap<>();

    public static EventState get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(EventState::new, EventState::load, null), NAME);
    }

    private static EventState load(CompoundTag tag, HolderLookup.Provider registries) {
        EventState state = new EventState();
        ListTag off = tag.getList("disabled", Tag.TAG_STRING);
        for (int i = 0; i < off.size(); i++) {
            state.disabled.add(off.getString(i));
        }
        CompoundTag flagTag = tag.getCompound("flags");
        for (String key : flagTag.getAllKeys()) {
            try {
                UUID id = UUID.fromString(key);
                ListTag list = flagTag.getList(key, Tag.TAG_STRING);
                Set<String> set = new HashSet<>();
                for (int i = 0; i < list.size(); i++) {
                    set.add(list.getString(i));
                }
                state.flags.put(id, set);
            } catch (IllegalArgumentException ignored) {
            }
        }
        CompoundTag timerTag = tag.getCompound("timers");
        for (String key : timerTag.getAllKeys()) {
            state.timers.put(key, timerTag.getLong(key));
        }
        CompoundTag progressTag = tag.getCompound("progress");
        for (String key : progressTag.getAllKeys()) {
            state.progress.put(key, progressTag.getString(key));
        }
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag off = new ListTag();
        disabled.forEach(id -> off.add(StringTag.valueOf(id)));
        tag.put("disabled", off);
        CompoundTag flagTag = new CompoundTag();
        flags.forEach((id, set) -> {
            ListTag list = new ListTag();
            set.forEach(flag -> list.add(StringTag.valueOf(flag)));
            flagTag.put(id.toString(), list);
        });
        tag.put("flags", flagTag);
        CompoundTag timerTag = new CompoundTag();
        timers.forEach(timerTag::putLong);
        tag.put("timers", timerTag);
        CompoundTag progressTag = new CompoundTag();
        progress.forEach(progressTag::putString);
        tag.put("progress", progressTag);
        return tag;
    }

    public boolean enabled(String event) {
        return !disabled.contains(event);
    }

    public void setEnabled(String event, boolean enabled) {
        if (enabled ? disabled.remove(event) : disabled.add(event)) {
            setDirty();
        }
    }

    public boolean flag(UUID player, String key) {
        return flags.getOrDefault(player, Set.of()).contains(key);
    }

    public void setFlag(UUID player, String key) {
        if (flags.computeIfAbsent(player, id -> new HashSet<>()).add(key)) {
            setDirty();
        }
    }

    public long timer(String key, long fallback) {
        return timers.getOrDefault(key, fallback);
    }

    public void setTimer(String key, long value) {
        timers.put(key, value);
        setDirty();
    }

    public String progress(String key) {
        return progress.get(key);
    }

    public void setProgress(String key, String value) {
        progress.put(key, value);
        setDirty();
    }

    public void removeProgress(String key) {
        if (progress.remove(key) != null) {
            setDirty();
        }
    }
}
