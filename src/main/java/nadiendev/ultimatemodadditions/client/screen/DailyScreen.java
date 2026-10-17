package nadiendev.ultimatemodadditions.client.screen;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.network.payload.DailyClaimPayload;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload.EntryView;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload.PeriodView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DailyScreen extends Screen {

    private static final ResourceLocation TEXTURE = UMA.id("textures/gui/daily_rewards.png");
    private static final ResourceLocation PIXEL_FONT = UMA.id("pixel");
    private static final int TEXTURE_WIDTH = 552;
    private static final int TEXTURE_HEIGHT = 300;
    private static final int WIDTH = 276;
    private static final int HEIGHT = 150;
    private static final int[] CENTERS = {55, 138, 221};
    private static final int FRAME_TOP = 53;
    private static final int FRAME_SIZE = 34;

    private static final int CREAM = 0xFFE8DCC8;
    private static final int LABEL = 0xFFCFC9D8;
    private static final int GREEN = 0xFF7DFF9B;
    private static final int GOLD = 0xFFFFD36A;
    private static final int GRAY = 0xFFA8A3B8;

    private int left;
    private int top;

    public DailyScreen() {
        super(Component.translatable("screen.ultimatemodadditions.daily"));
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        rebuild();
    }

    public void refresh() {
        rebuild();
    }

    private List<PeriodView> periods() {
        return ClientHooks.daily().periods();
    }

    private void rebuild() {
        clearWidgets();
        List<PeriodView> periods = periods();
        for (int i = 0; i < periods.size() && i < CENTERS.length; i++) {
            PeriodView view = periods.get(i);
            Component label = view.entries().isEmpty()
                    ? Component.translatable("screen.ultimatemodadditions.daily_empty")
                    : view.available() ? Component.translatable("screen.ultimatemodadditions.daily_claim")
                    : Component.literal(countdown(view.secondsLeft()));
            label = upper(label);
            Button button = Button.builder(label, pressed -> PacketDistributor.sendToServer(
                    new DailyClaimPayload(view.period()))).bounds(left + CENTERS[i] - 38, top + 116, 76, 14).build();
            button.active = view.available() && !view.entries().isEmpty();
            addRenderableWidget(button);
        }
    }

    private static String countdown(long seconds) {
        long days = seconds / 86_400L;
        long hours = (seconds % 86_400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        if (days > 0) {
            return days + " d " + hours + " h";
        }
        if (hours > 0) {
            return hours + " h " + minutes + " m";
        }
        return Math.max(1L, minutes) + " min";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int frameAt(double mouseX, double mouseY) {
        for (int i = 0; i < CENTERS.length; i++) {
            double x = mouseX - left - CENTERS[i] + FRAME_SIZE / 2.0D;
            double y = mouseY - top - FRAME_TOP;
            if (x >= 0 && x < FRAME_SIZE && y >= 0 && y < FRAME_SIZE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int index = frameAt(mouseX, mouseY);
        if (index >= 0 && index < periods().size() && button == 0) {
            PeriodView view = periods().get(index);
            if (view.available() && !view.entries().isEmpty()) {
                PacketDistributor.sendToServer(new DailyClaimPayload(view.period()));
            }
            return true;
        }
        return false;
    }

    private void centered(GuiGraphics graphics, Component text, int centerX, int y, int maxWidth, int color,
                          boolean shadow) {
        float fit = Math.min(1.0F, (float) maxWidth / Math.max(1, font.width(text)));
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, y + (1.0F - fit) * 4.0F, 0.0F);
        graphics.pose().scale(fit, fit, 1.0F);
        graphics.drawString(font, text, -font.width(text) / 2, 0, color, shadow);
        graphics.pose().popPose();
    }

    private static Component px(String text) {
        return Component.literal(text.toUpperCase(Locale.ROOT)).withStyle(style -> style.withFont(PIXEL_FONT));
    }

    private static Component upper(Component text) {
        return px(text.getString());
    }

    private void outlined(GuiGraphics graphics, Component text, int centerX, int y, int color, int outline) {
        int x = centerX - font.width(text) / 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    graphics.drawString(font, text, x + dx, y + dy, outline, false);
                }
            }
        }
        graphics.drawString(font, text, x, y, color, false);
    }

    private int nextIndex(PeriodView view) {
        int size = view.entries().size();
        return view.period() == DailyReward.Period.DAILY && size > 0 ? ClientHooks.daily().streak() % size : 0;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(TEXTURE, left, top, WIDTH, HEIGHT, 0.0F, 0.0F, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH,
                TEXTURE_HEIGHT);
        outlined(graphics, upper(title), left + WIDTH / 2, top + 13, CREAM, 0xFF2B1E16);

        List<PeriodView> periods = periods();
        for (int i = 0; i < periods.size() && i < CENTERS.length; i++) {
            renderColumn(graphics, periods.get(i), left + CENTERS[i], mouseX, mouseY);
        }
        centered(graphics, upper(Component.translatable("screen.ultimatemodadditions.daily_streak",
                ClientHooks.daily().streak())), left + WIDTH / 2, top + 137, 240, GOLD, false);
    }

    private void renderColumn(GuiGraphics graphics, PeriodView view, int centerX, int mouseX, int mouseY) {
        Component name = Component.translatable("screen.ultimatemodadditions.daily_"
                + view.period().getSerializedName());
        centered(graphics, upper(name), centerX, top + 38, 76, LABEL, false);
        int frameLeft = centerX - FRAME_SIZE / 2;
        List<EntryView> entries = view.entries();
        if (entries.isEmpty()) {
            return;
        }
        EntryView next = entries.get(nextIndex(view));
        ItemStack icon = next.items().isEmpty() ? new ItemStack(Items.GOLD_INGOT) : next.items().get(0);
        graphics.pose().pushPose();
        graphics.pose().translate(frameLeft + 1, top + FRAME_TOP + 1, 0.0F);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        graphics.renderItem(icon, 0, 0);
        graphics.pose().popPose();
        if (next.items().size() > 1) {
            String more = "+" + (next.items().size() - 1);
            graphics.drawString(font, more, frameLeft + FRAME_SIZE - 2 - font.width(more),
                    top + FRAME_TOP + FRAME_SIZE - 9, 0xFFFFFFFF, true);
        }
        if (!view.available()) {
            graphics.fill(frameLeft, top + FRAME_TOP, frameLeft + FRAME_SIZE, top + FRAME_TOP + FRAME_SIZE, 0x99100E14);
            centered(graphics, Component.literal("✔"), centerX, top + FRAME_TOP + 13, 20, GREEN, true);
        } else if (frameAt(mouseX, mouseY) >= 0 && centerX == left + CENTERS[frameAt(mouseX, mouseY)]) {
            graphics.fill(frameLeft, top + FRAME_TOP, frameLeft + FRAME_SIZE, top + FRAME_TOP + FRAME_SIZE, 0x30FFFFFF);
        }
        if (next.money() > 0.0D) {
            centered(graphics, Money.format(Math.round(next.money() * Money.toCents(1L))), centerX, top + 94, 80,
                    GREEN, false);
        }
        if (view.period() == DailyReward.Period.DAILY) {
            centered(graphics, upper(Component.translatable("screen.ultimatemodadditions.daily_day", nextIndex(view) + 1,
                    entries.size())), centerX, top + 104, 80, GRAY, false);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int index = frameAt(mouseX, mouseY);
        if (index < 0 || index >= periods().size() || periods().get(index).entries().isEmpty()) {
            return;
        }
        PeriodView view = periods().get(index);
        EntryView next = view.entries().get(nextIndex(view));
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("screen.ultimatemodadditions.daily_" + view.period().getSerializedName())
                .withStyle(ChatFormatting.YELLOW));
        if (next.money() > 0.0D) {
            lines.add(Money.format(Math.round(next.money() * Money.toCents(1L))).copy().withStyle(ChatFormatting.GREEN));
        }
        for (ItemStack stack : next.items()) {
            lines.add(Component.literal(stack.getCount() + "x ").append(stack.getHoverName())
                    .withStyle(ChatFormatting.WHITE));
        }
        graphics.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }
}
