package dev.bellaouzo.eventlens.paper.instrumentation;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.DispatchObservationRegistry;
import dev.bellaouzo.eventlens.observability.ListenerObservation;
import dev.bellaouzo.eventlens.observability.ObservationGate;
import dev.bellaouzo.eventlens.observability.ProtocolVersion;
import java.util.List;

public final class AgentInstrumentationAdapter implements InstrumentationPort {

    private final ListenerTimingRecordFactory timingRecordFactory;

    public AgentInstrumentationAdapter() {
        this(new ListenerTimingRecordFactory());
    }

    public static AgentInstrumentationAdapter createAndRegister() {
        AgentRuntime.setListenerSnapshotBridge(new PaperListenerSnapshotBridge());
        return new AgentInstrumentationAdapter();
    }

    AgentInstrumentationAdapter(ListenerTimingRecordFactory timingRecordFactory) {
        this.timingRecordFactory = timingRecordFactory;
    }

    @Override
    public boolean isAgentPresent() {
        return AgentRuntime.isAgentLoaded();
    }

    @Override
    public int protocolVersion() {
        return ProtocolVersion.CURRENT;
    }

    @Override
    public boolean isProtocolCompatible() {
        return ProtocolVersion.isCompatible(ProtocolVersion.CURRENT);
    }

    @Override
    public boolean listenerSnapshotsEnabled() {
        return AgentRuntime.listenerSnapshotsEnabled();
    }

    @Override
    public void refreshObservationState(boolean tracingEnabled, long slowThresholdNanos, boolean captureStacks) {
        ObservationGate.setEnabled(tracingEnabled);
        AgentRuntime.setSlowThresholdNanos(slowThresholdNanos);
        AgentRuntime.setCaptureStacks(captureStacks);
    }

    @Override
    public void clearObservationState() {
        ObservationGate.setEnabled(false);
        AgentRuntime.setCaptureStacks(false);
    }

    public List<ListenerTimingRecord> consumeListenerTimings(long dispatchKey, long slowThresholdNanos) {
        List<ListenerObservation> observations = DispatchObservationRegistry.finishDispatch(dispatchKey);
        return observations.stream()
                .map(observation -> timingRecordFactory.fromObservation(observation, slowThresholdNanos))
                .toList();
    }

    public void clearDispatch(long dispatchKey) {
        DispatchObservationRegistry.clear(dispatchKey);
    }
}
