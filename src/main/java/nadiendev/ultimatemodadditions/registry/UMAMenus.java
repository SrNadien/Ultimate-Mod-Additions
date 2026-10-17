package nadiendev.ultimatemodadditions.registry;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.currency.AtmMenu;
import nadiendev.ultimatemodadditions.currency.ExchangeMenu;
import nadiendev.ultimatemodadditions.currency.WalletMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerLevelAccess;
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


    public static final DeferredHolder<MenuType<?>, MenuType<WalletMenu>> WALLET =
            MENUS.register("wallet", () -> IMenuTypeExtension.create(
                    (containerId, inventory, buffer) -> new WalletMenu(containerId, inventory,
                            buffer.readEnum(InteractionHand.class))));

    public static final DeferredHolder<MenuType<?>, MenuType<AtmMenu>> ATM =
            MENUS.register("atm", () -> IMenuTypeExtension.create(
                    (containerId, inventory, buffer) -> new AtmMenu(containerId, inventory,
                            ContainerLevelAccess.NULL)));

    private UMAMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
