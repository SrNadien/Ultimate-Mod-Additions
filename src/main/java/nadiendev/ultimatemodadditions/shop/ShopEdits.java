package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.ShopCategory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ShopEdits extends SavedData {

    private static final String NAME = "ultimatemodadditions_shop_edits";
    private static final String CUSTOM = "custom";
    private static final String HIDDEN = "hidden";

    private final Map<ResourceLocation, ShopCategory> custom = new LinkedHashMap<>();
    private final Set<ResourceLocation> hidden = new HashSet<>();

    public static ShopEdits get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(ShopEdits::new, ShopEdits::load, null), NAME);
    }

    private static ShopEdits load(CompoundTag tag, HolderLookup.Provider registries) {
        ShopEdits edits = new ShopEdits();
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        ListTag categories = tag.getList(CUSTOM, Tag.TAG_COMPOUND);
        for (int i = 0; i < categories.size(); i++) {
            CompoundTag entry = categories.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("id"));
            if (id == null) {
                continue;
            }
            ShopCategory.CODEC.parse(ops, entry.get("data"))
                    .resultOrPartial(error -> UMA.LOGGER.error("Skipping saved shop zone {}: {}", id, error))
                    .ifPresent(category -> edits.custom.put(id, category));
        }
        ListTag hiddenList = tag.getList(HIDDEN, Tag.TAG_STRING);
        for (int i = 0; i < hiddenList.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(hiddenList.getString(i));
            if (id != null) {
                edits.hidden.add(id);
            }
        }
        return edits;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        ListTag categories = new ListTag();
        custom.forEach((id, category) -> ShopCategory.CODEC.encodeStart(ops, category)
                .resultOrPartial(error -> UMA.LOGGER.error("Could not save shop zone {}: {}", id, error))
                .ifPresent(data -> {
                    CompoundTag entry = new CompoundTag();
                    entry.putString("id", id.toString());
                    entry.put("data", data);
                    categories.add(entry);
                }));
        tag.put(CUSTOM, categories);
        ListTag hiddenList = new ListTag();
        hidden.forEach(id -> hiddenList.add(StringTag.valueOf(id.toString())));
        tag.put(HIDDEN, hiddenList);
        return tag;
    }

    public Map<ResourceLocation, ShopCategory> custom() {
        return custom;
    }

    public Set<ResourceLocation> hidden() {
        return hidden;
    }

    public void put(ResourceLocation id, ShopCategory category) {
        custom.put(id, category);
        hidden.remove(id);
        setDirty();
    }

    public void clear() {
        custom.clear();
        hidden.clear();
        setDirty();
    }

    public void remove(ResourceLocation id, boolean fromPack) {
        custom.remove(id);
        if (fromPack) {
            hidden.add(id);
        }
        setDirty();
    }
}
