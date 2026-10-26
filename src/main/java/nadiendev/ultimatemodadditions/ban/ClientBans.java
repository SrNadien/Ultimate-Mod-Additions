package nadiendev.ultimatemodadditions.ban;

import nadiendev.ultimatemodadditions.network.payload.BanSyncPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public final class ClientBans {

    private static volatile Map<Item, BanSyncPayload.Entry> entries = Map.of();
    private static volatile boolean received;

    private ClientBans() {
    }

    public static void accept(BanSyncPayload payload) {
        Map<Item, BanSyncPayload.Entry> map = new HashMap<>();
        for (BanSyncPayload.Entry entry : payload.entries()) {
            BuiltInRegistries.ITEM.getOptional(entry.item()).ifPresent(item -> map.put(item, entry));
        }
        entries = map;
        received = true;
    }

    public static boolean hasData() {
        return received;
    }

    public static BanSyncPayload.Entry get(Item item) {
        return entries.get(item);
    }

    public static Map<Item, BanSyncPayload.Entry> all() {
        return entries;
    }
}
