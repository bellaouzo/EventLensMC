package dev.bellaouzo.eventlens.domain.report;

import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TraceReportWarnings {

    private TraceReportWarnings() {}

    public static List<String> collect(TraceReportDocument document) {
        List<String> warnings = new ArrayList<>();
        TraceSessionSummary summary = document.summary();

        if (summary.droppedEvents() > 0) {
            warnings.add(summary.droppedEvents() + " dispatches dropped (session record limit).");
        }
        if (summary.sampledOutEvents() > 0) {
            warnings.add(summary.sampledOutEvents() + " dispatches sampled out (hot-event sampling).");
        }
        if ("THROTTLED".equals(summary.state().name())) {
            warnings.add("Session throttled due to EventLens overhead budget.");
        }

        SessionTimingSummary timing = summary.timingSummary();
        Set<TracePartialReason> sessionReasons = Set.of();
        if (timing != null) {
            sessionReasons = timing.sessionPartialReasons();
            for (TracePartialReason reason : sessionReasons) {
                warnings.add(describePartialReason(reason));
            }
        }

        for (TraceDispatchRecord dispatch : document.dispatches()) {
            for (TracePartialReason reason : dispatch.partialReasons()) {
                if (sessionReasons.contains(reason)) {
                    continue;
                }
                warnings.add("Dispatch #" + dispatch.sequence() + ": " + describePartialReason(reason));
            }
        }

        return warnings.stream().distinct().toList();
    }

    public static String describePartialReason(TracePartialReason reason) {
        return switch (reason) {
            case SAMPLED -> "Incomplete: dispatch sampled.";
            case THROTTLED -> "Incomplete: capture throttled.";
            case RECORD_LIMIT -> "Incomplete: listener record limit reached.";
            case LISTENER_LIMIT -> "Incomplete: listener chain truncated.";
            case AGENT_ABSENT -> "Incomplete: Java agent absent; per-listener timing unavailable.";
            case LISTENER_SNAPSHOTS_UNAVAILABLE ->
                "Incomplete: per-listener before/after snapshots unavailable; using priority-band fallback.";
            case INCOMPATIBLE_AGENT_PROTOCOL -> "Incomplete: Java agent protocol incompatible with plugin build.";
            case PAPER_VERSION_MISMATCH -> "Warning: server version differs from tested Paper build.";
        };
    }
}
