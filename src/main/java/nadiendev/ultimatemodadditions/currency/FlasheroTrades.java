package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.BasicItemListing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlasheroTrades {

    private static final Pattern TRADE = Pattern.compile("(\\d+)x([a-z0-9_]+)=(\\d+)x([a-z0-9_]+)");

    private FlasheroTrades() {
    }

    public static List<ItemListing> listings() {
        List<ItemListing> listings = new ArrayList<>();
        if (!UMAConfig.COMMON.flasheroEnabled.get()) {
            return listings;
        }
        int maxUses = UMAConfig.COMMON.flasheroMaxUses.get();
        int xp = UMAConfig.COMMON.flasheroXp.get();
        for (String entry : UMAConfig.COMMON.flasheroTrades.get()) {
            Matcher matcher = TRADE.matcher(entry.trim());
            if (!matcher.matches()) {
                UMA.LOGGER.warn("Skipping malformed Flashero trade '{}'", entry);
                continue;
            }
            Denomination give = exact(matcher.group(2));
            Denomination get = exact(matcher.group(4));
            if (give == null || get == null) {
                UMA.LOGGER.warn("Skipping Flashero trade '{}': unknown denomination", entry);
                continue;
            }
            int giveCount = Integer.parseInt(matcher.group(1));
            int getCount = Integer.parseInt(matcher.group(3));
            if (giveCount < 1 || getCount < 1 || giveCount > 64 || getCount > 64) {
                UMA.LOGGER.warn("Skipping Flashero trade '{}': counts must be between 1 and 64", entry);
                continue;
            }
            listings.add(new BasicItemListing(
                    new ItemStack(UMAItems.cash(give), giveCount),
                    new ItemStack(UMAItems.cash(get), getCount),
                    maxUses, xp, 0.0F));
        }
        return listings;
    }

    private static Denomination exact(String key) {
        for (Denomination denomination : Denomination.values()) {
            if (denomination.key().equals(key)) {
                return denomination;
            }
        }
        return null;
    }
}
