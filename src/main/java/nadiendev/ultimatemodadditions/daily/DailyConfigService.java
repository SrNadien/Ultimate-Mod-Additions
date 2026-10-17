package nadiendev.ultimatemodadditions.daily;

import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.data.JobDefinition;
import nadiendev.ultimatemodadditions.network.payload.DailyConfigActionPayload;
import nadiendev.ultimatemodadditions.network.payload.DailyConfigPayload;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class DailyConfigService {

    private DailyConfigService() {
    }

    public static void open(ServerPlayer player) {
        send(player, true);
    }

    private static void send(ServerPlayer player, boolean open) {
        List<DailyConfigPayload.ConfigPeriod> periods = new ArrayList<>();
        for (DailyReward.Period period : DailyReward.Period.values()) {
            List<DailySyncPayload.EntryView> views = new ArrayList<>();
            for (DailyReward.Entry entry : DailyService.entries(player.server, period)) {
                List<ItemStack> items = new ArrayList<>();
                for (JobDefinition.RewardItem item : entry.items()) {
                    items.add(new ItemStack(item.item(), item.count()));
                }
                views.add(new DailySyncPayload.EntryView(entry.money(), items));
            }
            periods.add(new DailyConfigPayload.ConfigPeriod(period, views));
        }
        PacketDistributor.sendToPlayer(player, new DailyConfigPayload(open, UMAConfig.COMMON.dailyModBonus.get(),
                UMAConfig.COMMON.dailyModBonusChance.get(), UMAConfig.COMMON.dailyModBonusDaily.get(),
                UMAConfig.COMMON.dailyModBonusWeekly.get(), UMAConfig.COMMON.dailyModBonusMonthly.get(),
                UMAConfig.COMMON.dailyResetHour.get(), UMAConfig.COMMON.dailyNotify.get(), periods));
    }

    public static void handle(ServerPlayer player, DailyConfigActionPayload payload) {
        if (!DailyPermissions.canConfigure(player)) {
            player.displayClientMessage(Component.translatable("message.ultimatemodadditions.daily_no_permission")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        DailyEdits edits = DailyEdits.get(player.server);
        DailyReward.Period period = payload.period();
        switch (payload.action()) {
            case SETTINGS -> settings(payload);
            case RESTORE -> edits.clear();
            case ADD_ENTRY -> editable(player, edits, period).add(new DailyReward.Entry(0.0D, List.of(), List.of()));
            default -> entryAction(player, edits, payload);
        }
        send(player, false);
        DailyService.sync(player, false);
    }

    private static List<DailyReward.Entry> editable(ServerPlayer player, DailyEdits edits, DailyReward.Period period) {
        return edits.editable(period, DailyService.entries(player.server, period));
    }

    private static void settings(DailyConfigActionPayload payload) {
        UMAConfig.COMMON.dailyModBonus.set(payload.flagOne());
        UMAConfig.COMMON.dailyModBonusChance.set(Math.max(0.0D, Math.min(100.0D, payload.first())));
        UMAConfig.COMMON.dailyModBonusDaily.set(Math.max(0.0D, payload.second()));
        UMAConfig.COMMON.dailyModBonusWeekly.set(Math.max(0.0D, payload.third()));
        UMAConfig.COMMON.dailyModBonusMonthly.set(Math.max(0.0D, payload.fourth()));
        UMAConfig.COMMON.dailyResetHour.set(Math.max(0, Math.min(23, payload.hour())));
        UMAConfig.COMMON.dailyNotify.set(payload.flagTwo());
        UMAConfig.COMMON_SPEC.save();
    }

    private static void entryAction(ServerPlayer player, DailyEdits edits, DailyConfigActionPayload payload) {
        List<DailyReward.Entry> list = editable(player, edits, payload.period());
        int index = payload.index();
        if (index < 0 || index >= list.size()) {
            return;
        }
        DailyReward.Entry entry = list.get(index);
        switch (payload.action()) {
            case SET_MONEY -> list.set(index, new DailyReward.Entry(Math.max(0.0D, payload.first()), entry.items(),
                    entry.commands()));
            case CLEAR_ITEMS -> list.set(index, new DailyReward.Entry(entry.money(), List.of(), entry.commands()));
            case REMOVE_ENTRY -> list.remove(index);
            case ADD_HAND_ITEM -> {
                ItemStack held = player.getMainHandItem();
                if (held.isEmpty()) {
                    player.displayClientMessage(Component.translatable("message.ultimatemodadditions.shop_admin_need_hand")
                            .withStyle(ChatFormatting.RED), true);
                    return;
                }
                List<JobDefinition.RewardItem> items = new ArrayList<>(entry.items());
                items.add(new JobDefinition.RewardItem(held.getItem(), held.getCount()));
                list.set(index, new DailyReward.Entry(entry.money(), items, entry.commands()));
            }
            default -> {
            }
        }
    }
}
