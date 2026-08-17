package dev.bellaouzo.eventlens.paper.instrumentation;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;

public final class NoOpInstrumentationAdapter implements InstrumentationPort {

    @Override
    public boolean isAgentPresent() {
        return false;
    }

    @Override
    public int protocolVersion() {
        return 0;
    }

    @Override
    public boolean isProtocolCompatible() {
        return true;
    }

    @Override
    public boolean listenerSnapshotsEnabled() {
        return false;
    }

    @Override
    public void refreshObservationState(boolean tracingEnabled, long slowThresholdNanos, boolean captureStacks) {
        // no agent
    }

    @Override
    public void clearObservationState() {
        // no agent
    }
}
