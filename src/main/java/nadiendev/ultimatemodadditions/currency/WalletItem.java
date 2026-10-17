package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class WalletItem extends Item {

    public WalletItem(Properties properties) {
        super(properties);
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
        if (WalletStorage.balance(stack).signum() == 0) {
            serverPlayer.displayClientMessage(Component.translatable("message.ultimatemodadditions.wallet_empty")
                    .withStyle(ChatFormatting.RED), true);
        }
        BankService.sendWallet(serverPlayer, WalletStorage.balance(stack));
        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, owner) -> new WalletMenu(id, inventory, hand),
                Component.translatable("container.ultimatemodadditions.wallet")),
                buffer -> buffer.writeEnum(hand));
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!UMAConfig.COMMON.cashPreview.get()) {
            return Optional.empty();
        }
        return Optional.of(CashPreview.wallet());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.wallet_total",
                Money.format(WalletStorage.balance(stack))).withStyle(ChatFormatting.GOLD));
    }
}
