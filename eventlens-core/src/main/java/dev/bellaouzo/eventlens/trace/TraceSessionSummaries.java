package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.application.SessionConflictAnalyzer;
import dev.bellaouzo.eventlens.application.SessionTimingAnalyzer;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;

final class TraceSessionSummaries {

    private TraceSessionSummaries() {}

    static TraceSessionSummary create(TraceSession session, boolean agentAttached) {
        List<TraceDispatchRecord> records = session.getRecordsSnapshot();
        return new TraceSessionSummary(
                session.getSessionId(),
                session.getEventClassName(),
                session.getState(),
                session.getOwnerName(),
                session.getStartedAtMillis(),
                session.getLastActivityAtMillis(),
                records.size(),
                session.droppedEvents(),
                session.sampledOutEvents(),
                session.getConfig().effectiveMaxEventCount(),
                session.getConfig().effectiveMaxDurationMillis(),
                session.getConfig().slowThresholdNanos(),
                session.getConfig().captureStacks(),
                SessionTimingAnalyzer.analyze(
                        records, session.sampledOutEvents(), session.getConfig().slowThresholdNanos(), agentAttached),
                SessionConflictAnalyzer.analyze(records, session.getConfig().slowThresholdNanos()),
                session.getRestartCount(),
                session.getEventClassNames());
    }
}
