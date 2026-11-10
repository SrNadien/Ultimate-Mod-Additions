package nadiendev.ultimatemodadditions.compat.ftb;

import dev.ftb.mods.ftblibrary.integration.stages.StageProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class OpenStageProvider implements StageProvider {

    public static final String OPEN_STAGE = "uma_echoes_open";

    private final StageProvider delegate;

    public OpenStageProvider(StageProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean has(Player player, String stage) {
        return OPEN_STAGE.equals(stage) || delegate.has(player, stage);
    }

    @Override
    public void add(ServerPlayer player, String stage) {
        delegate.add(player, stage);
    }

    @Override
    public void remove(ServerPlayer player, String stage) {
        delegate.remove(player, stage);
    }

    @Override
    public void sync(ServerPlayer player) {
        delegate.sync(player);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
