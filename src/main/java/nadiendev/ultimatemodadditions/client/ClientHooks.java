package nadiendev.ultimatemodadditions.client;

import nadiendev.ultimatemodadditions.client.screen.DailyConfigScreen;
import nadiendev.ultimatemodadditions.client.screen.DailyScreen;
import nadiendev.ultimatemodadditions.client.screen.JobsScreen;
import nadiendev.ultimatemodadditions.client.screen.RentScreen;
import nadiendev.ultimatemodadditions.client.screen.ShopScreen;
import nadiendev.ultimatemodadditions.client.screen.StandScreen;
import nadiendev.ultimatemodadditions.network.payload.DailyConfigPayload;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload;
import nadiendev.ultimatemodadditions.network.payload.JobsSyncPayload;
import nadiendev.ultimatemodadditions.network.payload.RentSyncPayload;
import nadiendev.ultimatemodadditions.network.payload.ShopSyncPayload;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class ClientHooks {

    private static JobsSyncPayload jobs = new JobsSyncPayload(false, 0, List.of());
    private static RentSyncPayload rent;
    private static DailyConfigPayload dailyConfig;
    private static DailySyncPayload daily = new DailySyncPayload(false, 0, List.of());
    private static ShopSyncPayload shop = new ShopSyncPayload(false, false, false, false, false, false, "0", 0, 0.0D, List.of());

    private ClientHooks() {
    }

    public static JobsSyncPayload jobs() {
        return jobs;
    }

    public static DailyConfigPayload dailyConfig() {
        return dailyConfig;
    }

    public static void acceptDailyConfig(DailyConfigPayload payload) {
        dailyConfig = payload;
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.open()) {
            minecraft.setScreen(new DailyConfigScreen());
        } else if (minecraft.screen instanceof DailyConfigScreen screen) {
            screen.refresh();
        }
    }

    public static DailySyncPayload daily() {
        return daily;
    }

    public static void acceptDaily(DailySyncPayload payload) {
        daily = payload;
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.open()) {
            minecraft.setScreen(new DailyScreen());
        } else if (minecraft.screen instanceof DailyScreen screen) {
            screen.refresh();
        }
    }

    public static ShopSyncPayload shop() {
        return shop;
    }

    public static void acceptShop(ShopSyncPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean keepCategories = !payload.open() && !payload.reload();
        shop = keepCategories
                ? new ShopSyncPayload(false, false, false, false, payload.canAdd(), payload.canRemove(), payload.balance(),
                payload.deliveries(), payload.commission(), shop.categories())
                : payload;
        if (payload.open()) {
            minecraft.setScreen(payload.stand() ? new StandScreen(payload.tiendita()) : new ShopScreen());
        } else if (minecraft.screen instanceof ShopScreen screen) {
            screen.refresh(payload.reload());
        } else if (minecraft.screen instanceof StandScreen screen) {
            screen.refresh(payload.reload());
        }
    }

    public static RentSyncPayload rent() {
        return rent;
    }

    public static void acceptJobs(JobsSyncPayload payload) {
        jobs = payload;
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.open()) {
            minecraft.setScreen(new JobsScreen());
        } else if (minecraft.screen instanceof JobsScreen screen) {
            screen.refresh();
        }
    }

    public static void acceptRent(RentSyncPayload payload) {
        rent = payload;
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.open()) {
            minecraft.setScreen(new RentScreen());
        } else if (minecraft.screen instanceof RentScreen screen) {
            screen.refresh();
        }
    }
}
