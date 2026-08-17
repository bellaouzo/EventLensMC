package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceLimits;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionExportBundle;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionGeneration;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class TraceSessionArchives {

    private final Map<String, List<ArchivedRun>> runs = new LinkedHashMap<>();

    void archive(TraceSession session, boolean agentAttached) {
        List<ArchivedRun> list = runs.computeIfAbsent(session.getSessionId(), id -> new ArrayList<>());
        list.add(new ArchivedRun(
                session.getRestartCount(), session.toSummary(agentAttached), session.getRecordsSnapshot()));
        while (list.size() > TraceLimits.MAX_RESTART_ARCHIVES) {
            list.removeFirst();
        }
    }

    void clear() {
        runs.clear();
    }

    List<TraceSessionGeneration> listWithCurrent(
            Map<String, TraceSession> sessions, String sessionId, boolean agentAttached) {
        TraceSession session = sessions.get(sessionId);
        if (session == null) {
            return List.of();
        }
        List<TraceSessionGeneration> generations = new ArrayList<>();
        for (ArchivedRun run : runs.getOrDefault(sessionId, List.of())) {
            generations.add(new TraceSessionGeneration(run.generation(), false, run.summary(), run.records()));
        }
        generations.add(new TraceSessionGeneration(
                session.getRestartCount(), true, session.toSummary(agentAttached), session.getRecordsSnapshot()));
        return generations;
    }

    Optional<TraceSessionDetail> detail(
            Map<String, TraceSession> sessions, String sessionId, Optional<Integer> generation, boolean agentAttached) {
        TraceSession session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        if (generation.isEmpty() || generation.orElseThrow() == session.getRestartCount()) {
            return Optional.of(new TraceSessionDetail(session.toSummary(agentAttached), session.getRecordsSnapshot()));
        }
        int requested = generation.orElseThrow();
        for (ArchivedRun run : runs.getOrDefault(sessionId, List.of())) {
            if (run.generation() == requested) {
                return Optional.of(new TraceSessionDetail(run.summary(), run.records()));
            }
        }
        return Optional.empty();
    }

    Optional<TraceSessionExportBundle> exportBundle(
            Map<String, TraceSession> sessions, String sessionId, Optional<Integer> generation, boolean agentAttached) {
        TraceSession session = sessions.get(sessionId);
        Optional<TraceSessionDetail> detail = detail(sessions, sessionId, generation, agentAttached);
        if (session == null || detail.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new TraceSessionExportBundle(
                detail.orElseThrow().summary(),
                session.getConfig(),
                detail.orElseThrow().records()));
    }

    private record ArchivedRun(int generation, TraceSessionSummary summary, List<TraceDispatchRecord> records) {}
}
