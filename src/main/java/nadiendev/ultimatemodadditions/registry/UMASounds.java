package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UMASounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, UMA.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> LOOT_BAG_OPEN = simple("loot_bag_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> COIN_GAINED = simple("coin_gained");
    public static final DeferredHolder<SoundEvent, SoundEvent> COLISEUM_PORTAL = simple("coliseum_portal");

    private UMASounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> simple(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(UMA.id(name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
