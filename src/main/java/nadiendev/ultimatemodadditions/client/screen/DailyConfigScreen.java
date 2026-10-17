package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.network.payload.DailyConfigActionPayload;
import nadiendev.ultimatemodadditions.network.payload.DailyConfigPayload;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload.EntryView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class DailyConfigScreen extends Screen {

    private static final int WIDTH = 330;
    private static final int HEIGHT = 214;
    private static final int LIST_X = 186;
    private static final int LIST_Y = 50;
    private static final int LIST_WIDTH = 134;
    private static final int ROW = 13;
    private static final int ROWS = 7;

    private static final int PANEL = 0xFFC6C6C6;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SHADE = 0xFF555555;
    private static final int OUTLINE = 0xFF000000;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    private static final int SLOT_DARK = 0xFF373737;
    private static final int ROW_A = 0xFFB4B4B4;
    private static final int ROW_B = 0xFFBFBFBF;
    private static final int TEXT = 0xFF202020;
    private static final int GREEN = 0xFF1E7B34;
    private static final int ORANGE = 0xFFD9741B;

    private int left;
    private int top;
    private int tab;
    private int selected = -1;
    private int scroll;
    private boolean modBonus;
    private boolean notify;
    private boolean initialised;
    private Button bonusButton;
    private Button notifyButton;
    private EditBox chance;
    private EditBox dailyCap;
    private EditBox weeklyCap;
    private EditBox monthlyCap;
    private EditBox hour;
    private EditBox money;

    public DailyConfigScreen() {
        super(Component.translatable("screen.ultimatemodadditions.daily_config"));
    }

    private DailyConfigPayload data() {
        return ClientHooks.dailyConfig();
    }

    private DailyReward.Period period() {
        return DailyReward.Period.values()[tab];
    }

    private List<EntryView> entries() {
        for (DailyConfigPayload.ConfigPeriod entry : data().periods()) {
            if (entry.period() == period()) {
                return entry.entries();
            }
        }
        return List.of();
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        if (!initialised) {
            modBonus = data().modBonus();
            notify = data().loginNotice();
        }
        chance = box(116, 52, initialised ? chance.getValue() : plain(data().chance()));
        dailyCap = box(116, 70, initialised ? dailyCap.getValue() : plain(data().budgetDaily()));
        weeklyCap = box(116, 88, initialised ? weeklyCap.getValue() : plain(data().budgetWeekly()));
        monthlyCap = box(116, 106, initialised ? monthlyCap.getValue() : plain(data().budgetMonthly()));
        hour = box(116, 124, initialised ? hour.getValue() : String.valueOf(data().resetHour()));
        initialised = true;

        bonusButton = addRenderableWidget(Button.builder(onOff(modBonus), button -> {
            modBonus = !modBonus;
            button.setMessage(onOff(modBonus));
        }).bounds(left + 116, top + 30, 60, 16).build());
        notifyButton = addRenderableWidget(Button.builder(onOff(notify), button -> {
            notify = !notify;
            button.setMessage(onOff(notify));
        }).bounds(left + 116, top + 142, 60, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_config_save"),
                button -> PacketDistributor.sendToServer(DailyConfigActionPayload.settings(modBonus, number(chance),
                        number(dailyCap), number(weeklyCap), number(monthlyCap), (int) number(hour), notify)))
                .bounds(left + 10, top + 166, 166, 16).build());

        for (int i = 0; i < 3; i++) {
            int index = i;
            addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_"
                            + DailyReward.Period.values()[i].getSerializedName()), button -> {
                tab = index;
                selected = -1;
                scroll = 0;
            }).bounds(left + LIST_X + i * 46, top + 28, 44, 16).build());
        }
        money = new EditBox(font, left + LIST_X, top + 146, 62, 16, Component.translatable("screen.ultimatemodadditions.daily_config_money"));
        money.setHint(Component.translatable("screen.ultimatemodadditions.daily_config_money"));
        money.setMaxLength(12);
        money.setFilter(text -> text.matches("[0-9.,]*"));
        addRenderableWidget(money);
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_config_set"),
                button -> entryAction(DailyConfigActionPayload.Action.SET_MONEY, number(money)))
                .bounds(left + LIST_X + 66, top + 146, 68, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_config_hand"),
                button -> entryAction(DailyConfigActionPayload.Action.ADD_HAND_ITEM, 0.0D))
                .bounds(left + LIST_X, top + 166, 42, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_config_clear"),
                button -> entryAction(DailyConfigActionPayload.Action.CLEAR_ITEMS, 0.0D))
                .bounds(left + LIST_X + 44, top + 166, 48, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_config_add"),
                button -> PacketDistributor.sendToServer(DailyConfigActionPayload.entry(
                        DailyConfigActionPayload.Action.ADD_ENTRY, period(), 0, 0.0D)))
                .bounds(left + LIST_X + 94, top + 166, 40, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_config_remove"),
                button -> {
                    entryAction(DailyConfigActionPayload.Action.REMOVE_ENTRY, 0.0D);
                    selected = -1;
                }).bounds(left + LIST_X, top + 186, 66, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.daily_config_restore"),
                button -> PacketDistributor.sendToServer(DailyConfigActionPayload.entry(
                        DailyConfigActionPayload.Action.RESTORE, period(), 0, 0.0D)))
                .bounds(left + LIST_X + 68, top + 186, 66, 16).build());
    }

    private EditBox box(int x, int y, String value) {
        EditBox editBox = new EditBox(font, left + x, top + y - 2, 60, 16, Component.empty());
        editBox.setMaxLength(12);
        editBox.setFilter(text -> text.matches("[0-9.,]*"));
        editBox.setValue(value);
        addRenderableWidget(editBox);
        return editBox;
    }

    private static Component onOff(boolean value) {
        return Component.translatable(value ? "screen.ultimatemodadditions.daily_config_on"
                : "screen.ultimatemodadditions.daily_config_off");
    }

    private static String plain(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static double number(EditBox box) {
        try {
            return Math.max(0.0D, Double.parseDouble(box.getValue().replace(',', '.')));
        } catch (NumberFormatException exception) {
            return 0.0D;
        }
    }

    private void entryAction(DailyConfigActionPayload.Action action, double value) {
        if (selected >= 0 && selected < entries().size()) {
            PacketDistributor.sendToServer(DailyConfigActionPayload.entry(action, period(), selected, value));
        }
    }

    public void refresh() {
        selected = Math.min(selected, entries().size() - 1);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, entries().size() - ROWS)));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        double x = mouseX - left - LIST_X;
        double y = mouseY - top - LIST_Y;
        if (x >= 0 && x < LIST_WIDTH && y >= 0 && y < ROW * ROWS) {
            int index = scroll + (int) (y / ROW);
            if (index < entries().size()) {
                selected = index;
                money.setValue(plain(entries().get(index).money()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(scroll - (int) Math.signum(scrollY), Math.max(0, entries().size() - ROWS)));
        return true;
    }

    private void raised(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, OUTLINE);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, PANEL);
        graphics.fill(x + 1, y + 1, x + w - 1, y + 2, LIGHT);
        graphics.fill(x + 1, y + 1, x + 2, y + h - 1, LIGHT);
        graphics.fill(x + 2, y + h - 2, x + w - 1, y + h - 1, SHADE);
        graphics.fill(x + w - 2, y + 2, x + w - 1, y + h - 1, SHADE);
    }

    private void inset(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, SLOT_FILL);
        graphics.fill(x, y, x + w, y + 1, SLOT_DARK);
        graphics.fill(x, y, x + 1, y + h, SLOT_DARK);
        graphics.fill(x + w - 1, y + 1, x + w, y + h, LIGHT);
        graphics.fill(x + 1, y + h - 1, x + w, y + h, LIGHT);
    }

    private void fitted(GuiGraphics graphics, Component text, int x, int y, int maxWidth, int color) {
        float fit = Math.min(1.0F, (float) maxWidth / Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + (1.0F - fit) * 4.0F, 0.0F);
        graphics.pose().scale(fit, fit, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        raised(graphics, left, top, WIDTH, HEIGHT);
        raised(graphics, left + WIDTH / 2 - 70, top + 5, 140, 17);
        Component header = title.copy();
        graphics.drawString(font, header, left + WIDTH / 2 - font.width(header) / 2, top + 10, TEXT, false);

        String[] labels = {"bonus", "chance", "daily", "weekly", "monthly", "hour", "notify"};
        int[] rows = {34, 54, 72, 90, 108, 126, 146};
        for (int i = 0; i < labels.length; i++) {
            fitted(graphics, Component.translatable("screen.ultimatemodadditions.daily_config_" + labels[i]),
                    left + 10, top + rows[i], 100, TEXT);
        }

        inset(graphics, left + LIST_X - 1, top + LIST_Y - 1, LIST_WIDTH + 2, ROW * ROWS + 2);
        List<EntryView> entries = entries();
        for (int i = 0; i < ROWS && scroll + i < entries.size(); i++) {
            int index = scroll + i;
            EntryView entry = entries.get(index);
            int y = top + LIST_Y + i * ROW;
            graphics.fill(left + LIST_X, y, left + LIST_X + LIST_WIDTH, y + ROW, i % 2 == 0 ? ROW_A : ROW_B);
            if (index == selected) {
                graphics.renderOutline(left + LIST_X, y, LIST_WIDTH, ROW, ORANGE);
            }
            List<String> parts = new ArrayList<>();
            if (entry.money() > 0.0D) {
                parts.add(Money.format(Math.round(entry.money() * Money.toCents(1L))).getString());
            }
            for (ItemStack stack : entry.items()) {
                parts.add(stack.getCount() + "x " + stack.getHoverName().getString());
            }
            fitted(graphics, Component.translatable("screen.ultimatemodadditions.daily_config_row", index + 1,
                    parts.isEmpty() ? "-" : String.join(" + ", parts)), left + LIST_X + 3, y + 3, LIST_WIDTH - 6,
                    index == selected ? GREEN : TEXT);
        }
    }
}
