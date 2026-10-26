package nadiendev.ultimatemodadditions.client;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.ban.BanAction;
import nadiendev.ultimatemodadditions.ban.ClientBans;
import nadiendev.ultimatemodadditions.network.payload.BanSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = UMA.MODID, value = Dist.CLIENT)
public final class BanTooltips {

    private BanTooltips() {
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        BanSyncPayload.Entry entry = ClientBans.get(event.getItemStack().getItem());
        if (entry == null) {
            return;
        }
        List<Component> lines = event.getToolTip();
        int at = Math.min(1, lines.size());
        ChatFormatting color = entry.disabled() ? ChatFormatting.RED : ChatFormatting.DARK_RED;
        lines.add(at, Component.translatable(entry.disabled() ? "tooltip.ultimatemodadditions.ban_disabled_title"
                : "tooltip.ultimatemodadditions.ban_banned_title").withStyle(color, ChatFormatting.BOLD));
        lines.add(at + 1, entry.message().isEmpty()
                ? Component.translatable(entry.disabled() ? "tooltip.ultimatemodadditions.ban_disabled_text"
                : "tooltip.ultimatemodadditions.ban_banned_text").withStyle(ChatFormatting.GRAY)
                : Component.literal(entry.message()).withStyle(ChatFormatting.GRAY));
        lines.add(at + 2, Component.translatable("tooltip.ultimatemodadditions.ban_modes",
                BanAction.describe(BanAction.fromMask(entry.modes()))).withStyle(ChatFormatting.DARK_GRAY));
    }
}
