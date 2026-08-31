package nadiendev.ultimatemodadditions.registry;

import com.mojang.serialization.Codec;
import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UMADataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, UMA.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> BAG_ID =
            COMPONENTS.registerComponentType("bag_id", builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> DIFFICULTY =
            COMPONENTS.registerComponentType("difficulty", builder -> builder
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC));

    private UMADataComponents() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
