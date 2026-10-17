package nadiendev.ultimatemodadditions.daily;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.DailyReward;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DailyEdits extends SavedData {

    private static final String NAME = "ultimatemodadditions_daily_edits";
    private static final String PERIODS = "periods";

    private final Map<DailyReward.Period, List<DailyReward.Entry>> custom = new EnumMap<>(DailyReward.Period.class);

    public static DailyEdits get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(DailyEdits::new, DailyEdits::load, null), NAME);
    }

    private static DailyEdits load(CompoundTag tag, HolderLookup.Provider registries) {
        DailyEdits edits = new DailyEdits();
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        ListTag periods = tag.getList(PERIODS, Tag.TAG_COMPOUND);
        for (int i = 0; i < periods.size(); i++) {
            CompoundTag entry = periods.getCompound(i);
            DailyReward.Period period = DailyReward.Period.CODEC.parse(NbtOps.INSTANCE, entry.get("period"))
                    .result().orElse(null);
            if (period == null) {
                continue;
            }
            List<DailyReward.Entry> entries = new ArrayList<>();
            ListTag list = entry.getList("entries", Tag.TAG_COMPOUND);
            for (int j = 0; j < list.size(); j++) {
                DailyReward.Entry.CODEC.parse(ops, list.get(j))
                        .resultOrPartial(error -> UMA.LOGGER.error("Skipping saved daily reward: {}", error))
                        .ifPresent(entries::add);
            }
            edits.custom.put(period, entries);
        }
        return edits;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        ListTag periods = new ListTag();
        custom.forEach((period, entries) -> {
            CompoundTag entry = new CompoundTag();
            DailyReward.Period.CODEC.encodeStart(NbtOps.INSTANCE, period).result().ifPresent(value -> entry.put("period", value));
            ListTag list = new ListTag();
            for (DailyReward.Entry reward : entries) {
                DailyReward.Entry.CODEC.encodeStart(ops, reward).result().ifPresent(list::add);
            }
            entry.put("entries", list);
            periods.add(entry);
        });
        tag.put(PERIODS, periods);
        return tag;
    }

    public List<DailyReward.Entry> custom(DailyReward.Period period) {
        return custom.get(period);
    }

    public List<DailyReward.Entry> editable(DailyReward.Period period, List<DailyReward.Entry> current) {
        List<DailyReward.Entry> list = custom.computeIfAbsent(period, key -> new ArrayList<>(current));
        setDirty();
        return list;
    }

    public void clear() {
        custom.clear();
        setDirty();
    }
}
