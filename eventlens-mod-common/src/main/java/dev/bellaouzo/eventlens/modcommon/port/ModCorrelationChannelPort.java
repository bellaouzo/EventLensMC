package dev.bellaouzo.eventlens.modcommon.port;

public interface ModCorrelationChannelPort {

    void sendHello(String clientSessionId, long sequence, String correlationKey);

    static ModCorrelationChannelPort noop() {
        return (clientSessionId, sequence, correlationKey) -> {};
    }
}
