package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.DispatchObservationRegistry;
import dev.bellaouzo.eventlens.observability.ListenerObservation;
import dev.bellaouzo.eventlens.observability.ListenerSnapshotBridge;
import dev.bellaouzo.eventlens.observability.ObservationGate;
import dev.bellaouzo.eventlens.observability.ProtocolVersion;
import java.util.List;

public final class ModAgentInstrumentationAdapter implements InstrumentationPort, ModListenerTimingSource {

    private final ModListenerTimingRecordFactory timingRecordFactory;

    public static ModAgentInstrumentationAdapter createAndRegister(
            ListenerSnapshotBridge snapshotBridge, AgentRuntime.OwnerIdResolver ownerIdResolver) {
        AgentRuntime.setListenerSnapshotBridge(snapshotBridge);
        AgentRuntime.setOwnerIdResolver(ownerIdResolver);
        return new ModAgentInstrumentationAdapter();
    }

    public ModAgentInstrumentationAdapter() {
        this(new ModListenerTimingRecordFactory());
    }

    ModAgentInstrumentationAdapter(ModListenerTimingRecordFactory timingRecordFactory) {
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

    @Override
    public List<ListenerTimingRecord> consume(long observationKey, long slowThresholdNanos) {
        List<ListenerObservation> observations = DispatchObservationRegistry.finishDispatch(observationKey);
        return observations.stream()
                .map(observation -> timingRecordFactory.fromObservation(observation, slowThresholdNanos))
                .toList();
    }
}
