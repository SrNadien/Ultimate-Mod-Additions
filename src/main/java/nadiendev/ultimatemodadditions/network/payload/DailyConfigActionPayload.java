package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import nadiendev.ultimatemodadditions.data.DailyReward;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DailyConfigActionPayload(Action action, DailyReward.Period period, int index, double first,
                                       double second, double third, double fourth, int hour, boolean flagOne,
                                       boolean flagTwo) implements CustomPacketPayload {

    public enum Action {
        SETTINGS,
        ADD_ENTRY,
        SET_MONEY,
        ADD_HAND_ITEM,
        CLEAR_ITEMS,
        REMOVE_ENTRY,
        RESTORE
    }

    public static final Type<DailyConfigActionPayload> TYPE = new Type<>(UMA.id("daily_config_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DailyConfigActionPayload> STREAM_CODEC =
            StreamCodec.of(DailyConfigActionPayload::write, DailyConfigActionPayload::read);

    public static DailyConfigActionPayload entry(Action action, DailyReward.Period period, int index, double money) {
        return new DailyConfigActionPayload(action, period, index, money, 0.0D, 0.0D, 0.0D, 0, false, false);
    }

    public static DailyConfigActionPayload settings(boolean modBonus, double chance, double daily, double weekly,
                                                    double monthly, int hour, boolean notify) {
        return new DailyConfigActionPayload(Action.SETTINGS, DailyReward.Period.DAILY, 0, chance, daily, weekly,
                monthly, hour, modBonus, notify);
    }

    private static void write(RegistryFriendlyByteBuf buffer, DailyConfigActionPayload payload) {
        buffer.writeEnum(payload.action);
        buffer.writeEnum(payload.period);
        buffer.writeVarInt(payload.index);
        buffer.writeDouble(payload.first);
        buffer.writeDouble(payload.second);
        buffer.writeDouble(payload.third);
        buffer.writeDouble(payload.fourth);
        buffer.writeVarInt(payload.hour);
        buffer.writeBoolean(payload.flagOne);
        buffer.writeBoolean(payload.flagTwo);
    }

    private static DailyConfigActionPayload read(RegistryFriendlyByteBuf buffer) {
        return new DailyConfigActionPayload(buffer.readEnum(Action.class), buffer.readEnum(DailyReward.Period.class),
                buffer.readVarInt(), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
