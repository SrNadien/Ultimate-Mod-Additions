package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.registry.UMAMenus;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class WalletMenu extends AbstractContainerMenu {

    private final Player player;
    private final InteractionHand hand;

    public WalletMenu(int containerId, Inventory inventory, InteractionHand hand) {
        super(UMAMenus.WALLET.get(), containerId);
        this.player = inventory.player;
        this.hand = hand;
    }

    public ItemStack wallet() {
        return player.getItemInHand(hand);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return wallet().getItem() instanceof WalletItem;
    }
}
