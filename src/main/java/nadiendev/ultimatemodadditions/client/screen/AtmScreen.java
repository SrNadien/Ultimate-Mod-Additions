package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.AtmMenu;
import nadiendev.ultimatemodadditions.currency.DebitCardItem;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.network.ClientBank;
import nadiendev.ultimatemodadditions.network.payload.MoneyActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.math.BigInteger;

public class AtmScreen extends AbstractContainerScreen<AtmMenu> {

    private static final int BUTTON_WIDTH = 76;

    private final WithdrawPanel withdraw = new WithdrawPanel(MoneyActionPayload.Target.ATM,
            UMAConfig.COMMON.atmWithdrawAmounts.get());

    public AtmScreen(AtmMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = AtmMenu.inventoryTop() + 58 + 18 + 8;
        titleLabelX = 8;
        titleLabelY = 7;
        inventoryLabelY = AtmMenu.inventoryTop() - 11;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(MoneyScreens.action(leftPos + 8, topPos + 44, BUTTON_WIDTH,
                Component.translatable("screen.ultimatemodadditions.deposit_all"),
                MoneyActionPayload.Target.ATM, MoneyActionPayload.Action.DEPOSIT_ALL, BigInteger.ZERO));
        addRenderableWidget(MoneyScreens.action(leftPos + 92, topPos + 44, BUTTON_WIDTH,
                Component.translatable("screen.ultimatemodadditions.request_card"),
                MoneyActionPayload.Target.ATM, MoneyActionPayload.Action.REQUEST_CARD, BigInteger.ZERO));
        withdraw.init(this::addRenderableWidget, font, leftPos, topPos, AtmMenu.WITHDRAW_TOP);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MoneyScreens.panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            MoneyScreens.slot(graphics, leftPos + slot.x - 1, topPos + slot.y - 1);
        }
        graphics.fill(leftPos + 7, topPos + 40, leftPos + imageWidth - 7, topPos + 41, MoneyScreens.SEPARATOR);
        graphics.fill(leftPos + 7, topPos + 62, leftPos + imageWidth - 7, topPos + 63, MoneyScreens.SEPARATOR);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, MoneyScreens.GOLD, true);
        boolean usable = minecraft != null && minecraft.player != null
                && DebitCardItem.owner(menu.card()) != null
                && DebitCardItem.usableBy(menu.card(), minecraft.player);
        if (usable) {
            graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.account"),
                    32, 22, 0xFFB0B0B0, true);
            MoneyScreens.fitted(graphics, font, Money.format(ClientBank.atm()), 32, 32,
                    imageWidth - 32 - 8, 0xFF8AE28A);
        } else {
            graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.insert_card"),
                    32, 26, 0xFFCC5555, true);
        }
        withdraw.renderLabels(graphics, font, AtmMenu.WITHDRAW_TOP);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xFFB0B0B0, true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
