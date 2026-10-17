package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.DailyReward;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record DailySyncPayload(boolean open, int streak, List<PeriodView> periods) implements CustomPacketPayload {

    public record EntryView(double money, List<ItemStack> items) {
    }

    public record PeriodView(DailyReward.Period period, boolean available, long secondsLeft, List<EntryView> entries) {
    }

    public static final Type<DailySyncPayload> TYPE = new Type<>(UMA.id("daily_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DailySyncPayload> STREAM_CODEC =
            StreamCodec.of(DailySyncPayload::write, DailySyncPayload::read);

    private static void write(RegistryFriendlyByteBuf buffer, DailySyncPayload payload) {
        buffer.writeBoolean(payload.open);
        buffer.writeVarInt(payload.streak);
        buffer.writeVarInt(payload.periods.size());
        for (PeriodView view : payload.periods) {
            buffer.writeEnum(view.period);
            buffer.writeBoolean(view.available);
            buffer.writeVarLong(view.secondsLeft);
            buffer.writeVarInt(view.entries.size());
            for (EntryView entry : view.entries) {
                buffer.writeDouble(entry.money);
                buffer.writeVarInt(entry.items.size());
                for (ItemStack stack : entry.items) {
                    ItemStack.STREAM_CODEC.encode(buffer, stack);
                }
            }
        }
    }

    private static DailySyncPayload read(RegistryFriendlyByteBuf buffer) {
        boolean open = buffer.readBoolean();
        int streak = buffer.readVarInt();
        int count = buffer.readVarInt();
        List<PeriodView> periods = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            DailyReward.Period period = buffer.readEnum(DailyReward.Period.class);
            boolean available = buffer.readBoolean();
            long seconds = buffer.readVarLong();
            int entryCount = buffer.readVarInt();
            List<EntryView> entries = new ArrayList<>(entryCount);
            for (int j = 0; j < entryCount; j++) {
                double money = buffer.readDouble();
                int itemCount = buffer.readVarInt();
                List<ItemStack> items = new ArrayList<>(itemCount);
                for (int k = 0; k < itemCount; k++) {
                    items.add(ItemStack.STREAM_CODEC.decode(buffer));
                }
                entries.add(new EntryView(money, items));
            }
            periods.add(new PeriodView(period, available, seconds, entries));
        }
        return new DailySyncPayload(open, streak, periods);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
