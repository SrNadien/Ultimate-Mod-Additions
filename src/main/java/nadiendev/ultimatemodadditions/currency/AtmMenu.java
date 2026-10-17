package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.registry.UMABlocks;
import nadiendev.ultimatemodadditions.registry.UMAMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AtmMenu extends AbstractContainerMenu {

    public static final int CARD_X = 8;
    public static final int CARD_Y = 20;
    public static final int WITHDRAW_TOP = 65;
    public static final int WITHDRAW_HEIGHT = 60;

    private final SimpleContainer cardContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            AtmMenu.this.slotsChanged(this);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.getItem() instanceof DebitCardItem;
        }
    };

    private final ContainerLevelAccess access;
    private final Player player;

    public AtmMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(UMAMenus.ATM.get(), containerId);
        this.access = access;
        this.player = inventory.player;
        addSlot(new Slot(cardContainer, 0, CARD_X, CARD_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof DebitCardItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        int top = inventoryTop();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, top + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, top + 58));
        }
    }

    public static int inventoryTop() {
        return WITHDRAW_TOP + WITHDRAW_HEIGHT + 16;
    }

    public ItemStack card() {
        return cardContainer.getItem(0);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container == cardContainer && player instanceof ServerPlayer serverPlayer) {
            CardOwner owner = DebitCardItem.owner(card());
            boolean usable = owner != null && DebitCardItem.usableBy(card(), player);
            BankService.sendAtm(serverPlayer, usable ? owner.id() : null);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof DebitCardItem) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, cardContainer));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, UMABlocks.ATM.get());
    }
}
