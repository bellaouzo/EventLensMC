package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.ModCorrelationBridge;
import dev.bellaouzo.eventlens.modcommon.port.ModCorrelationChannelPort;
import net.minecraft.client.Minecraft;

final class ForgeCorrelationChannel implements ModCorrelationChannelPort {

    private ModCorrelationBridge bridge;

    void bind(ModCorrelationBridge nextBridge) {
        this.bridge = nextBridge;
    }

    @Override
    public void sendHello(String clientSessionId, long sequence, String correlationKey) {
        if (Minecraft.getInstance().getConnection() == null
                || bridge == null
                || clientSessionId.isBlank()
                || correlationKey.isBlank()
                || sequence < 0L) {
            return;
        }
    }
}
