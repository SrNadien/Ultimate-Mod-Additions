package nadiendev.ultimatemodadditions.network;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.currency.ExchangeLogic;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.network.payload.BuyOfferPayload;
import nadiendev.ultimatemodadditions.network.payload.DataSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = UMA.MODID)
public final class UMANetwork {

    private UMANetwork() {
    }

    @EventBusSubscriber(modid = UMA.MODID)
    public static final class ModBus {
        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(DataSyncPayload.TYPE, DataSyncPayload.STREAM_CODEC, UMANetwork::handleDataSync);
            registrar.playToServer(BuyOfferPayload.TYPE, BuyOfferPayload.STREAM_CODEC, UMANetwork::handleBuy);
        }
    }

    private static void handleDataSync(DataSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> SyncedData.accept(payload.bags(), payload.trades()));
    }

    private static void handleBuy(BuyOfferPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ExchangeLogic.buy(serverPlayer, payload.offer(), payload.amount());
            }
        });
    }


    public static void syncData(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new DataSyncPayload(UMAData.lootBags(), UMAData.trades()));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            syncData(event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(player -> {
                syncData(player);
            });
        }
    }
}
