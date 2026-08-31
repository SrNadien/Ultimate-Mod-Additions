package nadiendev.ultimatemodadditions.coliseum;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class ColiseumKeyItem extends Item {

    public ColiseumKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !ColiseumPortalShape.isFrame(context.getLevel().getBlockState(context.getClickedPos()))) {
            return InteractionResult.PASS;
        }
        return ColiseumPortal.activate(context.getLevel(), context.getClickedPos(), player, context.getItemInHand());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.coliseum_key")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ultimatemodadditions.coliseum_key_corners")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
