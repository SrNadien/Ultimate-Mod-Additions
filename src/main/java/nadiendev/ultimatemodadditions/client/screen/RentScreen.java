package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Wealth;
import nadiendev.ultimatemodadditions.network.ClientBank;
import nadiendev.ultimatemodadditions.network.payload.RentActionPayload;
import nadiendev.ultimatemodadditions.network.payload.RentSyncPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class RentScreen extends Screen {

    private static final int WIDTH = 230;
    private static final int HEIGHT = 150;

    private int left;
    private int top;
    private Button payButton;

    public RentScreen() {
        super(Component.translatable("screen.ultimatemodadditions.rent"));
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        payButton = addRenderableWidget(Button.builder(Component.translatable("screen.ultimatemodadditions.rent_pay"),
                        button -> PacketDistributor.sendToServer(new RentActionPayload()))
                .bounds(left + 10, top + HEIGHT - 28, 100, 18)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(left + WIDTH - 110, top + HEIGHT - 28, 100, 18)
                .build());
        refresh();
    }

    public void refresh() {
        RentSyncPayload data = ClientHooks.rent();
        if (payButton != null && data != null) {
            payButton.active = data.canPay();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component duration(long milliseconds, long dayMs) {
        long days = milliseconds / dayMs;
        long hours = (milliseconds % dayMs) * 24L / dayMs;
        if (days > 0L) {
            return Component.translatable("message.ultimatemodadditions.rent_days_hours", days, hours);
        }
        return Component.translatable("message.ultimatemodadditions.rent_hours", Math.max(0L, hours));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        MoneyScreens.panel(graphics, left, top, WIDTH, HEIGHT);
        graphics.drawString(font, title, left + 8, top + 8, MoneyScreens.GOLD, true);
        graphics.fill(left + 7, top + 22, left + WIDTH - 7, top + 23, MoneyScreens.SEPARATOR);

        RentSyncPayload data = ClientHooks.rent();
        if (data == null || !data.inTeam()) {
            graphics.drawString(font, Component.translatable("command.ultimatemodadditions.rent_no_team"),
                    left + 10, top + 34, 0xFFCC5555, true);
            return;
        }
        graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.rent_team", data.teamName()),
                left + 10, top + 30, 0xFFE8E8E8, true);
        if (data.chunks() == 0) {
            graphics.drawString(font, Component.translatable("command.ultimatemodadditions.rent_status_none"),
                    left + 10, top + 46, 0xFF8A8A8A, true);
            return;
        }
        graphics.drawString(font, Component.translatable("screen.ultimatemodadditions.rent_chunks", data.chunks()),
                left + 10, top + 44, 0xFFB0B0B0, true);
        MoneyScreens.fitted(graphics, font, Component.translatable("command.ultimatemodadditions.rent_every",
                Money.format(Wealth.parse(data.costCents())), data.periodDays()), left + 10, top + 58, WIDTH - 20, 0xFFE7C25A);

        if (data.graceMs() >= 0L) {
            graphics.drawString(font, Component.translatable("command.ultimatemodadditions.rent_status_overdue",
                    duration(data.graceMs(), data.dayMs())), left + 10, top + 76, 0xFFCC5555, true);
        } else {
            graphics.drawString(font, Component.translatable("command.ultimatemodadditions.rent_status_next",
                    duration(Math.max(0L, data.remainingMs()), data.dayMs())), left + 10, top + 76, 0xFF8AE28A, true);
        }
        MoneyScreens.fitted(graphics, font, Component.translatable("screen.ultimatemodadditions.rent_account",
                Money.format(ClientBank.own())), left + 10, top + 94, WIDTH - 20, 0xFFB0B0B0);
    }
}
