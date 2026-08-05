package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;

public record TraceSessionDetail(TraceSessionSummary summary, List<TraceDispatchRecord> records) {}
