package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.data.TradeOffer;
import nadiendev.ultimatemodadditions.network.SyncedData;
import nadiendev.ultimatemodadditions.registry.UMATags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ExchangeLogic {

    private ExchangeLogic() {
    }

    public static boolean purchasable(TradeOffer offer) {
        TradeOffer.ResultEntry result = offer.result();
        if (result.bag().isPresent() && !LootBagItem.tierOf(result.bag().get()).purchasable()) {
            return false;
        }
        return result.item().map(item -> !new ItemStack(item).is(UMATags.NOT_PURCHASABLE)).orElse(true);
    }

    public static boolean buy(ServerPlayer player, ResourceLocation offerId, int amount) {
        TradeOffer offer = SyncedData.trades().get(offerId);
        if (offer == null || !offer.enabled()) {
            return false;
        }
        if (!purchasable(offer)) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.trade_forbidden")
                    .withStyle(ChatFormatting.RED), true);
            return false;
        }
        int times = Math.max(1, Math.min(amount, 64));
        int affordable = Math.min(times, maxAffordable(player, offer));
        if (affordable <= 0) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.trade_too_poor",
                    Money.format(offer.price())).withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (offer.price() > 0 && !Money.take(player, (long) offer.price() * affordable)) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.trade_too_poor",
                    Money.format(offer.price())).withStyle(ChatFormatting.RED), true);
            return false;
        }
        takeCost(player, offer, affordable);
        for (int i = 0; i < affordable; i++) {
            ItemStack result = offer.result().asStack();
            if (result.isEmpty()) {
                continue;
            }
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
        }
        return true;
    }

    public static int maxAffordable(Player player, TradeOffer offer) {
        int best = Integer.MAX_VALUE;
        if (offer.price() > 0) {
            best = (int) Math.min(best, Money.total(player) / offer.price());
        }
        for (TradeOffer.CostEntry entry : offer.cost()) {
            if (entry.count() <= 0) {
                continue;
            }
            best = Math.min(best, count(player, entry) / entry.count());
        }
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    private static int count(Player player, TradeOffer.CostEntry entry) {
        int found = 0;
        for (ItemStack stack : Money.stacks(player)) {
            if (stack.is(entry.item())) {
                found += stack.getCount();
            }
        }
        return found;
    }

    private static void takeCost(Player player, TradeOffer offer, int times) {
        List<ItemStack> stacks = Money.stacks(player);
        for (TradeOffer.CostEntry entry : offer.cost()) {
            int remaining = entry.count() * times;
            for (ItemStack stack : stacks) {
                if (remaining <= 0) {
                    break;
                }
                if (!stack.is(entry.item())) {
                    continue;
                }
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
            }
        }
    }
}
