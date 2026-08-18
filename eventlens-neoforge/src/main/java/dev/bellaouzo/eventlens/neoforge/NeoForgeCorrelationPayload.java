package dev.bellaouzo.eventlens.neoforge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NeoForgeCorrelationPayload(byte[] data) implements CustomPacketPayload {

    public static final Type<NeoForgeCorrelationPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("eventlens", "correlate"));

    public static final StreamCodec<FriendlyByteBuf, NeoForgeCorrelationPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeByteArray(payload.data()),
            buffer -> new NeoForgeCorrelationPayload(buffer.readByteArray()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
