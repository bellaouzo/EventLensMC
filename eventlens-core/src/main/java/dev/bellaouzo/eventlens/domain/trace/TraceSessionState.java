package dev.bellaouzo.eventlens.domain.trace;

public enum TraceSessionState {
    ACTIVE,
    THROTTLED,
    PAUSED,
    STOPPED,
    EXPIRED,
    FULL,
    ABANDONED;

    public boolean isTerminal() {
        return this == STOPPED || this == EXPIRED || this == FULL || this == ABANDONED;
    }
}
