package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.registry.UMAAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class ColiseumTravel {

    private ColiseumTravel() {
    }

    public static boolean travel(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || !UMAConfig.COMMON.coliseumEnabled.get()) {
            return false;
        }
        if (Coliseum.isColiseum(player.level())) {
            return leave(player, server);
        }
        return enter(player, server);
    }

    public static boolean enter(ServerPlayer player, MinecraftServer server) {
        ServerLevel target = server.getLevel(Coliseum.DIMENSION);
        if (target == null) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.no_coliseum")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        ColiseumStartup.ensureArena(server, false);
        player.setData(UMAAttachments.RETURN_POS.get(),
                GlobalPos.of(player.level().dimension(), player.blockPosition()));
        sendToArena(player, target);
        return true;
    }

    public static void sendToArena(ServerPlayer player, ServerLevel target) {
        BlockPos spawn = ColiseumArena.spawnPos();
        player.teleportTo(target, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D, 0.0F, 0.0F);
    }

    public static boolean leave(ServerPlayer player, MinecraftServer server) {
        GlobalPos stored = player.getData(UMAAttachments.RETURN_POS.get());
        ServerLevel target = server.getLevel(stored.dimension());
        BlockPos pos = stored.pos();
        if (target == null || pos.equals(BlockPos.ZERO)) {
            target = server.getLevel(Level.OVERWORLD);
            pos = target == null ? BlockPos.ZERO : target.getSharedSpawnPos();
        }
        if (target == null) {
            return false;
        }
        player.teleportTo(target, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
                player.getYRot(), player.getXRot());
        return true;
    }
}
