package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;

public final class ModNoOpInstrumentationAdapter implements InstrumentationPort {

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
        return false;
    }

    @Override
    public boolean listenerSnapshotsEnabled() {
        return false;
    }

    @Override
    public void refreshObservationState(boolean tracingEnabled, long slowThresholdNanos, boolean captureStacks) {}

    @Override
    public void clearObservationState() {}
}
