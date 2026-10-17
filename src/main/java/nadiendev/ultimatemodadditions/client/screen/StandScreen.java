package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Wealth;
import nadiendev.ultimatemodadditions.data.ShopItem;
import nadiendev.ultimatemodadditions.network.payload.ShopActionPayload;
import nadiendev.ultimatemodadditions.network.payload.ShopSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StandScreen extends Screen {

    private static final ResourceLocation STAND_TEXTURE = UMA.id("textures/gui/exchange_stand.png");
    private static final ResourceLocation TIENDITA_TEXTURE = UMA.id("textures/gui/shop_tiendita.png");
    private static final int TEXTURE_WIDTH = 552;
    private static final int TEXTURE_HEIGHT = 464;
    private static final int WIDTH = 276;
    private static final int HEIGHT = 232;
    private static final float GRID_X = 65.5F;
    private static final float GRID_Y = 97.1F;
    private static final float CELL_WIDTH = 29.18F;
    private static final float CELL_HEIGHT = 28.43F;
    private static final int COLUMNS = 5;
    private static final int ROWS = 4;
    private static final int PER_PAGE = COLUMNS * ROWS;
    private static final int DARK = 0xFF2B2B2B;
    private static final int GOLD = 0xFFE7C25A;
    private static final int PRICE = 0xFFF2D064;
    private static final int SELL_ALL = 2304;
    private static final int STACK = 64;

    private int left;
    private int top;
    private int selected;
    private int page;
    private Button deliveries;

    private final ResourceLocation texture;

    public StandScreen(boolean tiendita) {
        super(Component.translatable("screen.ultimatemodadditions.shop"));
        texture = tiendita ? TIENDITA_TEXTURE : STAND_TEXTURE;
    }

    private List<ShopSyncPayload.Entry> categories() {
        return ClientHooks.shop().categories();
    }

    private List<ShopItem> items() {
        return selected < categories().size() ? categories().get(selected).category().items() : List.of();
    }

    private int pageCount() {
        return Math.max(1, (items().size() + PER_PAGE - 1) / PER_PAGE);
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> category(-1))
                .bounds(left + 56, top + 27, 14, 13).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> category(1))
                .bounds(left + 206, top + 27, 14, 13).build());
        addRenderableWidget(Button.builder(Component.literal("<"), button -> page(-1))
                .bounds(left + 200, top + 217, 14, 13).build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> page(1))
                .bounds(left + 246, top + 217, 14, 13).build());
        deliveries = addRenderableWidget(Button.builder(deliveriesLabel(), button ->
                        PacketDistributor.sendToServer(ShopActionPayload.claim()))
                .bounds(left + 12, top + 217, 76, 13).build());
    }

    private Component deliveriesLabel() {
        return Component.translatable("screen.ultimatemodadditions.shop_deliveries", ClientHooks.shop().deliveries());
    }

    public void refresh(boolean reloaded) {
        if (deliveries != null) {
            deliveries.setMessage(deliveriesLabel());
        }
        if (reloaded) {
            selected = Math.max(0, Math.min(selected, categories().size() - 1));
            page = Math.min(page, pageCount() - 1);
        }
    }

    private void category(int direction) {
        int count = categories().size();
        if (count > 0) {
            selected = (selected + direction + count) % count;
            page = 0;
        }
    }

    private void page(int direction) {
        page = Math.max(0, Math.min(pageCount() - 1, page + direction));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int slotAt(double mouseX, double mouseY) {
        double x = mouseX - left - GRID_X;
        double y = mouseY - top - GRID_Y;
        if (x < 0 || y < 0 || x >= CELL_WIDTH * COLUMNS || y >= CELL_HEIGHT * ROWS) {
            return -1;
        }
        int index = page * PER_PAGE + (int) (y / CELL_HEIGHT) * COLUMNS + (int) (x / CELL_WIDTH);
        return index < items().size() ? index : -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int index = slotAt(mouseX, mouseY);
        if (index < 0 || selected >= categories().size() || button > 1) {
            return false;
        }
        ResourceLocation category = categories().get(selected).id();
        boolean shift = hasShiftDown();
        if (button == 0) {
            PacketDistributor.sendToServer(ShopActionPayload.trade(ShopActionPayload.Action.BUY, category, index,
                    shift ? STACK : 1));
        } else {
            PacketDistributor.sendToServer(ShopActionPayload.trade(ShopActionPayload.Action.SELL, category, index,
                    shift ? SELL_ALL : 1));
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        page(-(int) Math.signum(scrollY));
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(texture, left, top, WIDTH, HEIGHT, 0.0F, 0.0F, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
        Component name = selected < categories().size()
                ? Component.translatable(categories().get(selected).category().name()) : Component.empty();
        float fit = Math.min(1.0F, 120.0F / Math.max(1, font.width(name)));
        graphics.pose().pushPose();
        graphics.pose().translate(left + 139, top + 33.5F - 4.0F * fit, 0.0F);
        graphics.pose().scale(fit, fit, 1.0F);
        graphics.drawString(font, name, -font.width(name) / 2, 0, DARK, false);
        graphics.pose().popPose();

        List<ShopItem> items = items();
        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            if (index >= items.size()) {
                break;
            }
            float cellX = left + GRID_X + (i % COLUMNS) * CELL_WIDTH;
            float cellY = top + GRID_Y + (i / COLUMNS) * CELL_HEIGHT;
            if (slotAt(mouseX, mouseY) == index) {
                graphics.fill((int) cellX + 1, (int) cellY + 1, (int) (cellX + CELL_WIDTH) - 1,
                        (int) (cellY + CELL_HEIGHT) - 1, 0x40FFFFFF);
            }
            ShopItem item = items.get(index);
            graphics.renderItem(item.stack(), (int) (cellX + CELL_WIDTH / 2 - 8), (int) cellY + 2);
            if (item.buyable()) {
                MoneyScreens.fitted(graphics, font, Component.literal(compact(item.buy())),
                        (int) cellX + 3, (int) cellY + 19, (int) CELL_WIDTH - 6, PRICE);
            }
        }

        box(graphics, left + 92, top + 216, 104, 13);
        Component balance = Component.translatable("screen.ultimatemodadditions.shop_balance",
                Money.format(Wealth.parse(ClientHooks.shop().balance())));
        float balanceFit = Math.min(1.0F, 98.0F / Math.max(1, font.width(balance)));
        graphics.pose().pushPose();
        graphics.pose().translate(left + 144, top + 222.5F - 4.0F * balanceFit, 0.0F);
        graphics.pose().scale(balanceFit, balanceFit, 1.0F);
        graphics.drawString(font, balance, -font.width(balance) / 2, 0, GOLD, true);
        graphics.pose().popPose();
        box(graphics, left + 217, top + 216, 28, 13);
        String pageText = (page + 1) + "/" + pageCount();
        graphics.drawString(font, pageText, left + 231 - font.width(pageText) / 2, top + 219, 0xFFFFFFFF, true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int index = slotAt(mouseX, mouseY);
        if (index < 0) {
            return;
        }
        ShopItem item = items().get(index);
        List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, item.stack()));
        if (item.buyable()) {
            lines.add(Component.translatable("screen.ultimatemodadditions.shop_tooltip_buy",
                    Money.format(cents(item.buy()))).withStyle(ChatFormatting.GREEN));
        }
        if (item.sellable()) {
            lines.add(Component.translatable("screen.ultimatemodadditions.shop_tooltip_sell",
                    Money.format(cents(item.sell()))).withStyle(ChatFormatting.YELLOW));
        }
        lines.add(Component.translatable("screen.ultimatemodadditions.stand_hint").withStyle(ChatFormatting.GRAY));
        graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }

    private static void box(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, 0xFF1E1E1E);
        graphics.renderOutline(x, y, w, h, 0xFF6E6B68);
    }

    private static BigInteger cents(double mangos) {
        return BigDecimal.valueOf(mangos).multiply(BigDecimal.valueOf(100L)).setScale(0, RoundingMode.DOWN)
                .toBigInteger();
    }

    private static String compact(double value) {
        if (value >= 1_000_000.0D) {
            return trim(value / 1_000_000.0D) + "M";
        }
        if (value >= 10_000.0D) {
            return trim(value / 1000.0D) + "k";
        }
        return trim(value);
    }

    private static String trim(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
