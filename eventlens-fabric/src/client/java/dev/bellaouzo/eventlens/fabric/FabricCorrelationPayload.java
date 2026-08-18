package dev.bellaouzo.eventlens.fabric;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FabricCorrelationPayload(byte[] data) implements CustomPacketPayload {

    public static final Type<FabricCorrelationPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("eventlens", "correlate"));

    public static final StreamCodec<FriendlyByteBuf, FabricCorrelationPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeByteArray(payload.data()),
            buffer -> new FabricCorrelationPayload(buffer.readByteArray()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
