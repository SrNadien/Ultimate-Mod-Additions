package nadiendev.ultimatemodadditions.compat.ftb;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.network.payload.RentActionPayload;
import nadiendev.ultimatemodadditions.network.payload.RentSyncPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.math.BigInteger;

public final class RentNetwork {

    private RentNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(RentSyncPayload.TYPE, RentSyncPayload.STREAM_CODEC, RentNetwork::handleSync);
        registrar.playToServer(RentActionPayload.TYPE, RentActionPayload.STREAM_CODEC, RentNetwork::handleAction);
    }

    private static void handleSync(RentSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientHooks.acceptRent(payload);
            }
        });
    }

    private static void handleAction(RentActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !UMAConfig.COMMON.rentEnabled.get()) {
                return;
            }
            MinecraftServer server = player.server;
            Team team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
            if (team != null) {
                RentService.PayResult result = RentService.payNow(server, team, player);
                BigInteger cost = RentService.price(RentService.claimCount(team));
                switch (result) {
                    case PAID -> player.sendSystemMessage(Component.translatable(
                            "command.ultimatemodadditions.rent_paid", Money.format(cost),
                            RentService.duration(RentService.paidUntil(team) - RentService.now(server)))
                            .withStyle(ChatFormatting.GREEN));
                    case NO_FUNDS -> player.sendSystemMessage(Component.translatable(
                            "command.ultimatemodadditions.money_no_funds").withStyle(ChatFormatting.RED));
                    case AHEAD -> player.sendSystemMessage(Component.translatable(
                            "command.ultimatemodadditions.rent_ahead").withStyle(ChatFormatting.YELLOW));
                    case NO_CHUNKS -> player.sendSystemMessage(Component.translatable(
                            "command.ultimatemodadditions.rent_status_none").withStyle(ChatFormatting.YELLOW));
                }
            }
            BankService.sendOwn(player);
            sync(player, false);
        });
    }

    public static void sync(ServerPlayer player, boolean open) {
        MinecraftServer server = player.server;
        Team team = FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
        int periodDays = UMAConfig.COMMON.rentPeriodDays.get();
        long dayMs = RentService.dayLength();
        RentSyncPayload payload;
        if (team == null) {
            payload = new RentSyncPayload(open, false, "", 0, "0", periodDays, 0L, -1L, dayMs, false);
        } else {
            int chunks = RentService.claimCount(team);
            long now = RentService.now(server);
            long paidUntil = RentService.paidUntil(team);
            long overdue = RentService.overdueSince(team);
            long grace = (long) (UMAConfig.COMMON.rentGraceDays.get() * dayMs);
            long remaining = paidUntil > 0L ? paidUntil - now : 0L;
            long graceLeft = overdue > 0L ? Math.max(0L, grace - (now - overdue)) : -1L;
            boolean canPay = chunks > 0 && remaining <= RentService.period();
            payload = new RentSyncPayload(open, true, team.getName().getString(), chunks,
                    RentService.price(chunks).toString(), periodDays, remaining, graceLeft, dayMs, canPay);
        }
        PacketDistributor.sendToPlayer(player, payload);
    }
}
