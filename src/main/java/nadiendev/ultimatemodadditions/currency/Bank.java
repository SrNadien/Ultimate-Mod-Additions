package nadiendev.ultimatemodadditions.currency;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Bank extends SavedData {

    private static final String NAME = "ultimatemodadditions_bank";
    private static final String ACCOUNTS = "accounts";

    private final Map<UUID, BigInteger> balances = new HashMap<>();

    public static Bank get(MinecraftServer server) {
        return server.overworld().getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(Bank::new, Bank::load, null), NAME);
    }

    private static Bank load(CompoundTag tag, HolderLookup.Provider registries) {
        Bank bank = new Bank();
        ListTag accounts = tag.getList(ACCOUNTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < accounts.size(); i++) {
            CompoundTag account = accounts.getCompound(i);
            bank.balances.put(NbtUtils.loadUUID(account.get("id")), Wealth.parse(account.getString("balance")));
        }
        return bank;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag accounts = new ListTag();
        balances.forEach((id, balance) -> {
            if (balance.signum() > 0) {
                CompoundTag account = new CompoundTag();
                account.put("id", NbtUtils.createUUID(id));
                account.putString("balance", balance.toString());
                accounts.add(account);
            }
        });
        tag.put(ACCOUNTS, accounts);
        return tag;
    }

    public BigInteger balance(UUID id) {
        return balances.getOrDefault(id, BigInteger.ZERO);
    }

    public BigInteger deposit(UUID id, BigInteger cents) {
        BigInteger current = balance(id);
        BigInteger room = Wealth.bankCapacity().subtract(current).max(BigInteger.ZERO);
        BigInteger added = cents.max(BigInteger.ZERO).min(room);
        balances.put(id, current.add(added));
        setDirty();
        return added;
    }

    public BigInteger withdraw(UUID id, BigInteger cents) {
        BigInteger current = balance(id);
        BigInteger taken = cents.max(BigInteger.ZERO).min(current);
        balances.put(id, current.subtract(taken));
        setDirty();
        return taken;
    }

    public void set(UUID id, BigInteger cents) {
        balances.put(id, cents.max(BigInteger.ZERO).min(Wealth.bankCapacity()));
        setDirty();
    }
}
