package dev.bellaouzo.eventlens.domain.trace;

public enum TraceSessionState {
    ACTIVE,
    THROTTLED,
    STOPPED,
    EXPIRED,
    FULL,
    ABANDONED
}
