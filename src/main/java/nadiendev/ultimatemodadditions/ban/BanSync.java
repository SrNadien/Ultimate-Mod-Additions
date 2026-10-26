package nadiendev.ultimatemodadditions.ban;

import nadiendev.ultimatemodadditions.network.payload.BanSyncPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class BanSync {

    private BanSync() {
    }

    public static void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, payload(player.server));
    }

    public static void sendAll(MinecraftServer server) {
        BanSyncPayload payload = payload(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    private static BanSyncPayload payload(MinecraftServer server) {
        List<BanSyncPayload.Entry> entries = new ArrayList<>();
        if (BanService.enabled()) {
            for (BanRule rule : BanService.rules(server)) {
                Set<Item> items = rule.target().exactItems();
                if (items == null || !rule.everywhere() || !rule.data().gamemodes().isEmpty()) {
                    continue;
                }
                String message = rule.data().messages().isEmpty() ? "" : rule.data().messages().get(0);
                for (Item item : items) {
                    entries.add(new BanSyncPayload.Entry(BuiltInRegistries.ITEM.getKey(item), BanAction.mask(rule.actions()),
                            rule.disabled(), message));
                }
            }
        }
        return new BanSyncPayload(entries);
    }
}
