package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.registry.UMASounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ColiseumPortal {

    private static final Map<GlobalPos, Set<BlockPos>> PROGRESS = new HashMap<>();
    private static final int FAILED_COOLDOWN = 40;

    private ColiseumPortal() {
    }

    public static void clearProgress() {
        PROGRESS.clear();
    }

    public static InteractionResult activate(Level level, BlockPos pos, Player player, ItemStack key) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Optional<ColiseumPortalShape> found = ColiseumPortalShape.at(level, pos);
        if (found.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.portal_invalid")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        ColiseumPortalShape shape = found.get();
        if (shape.lit(level)) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.portal_already_lit")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }

        GlobalPos id = GlobalPos.of(level.dimension(), shape.bottomLeft());
        Set<BlockPos> marked = PROGRESS.computeIfAbsent(id, ignored -> new HashSet<>());
        marked.removeIf(corner -> !shape.isCorner(corner));
        marked.add(pos.immutable());

        if (marked.size() < shape.corners().size()) {
            level.playSound(null, pos, UMASounds.COLISEUM_PORTAL.get(), SoundSource.BLOCKS, 0.5F, 1.4F);
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.portal_corner",
                    marked.size(), shape.corners().size()).withStyle(ChatFormatting.GOLD), true);
            return InteractionResult.CONSUME;
        }

        PROGRESS.remove(id);
        shape.light(level);
        key.shrink(1);
        level.playSound(null, pos, UMASounds.COLISEUM_PORTAL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.ultimatemodadditions.portal_lit")
                .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return InteractionResult.CONSUME;
    }

    public static void travel(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        if (Coliseum.isColiseum(player.level())) {
            player.setPortalCooldown();
            ColiseumTravel.leave(player, server);
            return;
        }

        List<Component> missing = unmet(player);
        if (!missing.isEmpty()) {
            player.setPortalCooldown(FAILED_COOLDOWN);
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.portal_locked")
                    .withStyle(ChatFormatting.RED), true);
            for (Component line : missing) {
                player.sendSystemMessage(line);
            }
            return;
        }

        player.setPortalCooldown();
        ColiseumTravel.enter(player, server);
    }

    public static List<Component> unmet(ServerPlayer player) {
        List<Component> missing = new ArrayList<>();
        if (!UMAConfig.COMMON.portalRequirements.get()) {
            return missing;
        }

        int levels = UMAConfig.COMMON.portalRequiredLevels.get();
        if (player.experienceLevel < levels) {
            missing.add(Component.translatable("message.ultimatemodadditions.portal_need_levels",
                    player.experienceLevel, levels).withStyle(ChatFormatting.RED));
        }
        return missing;
    }

    public static void buildReturnPortal(ServerLevel level, BlockPos bottomLeft) {
        ColiseumPortalShape shape = new ColiseumPortalShape(net.minecraft.core.Direction.Axis.X, bottomLeft, 4, 5);
        for (int along = 0; along < 4; along++) {
            for (int up = 0; up < 5; up++) {
                boolean border = along == 0 || along == 3 || up == 0 || up == 4;
                BlockPos pos = bottomLeft.offset(along, up, 0);
                level.setBlock(pos, border
                        ? net.minecraft.world.level.block.Blocks.CRYING_OBSIDIAN.defaultBlockState()
                        : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
        }
        shape.light(level);
    }
}
