package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.DailyReward;
import nadiendev.ultimatemodadditions.network.payload.DailySyncPayload.EntryView;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record DailyConfigPayload(boolean open, boolean modBonus, double chance, double budgetDaily,
                                 double budgetWeekly, double budgetMonthly, int resetHour, boolean loginNotice,
                                 List<ConfigPeriod> periods) implements CustomPacketPayload {

    public record ConfigPeriod(DailyReward.Period period, List<EntryView> entries) {
    }

    public static final Type<DailyConfigPayload> TYPE = new Type<>(UMA.id("daily_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DailyConfigPayload> STREAM_CODEC =
            StreamCodec.of(DailyConfigPayload::write, DailyConfigPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, DailyConfigPayload payload) {
        buffer.writeBoolean(payload.open);
        buffer.writeBoolean(payload.modBonus);
        buffer.writeDouble(payload.chance);
        buffer.writeDouble(payload.budgetDaily);
        buffer.writeDouble(payload.budgetWeekly);
        buffer.writeDouble(payload.budgetMonthly);
        buffer.writeVarInt(payload.resetHour);
        buffer.writeBoolean(payload.loginNotice);
        buffer.writeVarInt(payload.periods.size());
        for (ConfigPeriod period : payload.periods) {
            buffer.writeEnum(period.period);
            buffer.writeVarInt(period.entries.size());
            for (EntryView entry : period.entries) {
                buffer.writeDouble(entry.money());
                buffer.writeVarInt(entry.items().size());
                for (ItemStack stack : entry.items()) {
                    ItemStack.STREAM_CODEC.encode(buffer, stack);
                }
            }
        }
    }

    private static DailyConfigPayload read(RegistryFriendlyByteBuf buffer) {
        boolean open = buffer.readBoolean();
        boolean modBonus = buffer.readBoolean();
        double chance = buffer.readDouble();
        double daily = buffer.readDouble();
        double weekly = buffer.readDouble();
        double monthly = buffer.readDouble();
        int hour = buffer.readVarInt();
        boolean loginNotice = buffer.readBoolean();
        int count = buffer.readVarInt();
        List<ConfigPeriod> periods = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DailyReward.Period period = buffer.readEnum(DailyReward.Period.class);
            int size = buffer.readVarInt();
            List<EntryView> entries = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                double money = buffer.readDouble();
                int itemCount = buffer.readVarInt();
                List<ItemStack> items = new ArrayList<>(itemCount);
                for (int k = 0; k < itemCount; k++) {
                    items.add(ItemStack.STREAM_CODEC.decode(buffer));
                }
                entries.add(new EntryView(money, items));
            }
            periods.add(new ConfigPeriod(period, entries));
        }
        return new DailyConfigPayload(open, modBonus, chance, daily, weekly, monthly, hour, loginNotice, periods);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
