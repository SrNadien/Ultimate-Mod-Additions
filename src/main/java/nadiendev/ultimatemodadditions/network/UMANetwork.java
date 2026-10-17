package nadiendev.ultimatemodadditions.network;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.client.ClientHooks;
import nadiendev.ultimatemodadditions.config.UMAConfig;
import nadiendev.ultimatemodadditions.currency.BankService;
import nadiendev.ultimatemodadditions.currency.ExchangeLogic;
import nadiendev.ultimatemodadditions.currency.MoneyActions;
import nadiendev.ultimatemodadditions.currency.Wealth;
import nadiendev.ultimatemodadditions.data.UMAData;
import nadiendev.ultimatemodadditions.jobs.JobsService;
import nadiendev.ultimatemodadditions.daily.DailyConfigService;
import nadiendev.ultimatemodadditions.daily.DailyService;
import nadiendev.ultimatemodadditions.network.payload.BankSyncPayload;
import nadiendev.ultimatemodadditions.network.payload.DailyClaimPayload;
import nadiendev.ultimatemodadditions.network.payload.DailyConfigActionPayload;
import nadiendev.ultimatemodadditions.network.payload.DailyConfigPayload;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload;
import nadiendev.ultimatemodadditions.network.payload.BuyOfferPayload;
import nadiendev.ultimatemodadditions.network.payload.MoneyActionPayload;
import nadiendev.ultimatemodadditions.network.payload.DataSyncPayload;
import nadiendev.ultimatemodadditions.network.payload.JobActionPayload;
import nadiendev.ultimatemodadditions.network.payload.JobsSyncPayload;
import nadiendev.ultimatemodadditions.network.payload.ShopActionPayload;
import nadiendev.ultimatemodadditions.network.payload.ShopSyncPayload;
import nadiendev.ultimatemodadditions.network.payload.WalletBalancePayload;
import nadiendev.ultimatemodadditions.shop.ModShops;
import nadiendev.ultimatemodadditions.shop.ShopService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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
            registrar.playToClient(BankSyncPayload.TYPE, BankSyncPayload.STREAM_CODEC, UMANetwork::handleBankSync);
            registrar.playToServer(MoneyActionPayload.TYPE, MoneyActionPayload.STREAM_CODEC, UMANetwork::handleMoneyAction);
            registrar.playToClient(JobsSyncPayload.TYPE, JobsSyncPayload.STREAM_CODEC, UMANetwork::handleJobsSync);
            registrar.playToClient(WalletBalancePayload.TYPE, WalletBalancePayload.STREAM_CODEC, UMANetwork::handleWalletBalance);
            registrar.playToServer(JobActionPayload.TYPE, JobActionPayload.STREAM_CODEC, UMANetwork::handleJobAction);
            registrar.playToClient(DailySyncPayload.TYPE, DailySyncPayload.STREAM_CODEC, UMANetwork::handleDailySync);
            registrar.playToServer(DailyClaimPayload.TYPE, DailyClaimPayload.STREAM_CODEC, UMANetwork::handleDailyClaim);
            registrar.playToClient(DailyConfigPayload.TYPE, DailyConfigPayload.STREAM_CODEC, UMANetwork::handleDailyConfig);
            registrar.playToServer(DailyConfigActionPayload.TYPE, DailyConfigActionPayload.STREAM_CODEC,
                    UMANetwork::handleDailyConfigAction);
            registrar.playToClient(ShopSyncPayload.TYPE, ShopSyncPayload.STREAM_CODEC, UMANetwork::handleShopSync);
            registrar.playToServer(ShopActionPayload.TYPE, ShopActionPayload.STREAM_CODEC, UMANetwork::handleShopAction);
        }
    }

    private static void handleBankSync(BankSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBank.accept(payload.own(), Wealth.parse(payload.cents())));
    }

    private static void handleWalletBalance(WalletBalancePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientBank.acceptWallet(Wealth.parse(payload.cents())));
    }

    private static void handleJobsSync(JobsSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientHooks.acceptJobs(payload);
            }
        });
    }

    private static void handleDailySync(DailySyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientHooks.acceptDaily(payload);
            }
        });
    }

    private static void handleDailyClaim(DailyClaimPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                DailyService.claim(serverPlayer, payload.period());
            }
        });
    }

    private static void handleDailyConfig(DailyConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientHooks.acceptDailyConfig(payload);
            }
        });
    }

    private static void handleDailyConfigAction(DailyConfigActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                DailyConfigService.handle(serverPlayer, payload);
            }
        });
    }

    private static void handleShopSync(ShopSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                ClientHooks.acceptShop(payload);
            }
        });
    }

    private static void handleShopAction(ShopActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ShopService.handle(serverPlayer, payload);
            }
        });
    }

    private static void handleJobAction(JobActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer && UMAConfig.COMMON.jobsEnabled.get()) {
                if (payload.join()) {
                    JobsService.join(serverPlayer, payload.job());
                } else {
                    JobsService.leave(serverPlayer, payload.job());
                }
                JobsService.sync(serverPlayer, false);
            }
        });
    }

    private static void handleMoneyAction(MoneyActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                MoneyActions.handle(serverPlayer, payload);
            }
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BankService.sendOwn(player);
            if (UMAConfig.COMMON.dailyNotify.get() && DailyService.anythingToClaim(player)) {
                player.sendSystemMessage(Component.translatable(
                        "message.ultimatemodadditions.daily_available").withStyle(ChatFormatting.GOLD));
            }
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
        ModShops.invalidate();
        if (event.getPlayer() != null) {
            syncData(event.getPlayer());
        } else {
            event.getPlayerList().getPlayers().forEach(player -> {
                syncData(player);
            });
        }
    }
}
