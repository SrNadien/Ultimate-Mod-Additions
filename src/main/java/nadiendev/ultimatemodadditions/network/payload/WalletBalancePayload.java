package nadiendev.ultimatemodadditions.network.payload;

import nadiendev.ultimatemodadditions.UMA;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record WalletBalancePayload(String cents) implements CustomPacketPayload {

    public static final Type<WalletBalancePayload> TYPE = new Type<>(UMA.id("wallet_balance"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WalletBalancePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, WalletBalancePayload::cents,
                    WalletBalancePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
