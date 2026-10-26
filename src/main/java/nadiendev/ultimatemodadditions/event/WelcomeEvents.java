package nadiendev.ultimatemodadditions.event;

import com.google.gson.JsonObject;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.compat.ModIds;
import nadiendev.ultimatemodadditions.config.EventConfigs;
import nadiendev.ultimatemodadditions.config.JsonValues;
import nadiendev.ultimatemodadditions.config.UMAConfig.PayTarget;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Payout;
import nadiendev.ultimatemodadditions.currency.Wealth;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.math.BigInteger;

@EventBusSubscriber(modid = UMA.MODID)
public final class WelcomeEvents {

    public static final String ID = "welcome";

    private WelcomeEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        EventState state = EventState.get(player.server);
        if (!state.enabled(ID)) {
            return;
        }
        JsonObject config = EventConfigs.get(EventDefaults.WELCOME);
        startingMoney(player, state, JsonValues.object(config, "starting_money"));
        questNotice(player, state, JsonValues.object(config, "quest_notice"));
    }

    private static void startingMoney(ServerPlayer player, EventState state, JsonObject money) {
        String key = JsonValues.string(money, "key", "starting_money_1");
        long mangos = JsonValues.number(money, "mangos", 2500L);
        if (!JsonValues.bool(money, "enabled", true) || mangos <= 0L || state.flag(player.getUUID(), key)) {
            return;
        }
        state.setFlag(player.getUUID(), key);
        BigInteger cents = Wealth.centsOfMangos(BigInteger.valueOf(mangos));
        Payout.give(player, cents, PayTarget.ACCOUNT);
        BankService.sendOwn(player);
        String custom = JsonValues.string(money, "message", "");
        Scheduler.after(JsonValues.integer(money, "delay_ticks", 160), () -> {
            if (player.hasDisconnected()) {
                return;
            }
            player.sendSystemMessage((custom.isEmpty()
                    ? Component.translatable("message.ultimatemodadditions.starting_money", Money.format(cents))
                    : Component.literal(custom)).copy().withStyle(ChatFormatting.GOLD));
        });
    }

    private static void questNotice(ServerPlayer player, EventState state, JsonObject notice) {
        if (!JsonValues.bool(notice, "enabled", true)
                || JsonValues.bool(notice, "only_with_ftb_quests", true) && !ModIds.loaded(ModIds.FTB_QUESTS)) {
            return;
        }
        String key = JsonValues.string(notice, "key", "quest_notice_1");
        boolean first = !state.flag(player.getUUID(), key);
        if (first) {
            state.setFlag(player.getUUID(), key);
        }
        boolean titleOnlyFirst = JsonValues.bool(notice, "title_first_join_only", true);
        Scheduler.after(JsonValues.integer(notice, "delay_ticks", 200), () -> {
            if (player.hasDisconnected()) {
                return;
            }
            String chat = JsonValues.string(notice, "chat_message", "");
            player.sendSystemMessage(chat.isEmpty()
                    ? Component.translatable("message.ultimatemodadditions.quest_notice").withStyle(ChatFormatting.GOLD)
                    : Component.literal(chat).withStyle(ChatFormatting.GOLD));
            if (first || !titleOnlyFirst) {
                String title = JsonValues.string(notice, "title", "");
                String subtitle = JsonValues.string(notice, "subtitle", "");
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 100, 20));
                player.connection.send(new ClientboundSetSubtitleTextPacket((subtitle.isEmpty()
                        ? Component.translatable("message.ultimatemodadditions.quest_notice_subtitle")
                        : Component.literal(subtitle)).copy().withStyle(ChatFormatting.YELLOW)));
                player.connection.send(new ClientboundSetTitleTextPacket((title.isEmpty()
                        ? Component.translatable("message.ultimatemodadditions.quest_notice_title")
                        : Component.literal(title)).copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
            }
        });
    }
}
