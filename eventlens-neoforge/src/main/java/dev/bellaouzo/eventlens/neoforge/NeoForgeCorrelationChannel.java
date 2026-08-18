package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.domain.correlation.CorrelationChannelCodec;
import dev.bellaouzo.eventlens.modcommon.ModCorrelationBridge;
import dev.bellaouzo.eventlens.modcommon.port.ModCorrelationChannelPort;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

final class NeoForgeCorrelationChannel implements ModCorrelationChannelPort {

    private ModCorrelationBridge bridge;

    void bind(ModCorrelationBridge nextBridge) {
        this.bridge = nextBridge;
    }

    @Override
    public void sendHello(String clientSessionId, long sequence, String correlationKey) {
        if (Minecraft.getInstance().getConnection() == null) {
            return;
        }
        try {
            PacketDistributor.sendToServer(new NeoForgeCorrelationPayload(
                    CorrelationChannelCodec.hello(clientSessionId, sequence, correlationKey)));
        } catch (RuntimeException ignored) {
            // Server is not running EventLens Paper; fail closed.
        }
    }

    void receive(NeoForgeCorrelationPayload payload) {
        if (bridge != null) {
            bridge.receiveReply(payload.data());
        }
    }

    static void register(net.neoforged.bus.api.IEventBus modBus, NeoForgeCorrelationChannel channel) {
        modBus.addListener((net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) -> event.registrar(
                        "1")
                .optional()
                .playToClient(
                        NeoForgeCorrelationPayload.TYPE,
                        NeoForgeCorrelationPayload.STREAM_CODEC,
                        (payload, context) -> channel.receive(payload))
                .playToServer(
                        NeoForgeCorrelationPayload.TYPE,
                        NeoForgeCorrelationPayload.STREAM_CODEC,
                        (payload, context) -> {}));
    }
}
