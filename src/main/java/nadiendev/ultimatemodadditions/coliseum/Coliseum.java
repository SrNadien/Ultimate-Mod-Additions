package nadiendev.ultimatemodadditions.coliseum;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class Coliseum {

    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, UMA.id("coliseum"));

    private Coliseum() {
    }

    public static boolean isColiseum(Level level) {
        return level != null && DIMENSION.equals(level.dimension());
    }
}
