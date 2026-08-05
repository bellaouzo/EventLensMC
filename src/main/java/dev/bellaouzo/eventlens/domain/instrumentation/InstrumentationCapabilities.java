package dev.bellaouzo.eventlens.domain.instrumentation;

public record InstrumentationCapabilities(
        boolean perListenerDuration,
        boolean perListenerSnapshots,
        boolean perListenerPropertyDiffs,
        boolean exactCancellationTimeline,
        boolean priorityBandFallback,
        boolean exceptionPreservation) {

    public static InstrumentationCapabilities precise() {
        return new InstrumentationCapabilities(true, true, true, true, true, true);
    }

    public static InstrumentationCapabilities dispatchOnly() {
        return new InstrumentationCapabilities(false, false, false, false, true, true);
    }

    public static InstrumentationCapabilities degraded(boolean snapshotsAvailable) {
        return new InstrumentationCapabilities(
                true, snapshotsAvailable, snapshotsAvailable, snapshotsAvailable, true, true);
    }
}
