package nadiendev.ultimatemodadditions.client;

import nadiendev.ultimatemodadditions.currency.CashPreview;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class ClientCashPreview implements ClientTooltipComponent {

    private static final int PADDING = 2;

    private final CashPreview preview;

    public ClientCashPreview(CashPreview preview) {
        this.preview = preview;
    }

    @Override
    public int getHeight() {
        return preview.height() + PADDING * 2;
    }

    @Override
    public int getWidth(Font font) {
        return preview.width();
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int textureWidth = preview.width() * 2;
        int textureHeight = preview.height() * 2;
        graphics.blit(preview.texture(), x, y + PADDING, preview.width(), preview.height(),
                0.0F, 0.0F, textureWidth, textureHeight, textureWidth, textureHeight);
    }
}
