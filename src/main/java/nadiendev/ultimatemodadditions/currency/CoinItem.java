package nadiendev.ultimatemodadditions.currency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CoinItem extends Item {

    private final Denomination denomination;

    public CoinItem(Properties properties, Denomination denomination) {
        super(properties);
        this.denomination = denomination;
    }

    public Denomination denomination() {
        return denomination;
    }

    public String tier() {
        return denomination.key();
    }

    public int value() {
        return denomination.value();
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return net.minecraft.world.InteractionResult.PASS;
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
                    "message.ultimatemodadditions.money_split", Money.format(value()))
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.coin_value",
                Money.format((long) value() * stack.getCount())).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.coin_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
