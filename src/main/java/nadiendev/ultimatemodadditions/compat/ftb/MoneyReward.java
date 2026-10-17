package nadiendev.ultimatemodadditions.compat.ftb;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.net.NotifyRewardMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.config.UMAConfig.PayTarget;
import nadiendev.ultimatemodadditions.currency.Money;
import nadiendev.ultimatemodadditions.currency.Payout;
import nadiendev.ultimatemodadditions.currency.Wealth;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigInteger;

public class MoneyReward extends Reward {

    private static final long DEFAULT_AMOUNT = 100L;

    private long amount = DEFAULT_AMOUNT;
    private boolean toAccount = true;

    public MoneyReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return FtbQuestsModule.money;
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putLong("amount", amount);
        nbt.putBoolean("to_account", toAccount);
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        amount = nbt.contains("amount") ? nbt.getLong("amount") : DEFAULT_AMOUNT;
        toAccount = !nbt.contains("to_account") || nbt.getBoolean("to_account");
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarLong(amount);
        buffer.writeBoolean(toAccount);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        amount = buffer.readVarLong();
        toAccount = buffer.readBoolean();
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addLong("amount", amount, value -> amount = value, DEFAULT_AMOUNT, 1L, Long.MAX_VALUE / 200L)
                .setNameKey("ftbquests.reward.ultimatemodadditions.money");
        config.addBool("to_account", toAccount, value -> toAccount = value, true)
                .setNameKey("ftbquests.reward.ultimatemodadditions.money.to_account");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        BigInteger cents = BigInteger.valueOf(Money.toCents(amount));
        BigInteger given = Payout.give(player, cents, toAccount ? PayTarget.ACCOUNT : PayTarget.CASH);
        if (given.signum() == 0) {
            return;
        }
        if (notify) {
            Component text = Component.translatable("ftbquests.reward.ultimatemodadditions.money").append(": ")
                    .append(Component.empty().append("+").append(Money.format(given)).withStyle(ChatFormatting.GREEN));
            NetworkManager.sendToPlayer(player, new NotifyRewardMessage(id, text,
                    Icon.getIcon(UMA.MODID + ":item/20_lucas"), disableRewardScreenBlur));
        }
    }

    @Override
    public MutableComponent getAltTitle() {
        return Component.translatable("ftbquests.reward.ultimatemodadditions.money").append(": ")
                .append(Component.empty().append("+").append(Money.format(Wealth.centsOfMangos(BigInteger.valueOf(amount))))
                        .withStyle(ChatFormatting.GREEN));
    }

    @Override
    public String getButtonText() {
        return "+" + Money.group(BigInteger.valueOf(amount));
    }
}
