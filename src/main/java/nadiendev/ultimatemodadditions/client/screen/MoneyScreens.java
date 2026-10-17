package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.network.payload.MoneyActionPayload;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigInteger;

final class MoneyScreens {

    static final int PANEL = 0xFF141419;
    static final int PANEL_EDGE = 0xFF6A5B2E;
    static final int PANEL_INNER_EDGE = 0xFF3B3323;
    static final int SLOT_FILL = 0xFF2A2A33;
    static final int SLOT_SHADOW = 0xFF101014;
    static final int SLOT_LIGHT = 0xFF5A5A6A;
    static final int SEPARATOR = 0xFF3A3A48;
    static final int GOLD = 0xFFE7C25A;

    private MoneyScreens() {
    }

    static void panel(GuiGraphics graphics, int left, int top, int width, int height) {
        graphics.fill(left, top, left + width, top + height, PANEL);
        graphics.renderOutline(left, top, width, height, PANEL_EDGE);
        graphics.renderOutline(left + 2, top + 2, width - 4, height - 4, PANEL_INNER_EDGE);
    }

    static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, SLOT_FILL);
        graphics.fill(x, y, x + 18, y + 1, SLOT_SHADOW);
        graphics.fill(x, y, x + 1, y + 18, SLOT_SHADOW);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, SLOT_LIGHT);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, SLOT_LIGHT);
    }

    static Button action(int x, int y, int width, Component label, MoneyActionPayload.Target target,
                         MoneyActionPayload.Action action, BigInteger cents) {
        return Button.builder(label, button ->
                        PacketDistributor.sendToServer(new MoneyActionPayload(target, action, cents.toString())))
                .bounds(x, y, width, 14)
                .build();
    }

    static void fitted(GuiGraphics graphics, Font font, Component text, int x, int y, int maxWidth, int color) {
        float fit = Math.min(1.0F, (float) maxWidth / Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y + (1.0F - fit) * 4.0F, 0.0F);
        graphics.pose().scale(fit, fit, 1.0F);
        graphics.drawString(font, text, 0, 0, color, true);
        graphics.pose().popPose();
    }

    static Component amountLabel(long mangos) {
        return Money.format(Money.toCents(mangos));
    }
}
