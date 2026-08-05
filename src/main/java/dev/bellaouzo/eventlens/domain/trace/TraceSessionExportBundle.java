package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;

public record TraceSessionExportBundle(
        TraceSessionSummary summary, TraceSessionConfig config, List<TraceDispatchRecord> records) {

    public TraceSessionExportBundle {
        records = records == null ? List.of() : List.copyOf(records);
    }
}
