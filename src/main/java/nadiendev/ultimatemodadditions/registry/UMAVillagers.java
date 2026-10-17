package nadiendev.ultimatemodadditions.registry;

import com.google.common.collect.ImmutableSet;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UMAVillagers {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, UMA.MODID);

    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, UMA.MODID);

    public static final DeferredHolder<PoiType, PoiType> FLASHERO_POI = POI_TYPES.register("flashero",
            () -> new PoiType(ImmutableSet.copyOf(UMABlocks.EXCHANGE_STAND.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> FLASHERO = PROFESSIONS.register("flashero",
            () -> new VillagerProfession("flashero",
                    holder -> holder.is(FLASHERO_POI.getKey()),
                    holder -> UMAConfig.COMMON.flasheroEnabled.get() && holder.is(FLASHERO_POI.getKey()),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN));

    private UMAVillagers() {
    }

    public static void register(IEventBus modBus) {
        POI_TYPES.register(modBus);
        PROFESSIONS.register(modBus);
    }
}
