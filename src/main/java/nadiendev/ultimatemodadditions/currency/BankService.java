package nadiendev.ultimatemodadditions.currency;

import nadiendev.ultimatemodadditions.network.payload.BankSyncPayload;
import nadiendev.ultimatemodadditions.network.payload.WalletBalancePayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.math.BigInteger;
import java.util.UUID;

public final class BankService {

    private BankService() {
    }

    public static BigInteger balance(MinecraftServer server, UUID id) {
        return Bank.get(server).balance(id);
    }

    public static BigInteger deposit(MinecraftServer server, UUID id, BigInteger cents) {
        BigInteger added = Bank.get(server).deposit(id, cents);
        syncOwn(server, id);
        return added;
    }

    public static BigInteger withdraw(MinecraftServer server, UUID id, BigInteger cents) {
        BigInteger taken = Bank.get(server).withdraw(id, cents);
        syncOwn(server, id);
        return taken;
    }

    public static void set(MinecraftServer server, UUID id, BigInteger cents) {
        Bank.get(server).set(id, cents);
        syncOwn(server, id);
    }

    public static void syncOwn(MinecraftServer server, UUID id) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player != null) {
            sendOwn(player);
        }
    }

    public static void sendOwn(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new BankSyncPayload(true, balance(player.server, player.getUUID()).toString()));
    }

    public static void sendWallet(ServerPlayer player, BigInteger cents) {
        PacketDistributor.sendToPlayer(player, new WalletBalancePayload(cents.toString()));
    }

    public static void sendAtm(ServerPlayer viewer, UUID account) {
        BigInteger value = account == null ? BigInteger.ZERO : balance(viewer.server, account);
        PacketDistributor.sendToPlayer(viewer, new BankSyncPayload(false, value.toString()));
    }
}
