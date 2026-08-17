package dev.bellaouzo.eventlens.domain.trace;

public enum TracePartialReason {
    SAMPLED,
    THROTTLED,
    RECORD_LIMIT,
    LISTENER_LIMIT,
    AGENT_ABSENT,
    LISTENER_SNAPSHOTS_UNAVAILABLE,
    INCOMPATIBLE_AGENT_PROTOCOL,
    PAPER_VERSION_MISMATCH
}
