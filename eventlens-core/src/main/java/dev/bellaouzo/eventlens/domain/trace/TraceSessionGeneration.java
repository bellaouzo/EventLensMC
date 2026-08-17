package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;

public record TraceSessionGeneration(
        int generation, boolean current, TraceSessionSummary summary, List<TraceDispatchRecord> records) {

    public int runNumber() {
        return generation + 1;
    }
}
