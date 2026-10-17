package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MoneyActionPayload(Target target, Action action, String cents) implements CustomPacketPayload {

    public enum Target {
        WALLET,
        ATM
    }

    public enum Action {
        DEPOSIT_ALL,
        WITHDRAW,
        REQUEST_CARD,
        WITHDRAW_ALL
    }

    public static final Type<MoneyActionPayload> TYPE = new Type<>(UMA.id("money_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MoneyActionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(index -> Target.values()[index], Target::ordinal), MoneyActionPayload::target,
            ByteBufCodecs.idMapper(index -> Action.values()[index], Action::ordinal), MoneyActionPayload::action,
            ByteBufCodecs.STRING_UTF8, MoneyActionPayload::cents,
            MoneyActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
