package nadiendev.ultimatemodadditions;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import nadiendev.ultimatemodadditions.compat.CompatModules;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.registry.UMARegistries;
import org.slf4j.Logger;

@Mod(UMA.MODID)
public final class UMA {

    public static final String MODID = "ultimatemodadditions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UMA(IEventBus modBus, ModContainer container) {
        UMAConfig.register(container);
        UMARegistries.register(modBus);
        CompatModules.init(modBus);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation parse(String value) {
        return value.indexOf(':') < 0 ? id(value) : ResourceLocation.parse(value);
    }
}
