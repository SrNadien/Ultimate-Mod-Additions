package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.WalletMenu;
import nadiendev.ultimatemodadditions.network.ClientBank;
import nadiendev.ultimatemodadditions.network.payload.MoneyActionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.math.BigInteger;

public class WalletScreen extends AbstractContainerScreen<WalletMenu> {

    private static final ResourceLocation OPEN_SHEET = UMA.id("textures/gui/wallet_open.png");
    private static final int FRAME_COUNT = 14;
    private static final int FRAME_COLUMNS = 2;
    private static final int FRAME_WIDTH = 320;
    private static final int FRAME_HEIGHT = 240;
    private static final int SHEET_WIDTH = FRAME_WIDTH * FRAME_COLUMNS;
    private static final int SHEET_HEIGHT = FRAME_HEIGHT * ((FRAME_COUNT + FRAME_COLUMNS - 1) / FRAME_COLUMNS);
    private static final long FRAME_MILLIS = 55L;
    private static final int ANIMATION_WIDTH = 160;
    private static final int ANIMATION_HEIGHT = 120;
    private static final int ANIMATION_TOP = 24;
    private static final int OFFSET = ANIMATION_HEIGHT + 4;
    private static final int WITHDRAW_TOP = 62 + OFFSET;

    private final WithdrawPanel withdraw = new WithdrawPanel(MoneyActionPayload.Target.WALLET,
            UMAConfig.COMMON.walletWithdrawAmounts.get());
    private long openedAt;

    public WalletScreen(WalletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = WITHDRAW_TOP + WithdrawPanel.HEIGHT + 8;
        titleLabelX = 8;
        titleLabelY = 7;
    }

    @Override
    protected void init() {
        super.init();
        openedAt = System.currentTimeMillis();
        addRenderableWidget(MoneyScreens.action(leftPos + 8, topPos + 40 + OFFSET, imageWidth - 16,
                Component.translatable("screen.ultimatemodadditions.deposit_all"),
                MoneyActionPayload.Target.WALLET, MoneyActionPayload.Action.DEPOSIT_ALL, BigInteger.ZERO));
        withdraw.init(this::addRenderableWidget, font, leftPos, topPos, WITHDRAW_TOP);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MoneyScreens.panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        graphics.fill(leftPos + 7, topPos + 20, leftPos + imageWidth - 7, topPos + 21, MoneyScreens.SEPARATOR);

        int frame = (int) Math.min(FRAME_COUNT - 1, (System.currentTimeMillis() - openedAt) / FRAME_MILLIS);
        float u = (frame % FRAME_COLUMNS) * FRAME_WIDTH;
        float v = (frame / FRAME_COLUMNS) * FRAME_HEIGHT;
        graphics.blit(OPEN_SHEET, leftPos + (imageWidth - ANIMATION_WIDTH) / 2, topPos + ANIMATION_TOP,
                ANIMATION_WIDTH, ANIMATION_HEIGHT, u, v, FRAME_WIDTH, FRAME_HEIGHT, SHEET_WIDTH, SHEET_HEIGHT);

        graphics.fill(leftPos + 7, topPos + 36 + OFFSET, leftPos + imageWidth - 7, topPos + 37 + OFFSET,
                MoneyScreens.SEPARATOR);
        graphics.fill(leftPos + 7, topPos + 58 + OFFSET, leftPos + imageWidth - 7, topPos + 59 + OFFSET,
                MoneyScreens.SEPARATOR);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, MoneyScreens.GOLD, true);
        BigInteger balance = ClientBank.wallet();
        if (balance.signum() > 0) {
            graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.inside"),
                    8, 19 + OFFSET, 0xFFB0B0B0, true);
            MoneyScreens.fitted(graphics, font, Money.format(balance), 8, 28 + OFFSET, imageWidth - 16, 0xFF8AE28A);
        } else {
            graphics.drawString(font, Component.translatable("message.ultimatemodadditions.wallet_empty"),
                    8, 24 + OFFSET, 0xFFCC5555, true);
        }
        withdraw.renderLabels(graphics, font, WITHDRAW_TOP);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
