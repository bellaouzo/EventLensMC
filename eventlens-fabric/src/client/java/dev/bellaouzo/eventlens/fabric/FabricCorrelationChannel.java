package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.domain.correlation.CorrelationChannelCodec;
import dev.bellaouzo.eventlens.modcommon.ModCorrelationBridge;
import dev.bellaouzo.eventlens.modcommon.port.ModCorrelationChannelPort;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

final class FabricCorrelationChannel implements ModCorrelationChannelPort {

    private ModCorrelationBridge bridge;

    void bind(ModCorrelationBridge nextBridge) {
        this.bridge = nextBridge;
    }

    @Override
    public void sendHello(String clientSessionId, long sequence, String correlationKey) {
        if (!ClientPlayNetworking.canSend(FabricCorrelationPayload.TYPE)) {
            return;
        }
        ClientPlayNetworking.send(new FabricCorrelationPayload(
                CorrelationChannelCodec.hello(clientSessionId, sequence, correlationKey)));
    }

    static void register(FabricCorrelationChannel channel) {
        PayloadTypeRegistry.serverboundPlay().register(FabricCorrelationPayload.TYPE, FabricCorrelationPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FabricCorrelationPayload.TYPE, FabricCorrelationPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(FabricCorrelationPayload.TYPE, (payload, context) -> {
            if (channel.bridge != null) {
                channel.bridge.receiveReply(payload.data());
            }
        });
    }
}
