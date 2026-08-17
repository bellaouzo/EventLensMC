package dev.bellaouzo.eventlens.application.port;

public interface InstrumentationPort {

    boolean isAgentPresent();

    int protocolVersion();

    boolean isProtocolCompatible();

    boolean listenerSnapshotsEnabled();

    void refreshObservationState(boolean tracingEnabled, long slowThresholdNanos, boolean captureStacks);

    void clearObservationState();
}
