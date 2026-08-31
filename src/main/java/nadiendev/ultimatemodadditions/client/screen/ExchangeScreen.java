package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.currency.ExchangeLogic;
import nadiendev.ultimatemodadditions.currency.ExchangeMenu;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.data.TradeOffer;
import nadiendev.ultimatemodadditions.network.SyncedData;
import nadiendev.ultimatemodadditions.network.payload.BuyOfferPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ExchangeScreen extends AbstractContainerScreen<ExchangeMenu> {

    private static final int PANEL = 0xFF141419;
    private static final int PANEL_EDGE = 0xFF6A5B2E;
    private static final int PANEL_INNER_EDGE = 0xFF3B3323;
    private static final int LIST_BACKGROUND = 0xFF101016;
    private static final int SLOT_FILL = 0xFF2A2A33;
    private static final int SLOT_SHADOW = 0xFF101014;
    private static final int SLOT_LIGHT = 0xFF5A5A6A;
    private static final int ROW_ODD = 0xFF191922;
    private static final int ROW_EVEN = 0xFF14141C;
    private static final int ROW_HOVER = 0xFF2E2E3E;
    private static final int SEPARATOR = 0xFF3A3A48;

    private static final int ROW_HEIGHT = 26;
    private static final int LIST_LEFT = 8;
    private static final int LIST_RIGHT = 234;
    private static final int LIST_TOP = 24;
    private static final int VISIBLE_ROWS = 4;
    private static final int LIST_HEIGHT = ROW_HEIGHT * VISIBLE_ROWS;
    private static final int SCROLL_LEFT = 238;
    private static final int SCROLL_WIDTH = 10;

    private final List<Map.Entry<ResourceLocation, TradeOffer>> offers = new ArrayList<>();
    private int scroll;

    public ExchangeScreen(ExchangeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 256;
        imageHeight = 224;
        inventoryLabelY = 129;
        titleLabelX = 8;
        titleLabelY = 7;
    }

    @Override
    protected void init() {
        super.init();
        refresh();
    }

    private void refresh() {
        offers.clear();
        SyncedData.trades().entrySet().stream()
                .filter(entry -> entry.getValue().enabled())
                .filter(entry -> ExchangeLogic.purchasable(entry.getValue()))
                .sorted(Comparator.comparingInt(entry -> entry.getValue().sortOrder()))
                .forEach(offers::add);
        scroll = Math.min(scroll, Math.max(0, offers.size() - VISIBLE_ROWS));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, PANEL_EDGE);
        graphics.renderOutline(leftPos + 2, topPos + 2, imageWidth - 4, imageHeight - 4, PANEL_INNER_EDGE);

        graphics.fill(leftPos + LIST_LEFT - 2, topPos + LIST_TOP - 2,
                leftPos + LIST_RIGHT + 2, topPos + LIST_TOP + LIST_HEIGHT + 2, LIST_BACKGROUND);
        graphics.renderOutline(leftPos + LIST_LEFT - 2, topPos + LIST_TOP - 2,
                LIST_RIGHT - LIST_LEFT + 4, LIST_HEIGHT + 4, SEPARATOR);

        scrollbar(graphics);

        graphics.fill(leftPos + 7, topPos + 19, leftPos + imageWidth - 7, topPos + 20, SEPARATOR);
        graphics.fill(leftPos + 7, topPos + LIST_TOP + LIST_HEIGHT + 6,
                leftPos + imageWidth - 7, topPos + LIST_TOP + LIST_HEIGHT + 7, SEPARATOR);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                slot(graphics, leftPos + 46 + column * 18, topPos + 139 + row * 18);
            }
        }
        graphics.fill(leftPos + 46, topPos + 195, leftPos + 46 + 9 * 18, topPos + 196, SEPARATOR);
        for (int column = 0; column < 9; column++) {
            slot(graphics, leftPos + 46 + column * 18, topPos + 197);
        }
    }

    private void scrollbar(GuiGraphics graphics) {
        int trackTop = topPos + LIST_TOP - 2;
        int trackHeight = LIST_HEIGHT + 4;
        graphics.fill(leftPos + SCROLL_LEFT, trackTop, leftPos + SCROLL_LEFT + SCROLL_WIDTH,
                trackTop + trackHeight, LIST_BACKGROUND);
        graphics.renderOutline(leftPos + SCROLL_LEFT, trackTop, SCROLL_WIDTH, trackHeight, SEPARATOR);
        int overflow = Math.max(0, offers.size() - VISIBLE_ROWS);
        int handleHeight = overflow == 0 ? trackHeight - 2
                : Math.max(12, (trackHeight - 2) * VISIBLE_ROWS / Math.max(1, offers.size()));
        int travel = trackHeight - 2 - handleHeight;
        int handleTop = trackTop + 1 + (overflow == 0 ? 0 : travel * scroll / overflow);
        graphics.fill(leftPos + SCROLL_LEFT + 1, handleTop, leftPos + SCROLL_LEFT + SCROLL_WIDTH - 1,
                handleTop + handleHeight, SLOT_LIGHT);
    }

    private void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT_FILL);
        graphics.fill(x, y, x + 18, y + 1, SLOT_SHADOW);
        graphics.fill(x, y, x + 1, y + 18, SLOT_SHADOW);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, SLOT_LIGHT);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, SLOT_LIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0xFFE7C25A, true);
        Component balance = Component.translatable("screen.ultimatemodadditions.balance",
                Money.format(minecraft != null && minecraft.player != null ? Money.total(minecraft.player) : 0L));
        graphics.drawString(font, balance, imageWidth - 8 - font.width(balance), titleLabelY, 0xFF8AE28A, true);
        graphics.drawString(font, playerInventoryTitle, 46, inventoryLabelY, 0xFFB0B0B0, true);

        if (offers.isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.no_offers"),
                    LIST_LEFT + 4, LIST_TOP + 8, 0xFF8A8A8A, true);
            return;
        }

        int shown = Math.min(VISIBLE_ROWS, offers.size() - scroll);
        for (int i = 0; i < shown; i++) {
            Map.Entry<ResourceLocation, TradeOffer> entry = offers.get(scroll + i);
            renderOffer(graphics, entry.getValue(), scroll + i, LIST_TOP + i * ROW_HEIGHT,
                    mouseX - leftPos, mouseY - topPos);
        }
    }

    private void renderOffer(GuiGraphics graphics, TradeOffer offer, int index, int y, int relX, int relY) {
        boolean hovered = relX >= LIST_LEFT && relX <= LIST_RIGHT && relY >= y && relY < y + ROW_HEIGHT;
        int background = hovered ? ROW_HOVER : (index % 2 == 0 ? ROW_ODD : ROW_EVEN);
        graphics.fill(LIST_LEFT, y, LIST_RIGHT, y + ROW_HEIGHT - 1, background);
        graphics.fill(LIST_LEFT, y + ROW_HEIGHT - 1, LIST_RIGHT, y + ROW_HEIGHT, SEPARATOR);
        graphics.fill(LIST_LEFT, y, LIST_LEFT + 1, y + ROW_HEIGHT - 1, hovered ? 0xFFE7C25A : SEPARATOR);

        slot(graphics, LIST_LEFT + 4, y + 4);
        ItemStack result = offer.result().asStack();
        graphics.renderItem(result, LIST_LEFT + 5, y + 5);
        graphics.renderItemDecorations(font, result, LIST_LEFT + 5, y + 5);

        String label = offer.name().orElseGet(() -> result.getHoverName().getString());
        graphics.drawString(font, label, LIST_LEFT + 26, y + 4, 0xFFE8E8E8, true);

        int costLeft = LIST_RIGHT - 4;
        if (!offer.cost().isEmpty()) {
            for (int i = offer.cost().size() - 1; i >= 0; i--) {
                costLeft -= 20;
                ItemStack stack = offer.cost().get(i).asStack();
                slot(graphics, costLeft, y + 4);
                graphics.renderItem(stack, costLeft + 1, y + 5);
                graphics.renderItemDecorations(font, stack, costLeft + 1, y + 5);
            }
            graphics.fill(costLeft - 5, y + 4, costLeft - 4, y + ROW_HEIGHT - 5, SEPARATOR);
        }

        if (offer.price() > 0) {
            boolean affordable = minecraft != null && minecraft.player != null
                    && Money.total(minecraft.player) >= offer.price();
            Component price = Money.format(offer.price());
            graphics.drawString(font, price, LIST_LEFT + 26, y + 15,
                    affordable ? 0xFFE7C25A : 0xFFCC5555, true);
        } else if (offer.cost().isEmpty()) {
            graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.free"),
                    LIST_LEFT + 26, y + 15, 0xFF8AE28A, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int relX = (int) mouseX - leftPos;
        int relY = (int) mouseY - topPos;
        if (button == 0 && relX >= LIST_LEFT && relX <= LIST_RIGHT
                && relY >= LIST_TOP && relY < LIST_TOP + LIST_HEIGHT) {
            int index = scroll + (relY - LIST_TOP) / ROW_HEIGHT;
            if (index >= 0 && index < offers.size()) {
                int amount = hasShiftDown() ? 8 : 1;
                PacketDistributor.sendToServer(new BuyOfferPayload(offers.get(index).getKey(), amount));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int max = Math.max(0, offers.size() - VISIBLE_ROWS);
        scroll = Math.max(0, Math.min(max, scroll - (int) Math.signum(scrollY)));
        return true;
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    protected void renderMenuBackground(GuiGraphics graphics) {
        renderTransparentBackground(graphics);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
