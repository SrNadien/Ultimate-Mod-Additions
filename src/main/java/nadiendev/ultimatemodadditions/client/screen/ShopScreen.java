package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Wealth;
import nadiendev.ultimatemodadditions.data.ShopCategory;
import nadiendev.ultimatemodadditions.data.ShopItem;
import nadiendev.ultimatemodadditions.network.payload.ShopActionPayload;
import nadiendev.ultimatemodadditions.network.payload.ShopSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ShopScreen extends Screen {

    private static final int WIDTH = 380;
    private static final int HEIGHT = 252;
    private static final int CATEGORY_LEFT = 8;
    private static final int CATEGORY_TOP = 30;
    private static final int CATEGORY_WIDTH = 98;
    private static final int CATEGORY_ROW = 20;
    private static final int CATEGORY_VISIBLE = 8;
    private static final int GRID_LEFT = 112;
    private static final int GRID_TOP = 30;
    private static final int CARD_WIDTH = 61;
    private static final int CARD_HEIGHT = 58;
    private static final int CARD_STEP_X = 64;
    private static final int CARD_STEP_Y = 60;
    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static final int PER_PAGE = COLUMNS * ROWS;
    private static final int BOTTOM = 228;

    private static final int CARD = 0xFF101018;
    private static final int CARD_HOVER = 0xFF1B1B28;
    private static final int CATEGORY_SELECTED = 0xFF3B3323;
    private static final int CATEGORY_IDLE = 0xFF1A1A22;
    private static final int GREEN = 0xFF5CE08A;
    private static final int YELLOW = 0xFFF2D064;
    private static final int GOLD_TEXT = 0xFFFFD36A;

    private enum Sort {
        DEFAULT("shop_sort_default"),
        NAME("shop_sort_name"),
        BUY("shop_sort_buy"),
        SELL("shop_sort_sell");

        private final String key;

        Sort(String key) {
            this.key = key;
        }
    }

    private record Slot(ResourceLocation category, int index, ShopItem item) {
    }

    private int left;
    private int top;
    private int selected;
    private int categoryScroll;
    private int page;
    private Sort sort = Sort.DEFAULT;
    private EditBox search;
    private EditBox quantity;
    private Button sortButton;
    private final List<Button> cardButtons = new ArrayList<>();
    private final List<AbstractWidget> adminWidgets = new ArrayList<>();
    private boolean adminMode;
    private EditBox buyBox;
    private EditBox sellBox;
    private List<Slot> visible = new ArrayList<>();

    public ShopScreen() {
        super(Component.translatable("screen.ultimatemodadditions.shop"));
    }

    private List<ShopSyncPayload.Entry> categories() {
        return ClientHooks.shop().categories();
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        String previousSearch = search == null ? "" : search.getValue();
        String previousQuantity = quantity == null ? "1" : quantity.getValue();

        search = new EditBox(font, left + 8, top + 8, 100, 16, Component.translatable("screen.ultimatemodadditions.shop_search"));
        search.setHint(Component.translatable("screen.ultimatemodadditions.shop_search"));
        search.setMaxLength(32);
        search.setValue(previousSearch);
        search.setResponder(text -> {
            page = 0;
            rebuild();
        });
        addRenderableWidget(search);

        quantity = new EditBox(font, left + 8, top + BOTTOM, 98, 16, Component.translatable("screen.ultimatemodadditions.shop_quantity"));
        quantity.setMaxLength(5);
        quantity.setFilter(text -> text.isEmpty() || text.matches("\\d{1,5}"));
        quantity.setValue(previousQuantity);
        addRenderableWidget(quantity);

        sortButton = addRenderableWidget(Button.builder(sortLabel(), button -> {
            sort = Sort.values()[(sort.ordinal() + 1) % Sort.values().length];
            button.setMessage(sortLabel());
            page = 0;
            rebuild();
        }).bounds(left + 280, top + 8, 92, 16).build());

        if (ClientHooks.shop().canAdd() || ClientHooks.shop().canRemove()) {
            addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.shop_admin"), button -> {
                adminMode = !adminMode;
                rebuild();
            }).bounds(left + 216, top + BOTTOM, 42, 16).build());
        }

        buyBox = new EditBox(font, left + GRID_LEFT, top + 210, 60, 14, Component.translatable("screen.ultimatemodadditions.shop_buy_price"));
        buyBox.setHint(Component.translatable("screen.ultimatemodadditions.shop_buy_price"));
        buyBox.setMaxLength(12);
        buyBox.setFilter(text -> text.matches("[0-9.,]*"));
        sellBox = new EditBox(font, left + GRID_LEFT + 64, top + 210, 60, 14, Component.translatable("screen.ultimatemodadditions.shop_sell_price"));
        sellBox.setHint(Component.translatable("screen.ultimatemodadditions.shop_sell_price"));
        sellBox.setMaxLength(12);
        sellBox.setFilter(text -> text.matches("[0-9.,]*"));

        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.shop_prev"), button -> {
            page = Math.max(0, page - 1);
            rebuild();
        }).bounds(left + 262, top + BOTTOM, 40, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.shop_next"), button -> {
            page = Math.min(pageCount() - 1, page + 1);
            rebuild();
        }).bounds(left + 330, top + BOTTOM, 42, 16).build());

        rebuild();
    }

    private Component sortLabel() {
        return Component.translatable("screen.ultimatemodadditions.shop_sort",
                Component.translatable("screen.ultimatemodadditions." + sort.key));
    }

    public void refresh(boolean reloaded) {
        if (reloaded) {
            int count = categories().size();
            selected = Math.max(0, Math.min(selected, count - 1));
            categoryScroll = Math.max(0, Math.min(categoryScroll, Math.max(0, count - CATEGORY_VISIBLE)));
            rebuild();
        }
    }

    private int pageCount() {
        return Math.max(1, (visible.size() + PER_PAGE - 1) / PER_PAGE);
    }

    private int amount() {
        try {
            return Math.max(1, Integer.parseInt(quantity.getValue()));
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    private void rebuild() {
        for (Button button : cardButtons) {
            removeWidget(button);
        }
        cardButtons.clear();
        for (AbstractWidget widget : adminWidgets) {
            removeWidget(widget);
        }
        adminWidgets.clear();
        visible = collect();
        page = Math.min(page, pageCount() - 1);

        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            if (index >= visible.size()) {
                break;
            }
            Slot slot = visible.get(index);
            int x = left + GRID_LEFT + (i % COLUMNS) * CARD_STEP_X;
            int y = top + GRID_TOP + (i / COLUMNS) * CARD_STEP_Y;
            Button buy = Button.builder(Component.translatable("screen.ultimatemodadditions.shop_buy_short"), button ->
                            send(ShopActionPayload.Action.BUY, slot))
                    .bounds(x + 2, y + 43, 28, 13).build();
            buy.active = slot.item().buyable();
            Button sell = Button.builder(Component.translatable("screen.ultimatemodadditions.shop_sell_short"), button ->
                            send(ShopActionPayload.Action.SELL, slot))
                    .bounds(x + 31, y + 43, 28, 13).build();
            sell.active = slot.item().sellable();
            cardButtons.add(addRenderableWidget(buy));
            cardButtons.add(addRenderableWidget(sell));
            if (adminMode && ClientHooks.shop().canRemove()) {
                adminWidgets.add(addRenderableWidget(Button.builder(Component.literal("x"), button ->
                                PacketDistributor.sendToServer(ShopActionPayload.edit(ShopActionPayload.Action.REMOVE_ITEM,
                                        slot.category(), slot.index(), "", 0.0D, 0.0D)))
                        .bounds(x + CARD_WIDTH - 11, y + 2, 9, 9).build()));
            }
            if (adminMode && ClientHooks.shop().canAdd()) {
                adminWidgets.add(addRenderableWidget(Button.builder(Component.literal("$"), button ->
                                PacketDistributor.sendToServer(ShopActionPayload.edit(ShopActionPayload.Action.EDIT_PRICE,
                                        slot.category(), slot.index(), "", keep(buyBox), keep(sellBox))))
                        .bounds(x + 2, y + 2, 9, 9).build()));
            }
        }
        if (adminMode) {
            buildAdminPanel();
        }
    }

    private ResourceLocation selectedCategory() {
        return selected < categories().size() ? categories().get(selected).id() : null;
    }

    private static double keep(EditBox box) {
        return box.getValue().isBlank() ? -1.0D : number(box);
    }

    private static double number(EditBox box) {
        try {
            return Math.max(0.0D, Double.parseDouble(box.getValue().replace(',', '.')));
        } catch (NumberFormatException exception) {
            return 0.0D;
        }
    }

    private void buildAdminPanel() {
        if (ClientHooks.shop().canAdd()) {
            adminWidgets.add(addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.shop_zone_add"), button -> {
                PacketDistributor.sendToServer(ShopActionPayload.edit(ShopActionPayload.Action.ADD_CATEGORY, null, 0,
                        search.getValue().trim(), 0.0D, 0.0D));
                search.setValue("");
            }).bounds(left + 8, top + 206, 48, 16).build()));
            adminWidgets.add(addRenderableWidget(buyBox));
            adminWidgets.add(addRenderableWidget(sellBox));
            adminWidgets.add(addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.shop_add_hand"), button -> {
                ResourceLocation category = selectedCategory();
                if (category != null) {
                    PacketDistributor.sendToServer(ShopActionPayload.edit(ShopActionPayload.Action.ADD_ITEM, category, 0,
                            "", number(buyBox), number(sellBox)));
                }
            }).bounds(left + GRID_LEFT + 128, top + 209, 90, 16).build()));
        }
        if (ClientHooks.shop().canRemove()) {
            adminWidgets.add(addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.shop_zone_remove"), button -> {
                ResourceLocation category = selectedCategory();
                if (category != null) {
                    PacketDistributor.sendToServer(ShopActionPayload.edit(ShopActionPayload.Action.REMOVE_CATEGORY,
                            category, 0, "", 0.0D, 0.0D));
                }
            }).bounds(left + 58, top + 206, 48, 16).build()));
        }
    }

    private void send(ShopActionPayload.Action action, Slot slot) {
        PacketDistributor.sendToServer(ShopActionPayload.trade(action, slot.category(), slot.index(), amount()));
    }

    private List<Slot> collect() {
        List<Slot> slots = new ArrayList<>();
        String query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
        List<ShopSyncPayload.Entry> categories = categories();
        for (int c = 0; c < categories.size(); c++) {
            if (query.isEmpty() && c != selected) {
                continue;
            }
            ShopSyncPayload.Entry entry = categories.get(c);
            List<ShopItem> items = entry.category().items();
            for (int i = 0; i < items.size(); i++) {
                ShopItem item = items.get(i);
                if (query.isEmpty() || item.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
                    slots.add(new Slot(entry.id(), i, item));
                }
            }
        }
        switch (sort) {
            case NAME -> slots.sort(Comparator.comparing(slot -> slot.item().stack().getHoverName().getString()));
            case BUY -> slots.sort(Comparator.comparingDouble(slot -> slot.item().buyable() ? slot.item().buy() : Double.MAX_VALUE));
            case SELL -> slots.sort(Comparator.comparingDouble((Slot slot) -> slot.item().sell()).reversed());
            default -> {
            }
        }
        return slots;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - left;
        int y = (int) mouseY - top;
        if (button == 0 && x >= CATEGORY_LEFT && x < CATEGORY_LEFT + CATEGORY_WIDTH && y >= CATEGORY_TOP) {
            int row = (y - CATEGORY_TOP) / CATEGORY_ROW;
            int index = categoryScroll + row;
            if (row < CATEGORY_VISIBLE && index < categories().size()) {
                selected = index;
                page = 0;
                search.setValue("");
                rebuild();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = (int) mouseX - left;
        if (x < GRID_LEFT - 4) {
            int max = Math.max(0, categories().size() - CATEGORY_VISIBLE);
            categoryScroll = Math.max(0, Math.min(max, categoryScroll - (int) Math.signum(scrollY)));
        } else {
            page = Math.max(0, Math.min(pageCount() - 1, page - (int) Math.signum(scrollY)));
            rebuild();
        }
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        MoneyScreens.panel(graphics, left, top, WIDTH, HEIGHT);

        graphics.fill(left + 114, top + 8, left + 274, top + 24, CARD);
        graphics.renderOutline(left + 114, top + 8, 160, 16, MoneyScreens.PANEL_EDGE);
        MoneyScreens.fitted(graphics, font, Component.translatable("screen.ultimatemodadditions.shop_balance",
                Money.format(Wealth.parse(ClientHooks.shop().balance()))), left + 118, top + 12, 152, GOLD_TEXT);

        renderCategories(graphics, mouseX, mouseY);
        renderCards(graphics, mouseX, mouseY);

        ShopSyncPayload.Entry current = selected < categories().size() ? categories().get(selected) : null;
        Component categoryName = current == null ? Component.empty() : Component.translatable(current.category().name());
        if (!adminMode) {
            MoneyScreens.fitted(graphics, font, Component.translatable("screen.ultimatemodadditions.shop_commission",
                            trimmed(ClientHooks.shop().commission()), categoryName),
                    left + GRID_LEFT, top + 214, 260, 0xFFB0B0B0);
        }

        graphics.fill(left + 304, top + BOTTOM, left + 328, top + BOTTOM + 16, CARD);
        graphics.renderOutline(left + 304, top + BOTTOM, 24, 16, MoneyScreens.PANEL_EDGE);
        String pageText = String.valueOf(page + 1);
        graphics.drawString(font, pageText, left + 316 - font.width(pageText) / 2, top + BOTTOM + 4, 0xFFFFFFFF, true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltips(graphics, mouseX, mouseY);
    }

    private static String trimmed(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private void renderCategories(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ShopSyncPayload.Entry> categories = categories();
        for (int row = 0; row < CATEGORY_VISIBLE; row++) {
            int index = categoryScroll + row;
            if (index >= categories.size()) {
                break;
            }
            ShopCategory category = categories.get(index).category();
            int x = left + CATEGORY_LEFT;
            int y = top + CATEGORY_TOP + row * CATEGORY_ROW;
            boolean active = index == selected && search.getValue().isBlank();
            boolean hover = mouseX >= x && mouseX < x + CATEGORY_WIDTH && mouseY >= y && mouseY < y + 18;
            graphics.fill(x, y, x + CATEGORY_WIDTH, y + 18, active ? CATEGORY_SELECTED : hover ? CARD_HOVER : CATEGORY_IDLE);
            graphics.renderOutline(x, y, CATEGORY_WIDTH, 18, active ? MoneyScreens.GOLD : MoneyScreens.PANEL_INNER_EDGE);
            graphics.renderItem(new ItemStack(category.icon()), x + 2, y + 1);
            MoneyScreens.fitted(graphics, font, Component.translatable(category.name()), x + 22, y + 5,
                    CATEGORY_WIDTH - 26, active ? GOLD_TEXT : 0xFFE8E8E8);
        }
        if (categories.size() > CATEGORY_VISIBLE) {
            boolean more = categoryScroll + CATEGORY_VISIBLE < categories.size();
            graphics.drawString(font, more ? "▼" : "▲", left + CATEGORY_LEFT + CATEGORY_WIDTH / 2 - 3,
                    top + CATEGORY_TOP + CATEGORY_VISIBLE * CATEGORY_ROW + 2, MoneyScreens.GOLD, true);
        }
    }

    private void renderCards(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            int x = left + GRID_LEFT + (i % COLUMNS) * CARD_STEP_X;
            int y = top + GRID_TOP + (i / COLUMNS) * CARD_STEP_Y;
            boolean filled = index < visible.size();
            boolean hover = filled && mouseX >= x && mouseX < x + CARD_WIDTH && mouseY >= y && mouseY < y + CARD_HEIGHT;
            graphics.fill(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, hover ? CARD_HOVER : CARD);
            graphics.fill(x, y, x + CARD_WIDTH, y + 1, filled ? MoneyScreens.GOLD : MoneyScreens.PANEL_INNER_EDGE);
            if (!filled) {
                continue;
            }
            ShopItem item = visible.get(index).item();
            graphics.renderItem(item.stack(), x + CARD_WIDTH / 2 - 8, y + 5);
            Component name = item.stack().getHoverName();
            int nameWidth = Math.min(CARD_WIDTH - 4, font.width(name));
            MoneyScreens.fitted(graphics, font, name, x + (CARD_WIDTH - nameWidth) / 2, y + 23, CARD_WIDTH - 4, 0xFFFFFFFF);
            Component prices = Component.translatable("screen.ultimatemodadditions.shop_price_buy",
                            item.buyable() ? compact(item.buy()) : "-").withStyle(style -> style.withColor(GREEN))
                    .append(Component.literal(" "))
                    .append(Component.translatable("screen.ultimatemodadditions.shop_price_sell",
                            item.sellable() ? compact(item.sell()) : "-").withStyle(style -> style.withColor(YELLOW)));
            int priceWidth = Math.min(CARD_WIDTH - 4, font.width(prices));
            MoneyScreens.fitted(graphics, font, prices, x + (CARD_WIDTH - priceWidth) / 2, y + 33, CARD_WIDTH - 4, 0xFFFFFFFF);
        }
    }

    private static String compact(double value) {
        if (value >= 1_000_000_000_000.0D) {
            return trimmed(value / 1_000_000_000_000.0D) + "B";
        }
        if (value >= 1_000_000.0D) {
            return trimmed(value / 1_000_000.0D) + "M";
        }
        if (value >= 10_000.0D) {
            return trimmed(value / 1000.0D) + "k";
        }
        return trimmed(value);
    }

    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            if (index >= visible.size()) {
                break;
            }
            int x = left + GRID_LEFT + (i % COLUMNS) * CARD_STEP_X;
            int y = top + GRID_TOP + (i / COLUMNS) * CARD_STEP_Y;
            if (mouseX >= x && mouseX < x + CARD_WIDTH && mouseY >= y && mouseY < y + 42) {
                ShopItem item = visible.get(index).item();
                List<Component> lines = new ArrayList<>(Screen.getTooltipFromItem(minecraft, item.stack()));
                if (item.buyable()) {
                    lines.add(Component.translatable("screen.ultimatemodadditions.shop_tooltip_buy",
                            Money.format(cents(item.buy()))).withStyle(ChatFormatting.GREEN));
                }
                if (item.sellable()) {
                    lines.add(Component.translatable("screen.ultimatemodadditions.shop_tooltip_sell",
                            Money.format(cents(item.sell()))).withStyle(ChatFormatting.YELLOW));
                }
                graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
                return;
            }
        }
    }

    private static BigInteger cents(double mangos) {
        return BigDecimal.valueOf(mangos).multiply(BigDecimal.valueOf(100L)).setScale(0, RoundingMode.DOWN).toBigInteger();
    }
}
