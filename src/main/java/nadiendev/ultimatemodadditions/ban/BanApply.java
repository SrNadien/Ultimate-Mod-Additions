package nadiendev.ultimatemodadditions.ban;

import net.minecraft.server.MinecraftServer;

public final class BanApply {

    private BanApply() {
    }

    public static void apply(MinecraftServer server) {
        BanService.invalidate();
        BanService.markApplied(server);
        BanRecipes.recompute(server);
        BanSync.sendAll(server);
        server.getPlayerList().reloadResources();
    }
}
