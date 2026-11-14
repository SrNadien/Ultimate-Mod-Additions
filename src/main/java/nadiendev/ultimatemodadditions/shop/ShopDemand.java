package nadiendev.ultimatemodadditions.shop;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@EventBusSubscriber(modid = UMA.MODID)
public final class ShopDemand extends SavedData {

    private static final String NAME = "ultimatemodadditions_shop_demand";
    private static final long HOUR_MILLIS = 3_600_000L;
    private static final int MAX_PERIODS = 400;
    private static final int MAX_COUNT = 1_000_000;
    private static final int CHECK_TICKS = 1200;

    private final Map<String, Entry> entries = new HashMap<>();
    private long period;
    private long lastDecay;

    private static final class Entry {
        private int count;
        private long lastBuy;
        private boolean bought;
    }

    public static final class Curve {
        private final TreeMap<Integer, Double> steps = new TreeMap<>();

        double percent(int count) {
            Map.Entry<Integer, Double> step = steps.floorEntry(count);
            return step == null ? 0.0D : step.getValue();
        }
    }

    public static ShopDemand get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(ShopDemand::new, ShopDemand::load, null), NAME);
    }

    public static Curve curve() {
        Curve curve = new Curve();
        for (String entry : UMAConfig.COMMON.demandTiers.get()) {
            int split = entry.indexOf(':');
            try {
                int from = Integer.parseInt(entry.substring(0, split).trim());
                double percent = Double.parseDouble(entry.substring(split + 1).trim());
                curve.steps.put(from, percent);
            } catch (RuntimeException exception) {
                UMA.LOGGER.error("Bad shop demand step {}", entry);
            }
        }
        return curve;
    }

    private static String key(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static ShopDemand load(CompoundTag tag, HolderLookup.Provider registries) {
        ShopDemand demand = new ShopDemand();
        demand.period = tag.getLong("period");
        demand.lastDecay = tag.getLong("last");
        ListTag list = tag.getList("items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag saved = list.getCompound(i);
            Entry entry = new Entry();
            entry.count = saved.getInt("count");
            entry.lastBuy = saved.getLong("last_buy");
            entry.bought = saved.getBoolean("bought");
            demand.entries.put(saved.getString("id"), entry);
        }
        return demand;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("period", period);
        tag.putLong("last", lastDecay);
        ListTag list = new ListTag();
        entries.forEach((id, entry) -> {
            CompoundTag saved = new CompoundTag();
            saved.putString("id", id);
            saved.putInt("count", entry.count);
            saved.putLong("last_buy", entry.lastBuy);
            saved.putBoolean("bought", entry.bought);
            list.add(saved);
        });
        tag.put("items", list);
        return tag;
    }

    public double multiplier(Curve curve, Item item) {
        Entry entry = entries.get(key(item));
        int count = entry == null ? 0 : entry.count;
        double rise = 1.0D + curve.percent(count) / 100.0D;
        if (count > 0) {
            return rise;
        }
        long idle = period - (entry == null ? 0L : entry.lastBuy);
        double discount = Math.min(UMAConfig.COMMON.demandIdleDiscountMax.get(),
                idle * UMAConfig.COMMON.demandIdleDiscount.get());
        return rise * Math.max(0.0D, 1.0D - discount / 100.0D);
    }

    public boolean record(Item item, int units) {
        Curve curve = curve();
        double before = multiplier(curve, item);
        Entry entry = entries.computeIfAbsent(key(item), id -> new Entry());
        int added = UMAConfig.COMMON.demandCountUnits.get() ? Math.max(1, units) : 1;
        entry.count = Math.min(MAX_COUNT, entry.count + added);
        entry.lastBuy = period;
        entry.bought = true;
        setDirty();
        return Math.abs(multiplier(curve, item) - before) > 1.0E-9D;
    }

    private boolean decay(long now) {
        if (lastDecay == 0L) {
            lastDecay = now;
            setDirty();
            return false;
        }
        long length = Math.max(1L, UMAConfig.COMMON.demandDecayHours.get()) * HOUR_MILLIS;
        long elapsed = (now - lastDecay) / length;
        if (elapsed <= 0L) {
            return false;
        }
        lastDecay += elapsed * length;
        int percent = UMAConfig.COMMON.demandCountDecay.get();
        for (long step = 0L; step < Math.min(elapsed, MAX_PERIODS); step++) {
            period++;
            for (Entry entry : entries.values()) {
                if (entry.bought) {
                    entry.bought = false;
                } else if (entry.count > 0) {
                    entry.count = Math.max(0, entry.count - Math.max(1, (entry.count * percent + 99) / 100));
                    if (entry.count == 0) {
                        entry.lastBuy = period;
                    }
                }
            }
        }
        setDirty();
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % CHECK_TICKS != 0 || !UMAConfig.COMMON.demandEnabled.get()) {
            return;
        }
        if (get(server).decay(System.currentTimeMillis())) {
            ShopService.refreshAll(server);
        }
    }
}
