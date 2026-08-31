package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.currency.ExchangeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UMAMenus {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, UMA.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ExchangeMenu>> EXCHANGE =
            MENUS.register("exchange", () -> IMenuTypeExtension.create(
                    (containerId, inventory, buffer) -> new ExchangeMenu(containerId, inventory)));


    private UMAMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
