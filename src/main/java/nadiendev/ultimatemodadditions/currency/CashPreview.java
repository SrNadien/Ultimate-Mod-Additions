package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CashPreview(ResourceLocation texture, int width, int height) implements TooltipComponent {

    private static final int COIN_SIZE = 64;
    private static final int BILL_WIDTH = 96;
    private static final int BILL_HEIGHT = 40;
    private static final int WALLET_SIZE = 96;
    private static final int CARD_WIDTH = 96;
    private static final int CARD_HEIGHT = 60;

    public static CashPreview of(Denomination denomination) {
        ResourceLocation texture = texture(denomination.itemName());
        if (denomination.kind() == Denomination.Kind.COIN) {
            return new CashPreview(texture, COIN_SIZE, COIN_SIZE);
        }
        return new CashPreview(texture, BILL_WIDTH, BILL_HEIGHT);
    }

    public static CashPreview wallet() {
        return new CashPreview(texture("wallet"), WALLET_SIZE, WALLET_SIZE);
    }

    public static CashPreview card() {
        return new CashPreview(texture("debit_card"), CARD_WIDTH, CARD_HEIGHT);
    }

    private static ResourceLocation texture(String name) {
        return UMA.id("textures/gui/cash/" + name + ".png");
    }
}
