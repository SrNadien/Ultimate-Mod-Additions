package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.registry.UMADataComponents;
import nadiendev.ultimatemodadditions.registry.UMAItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class DebitCardItem extends Item {

    public DebitCardItem(Properties properties) {
        super(properties);
    }

    public static CardOwner owner(ItemStack card) {
        return card.get(UMADataComponents.CARD_OWNER);
    }

    public static boolean usableBy(ItemStack card, Player player) {
        CardOwner owner = owner(card);
        if (owner == null) {
            return false;
        }
        return !UMAConfig.COMMON.cardBoundToOwner.get() || owner.id().equals(player.getUUID());
    }

    public static ItemStack create(Player owner) {
        ItemStack card = new ItemStack(UMAItems.DEBIT_CARD.get());
        card.set(UMADataComponents.CARD_OWNER, new CardOwner(owner.getUUID(), owner.getGameProfile().getName()));
        return card;
    }

    public static ItemStack findUsable(Player player) {
        for (ItemStack stack : Money.stacks(player)) {
            if (stack.getItem() instanceof DebitCardItem && usableBy(stack, player)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        if (owner(stack) == null) {
            stack.set(UMADataComponents.CARD_OWNER,
                    new CardOwner(player.getUUID(), player.getGameProfile().getName()));
        }
        if (!usableBy(stack, player)) {
            serverPlayer.displayClientMessage(Component.translatable("message.ultimatemodadditions.card_not_yours")
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        serverPlayer.displayClientMessage(Component.translatable("message.ultimatemodadditions.card_balance",
                Money.format(BankService.balance(serverPlayer.server, owner(stack).id())))
                .withStyle(ChatFormatting.GOLD), true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!UMAConfig.COMMON.cashPreview.get()) {
            return Optional.empty();
        }
        return Optional.of(CashPreview.card());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CardOwner owner = owner(stack);
        if (owner == null) {
            tooltip.add(Component.translatable("tooltip.ultimatemodadditions.card_unbound")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.card_number", owner.maskedNumber())
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.card_owner", owner.name())
                .withStyle(ChatFormatting.GRAY));
    }
}
