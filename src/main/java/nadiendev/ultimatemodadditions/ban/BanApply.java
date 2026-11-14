package nadiendev.ultimatemodadditions.ban;

import nadiendev.ultimatemodadditions.config.JsonValues;
import net.minecraft.server.MinecraftServer;

public final class BanApply {

    private BanApply() {
    }

    public static void apply(MinecraftServer server) {
        BanService.invalidate();
        BanService.markApplied(server);
        BanSync.sendAll(server);
        BanRecipes.request();
        if (JsonValues.bool(BanService.config(), "refresh_clients", false)) {
            server.getPlayerList().reloadResources();
        }
    }
}
