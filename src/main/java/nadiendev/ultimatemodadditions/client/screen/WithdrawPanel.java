package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.network.payload.MoneyActionPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class WithdrawPanel {

    static final int HEIGHT = 60;

    private static final int BOX_LEFT = 32;
    private static final int BOX_WIDTH = 72;
    private static final long CENTS = 100L;

    private final MoneyActionPayload.Target target;
    private final List<Long> steps = new ArrayList<>();
    private final long limit;
    private int stepIndex;
    private String text;
    private Button stepButton;
    private EditBox box;

    WithdrawPanel(MoneyActionPayload.Target target, List<? extends Number> configured) {
        this.target = target;
        for (Number number : configured) {
            if (number.longValue() > 0L) {
                steps.add(number.longValue());
            }
        }
        if (steps.isEmpty()) {
            steps.add(100L);
        }
        stepIndex = steps.size() > 1 ? 1 : 0;
        limit = UMAConfig.COMMON.maxWithdrawMangos.get();
        text = plain(steps.get(stepIndex) * CENTS);
    }

    void init(Consumer<AbstractWidget> add, Font font, int left, int top, int y) {
        add.accept(Button.builder(Component.literal("-"), button -> change(-1))
                .bounds(left + 8, top + y + 12, 22, 18).build());
        box = new EditBox(font, left + BOX_LEFT, top + y + 12, BOX_WIDTH, 18,
                Component.translatable("screen.ultimatemodadditions.withdraw"));
        box.setMaxLength(18);
        box.setFilter(value -> value.matches("[0-9]*([.,][0-9]{0,2})?"));
        box.setTextColor(0xFFE7C25A);
        box.setValue(text);
        box.setResponder(value -> text = value);
        add.accept(box);
        add.accept(Button.builder(Component.literal("+"), button -> change(1))
                .bounds(left + 106, top + y + 12, 22, 18).build());
        stepButton = Button.builder(stepLabel(), button -> cycleStep())
                .bounds(left + 130, top + y + 12, 38, 18).build();
        add.accept(stepButton);
        add.accept(Button.builder(Component.translatable("screen.ultimatemodadditions.withdraw_button"),
                        button -> send(MoneyActionPayload.Action.WITHDRAW, String.valueOf(cents())))
                .bounds(left + 8, top + y + 36, 76, 18).build());
        add.accept(Button.builder(Component.translatable("screen.ultimatemodadditions.withdraw_all"),
                        button -> send(MoneyActionPayload.Action.WITHDRAW_ALL, "0"))
                .bounds(left + 92, top + y + 36, 76, 18).build());
    }

    void renderLabels(GuiGraphics graphics, Font font, int y) {
        graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.withdraw"),
                8, y, 0xFFB0B0B0, true);
    }

    private long cents() {
        String value = text.replace(',', '.');
        if (value.isEmpty() || value.equals(".")) {
            return 0L;
        }
        long parsed = new BigDecimal(value).multiply(BigDecimal.valueOf(CENTS)).setScale(0, RoundingMode.DOWN)
                .min(BigDecimal.valueOf(limit * CENTS)).longValue();
        return Math.max(0L, parsed);
    }

    private static String plain(long cents) {
        return (cents / CENTS) + "," + String.format("%02d", cents % CENTS);
    }

    private void change(int direction) {
        long next = cents() + direction * steps.get(stepIndex) * CENTS;
        long clamped = Math.max(CENTS, Math.min(limit * CENTS, next));
        text = plain(clamped);
        box.setValue(text);
    }

    private void cycleStep() {
        stepIndex = (stepIndex + 1) % steps.size();
        stepButton.setMessage(stepLabel());
    }

    private Component stepLabel() {
        return Component.literal("±" + compact(steps.get(stepIndex)));
    }

    private static String compact(long value) {
        if (value >= 1_000_000L && value % 1_000_000L == 0L) {
            return (value / 1_000_000L) + "M";
        }
        if (value >= 1_000L && value % 1_000L == 0L) {
            return (value / 1_000L) + "k";
        }
        return String.valueOf(value);
    }

    private void send(MoneyActionPayload.Action action, String cents) {
        PacketDistributor.sendToServer(new MoneyActionPayload(target, action, cents));
    }
}
