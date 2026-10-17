package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class CashItem extends Item {

    private final Denomination denomination;

    public CashItem(Properties properties, Denomination denomination) {
        super(properties);
        this.denomination = denomination;
    }

    public Denomination denomination() {
        return denomination;
    }

    public long cents() {
        return denomination.cents();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return use(context.getLevel(), player, context.getHand()).getResult();
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
        if (player.isShiftKeyDown()) {
            if (!Money.split(player, stack)) {
                return InteractionResultHolder.fail(stack);
            }
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.ultimatemodadditions.money_split", Money.format(cents()))
                    .withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.consume(stack);
        }
        long total = Money.consolidate(player);
        serverPlayer.displayClientMessage(Component.translatable(
                "message.ultimatemodadditions.money_total", Money.format(total))
                .withStyle(ChatFormatting.GOLD), true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!UMAConfig.COMMON.cashPreview.get()) {
            return Optional.empty();
        }
        return Optional.of(CashPreview.of(denomination));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.cash_value",
                Money.format(cents() * stack.getCount())).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.cash_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
