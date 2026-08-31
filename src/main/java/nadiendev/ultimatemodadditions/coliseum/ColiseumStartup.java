package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid = UMA.MODID)
public final class ColiseumStartup {

    private ColiseumStartup() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!UMAConfig.COMMON.coliseumEnabled.get()) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Coliseum.DIMENSION);
        if (level == null) {
            UMA.LOGGER.warn("The Coliseum dimension did not load. Its datapack files are missing or overridden.");
            return;
        }
        ensureArena(event.getServer(), false);
        UMA.LOGGER.info("Coliseum dimension ready, difficulty {}",
                ColiseumState.get(event.getServer()).difficulty());
    }

    @SubscribeEvent
    public static void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        ColiseumPortal.clearProgress();
    }

    public static boolean ensureArena(MinecraftServer server, boolean force) {
        ServerLevel level = server.getLevel(Coliseum.DIMENSION);
        if (level == null || !UMAConfig.COMMON.coliseumBuildArena.get()) {
            return false;
        }
        ColiseumState state = ColiseumState.get(server);
        if (state.arenaBuilt() && !force && standing(level)) {
            return false;
        }
        if (state.arenaBuilt() && !force) {
            UMA.LOGGER.info("The Coliseum is flagged as built but the arena is not there. Building it again.");
        }
        ColiseumArena.build(level);
        state.setArenaBuilt(true);
        return true;
    }

    private static boolean standing(ServerLevel level) {
        return level.getBlockState(new net.minecraft.core.BlockPos(0, ColiseumArena.floorY() - 6, 0))
                .is(net.minecraft.world.level.block.Blocks.BEDROCK);
    }
}
