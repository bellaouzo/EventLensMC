package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.EventSnapshotRegistryPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionDetail;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.domain.trace.TraceStartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceStopResult;
import dev.bellaouzo.eventlens.domain.trace.TraceViewResult;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
import java.util.Optional;

public final class TraceCommandService {

    public static final int VIEW_PAGE_SIZE = 6;
    private static final EventSnapshotRegistryPort DEFAULT_EVENT_SNAPSHOT_REGISTRY = new DefaultEventSnapshotRegistry();

    private final TraceSessionManager traceSessionManager;
    private final ListenerRegistryPort listenerRegistryPort;
    private final TraceHookPort traceHookPort;
    private final EventLensCommandConfig commandConfig;
    private final EventSnapshotRegistryPort eventSnapshotRegistryPort;

    public TraceCommandService(
            TraceSessionManager traceSessionManager,
            ListenerRegistryPort listenerRegistryPort,
            TraceHookPort traceHookPort) {
        this(
                traceSessionManager,
                listenerRegistryPort,
                traceHookPort,
                EventLensCommandConfig.defaults(),
                DEFAULT_EVENT_SNAPSHOT_REGISTRY);
    }

    public TraceCommandService(
            TraceSessionManager traceSessionManager,
            ListenerRegistryPort listenerRegistryPort,
            TraceHookPort traceHookPort,
            EventLensCommandConfig commandConfig) {
        this(traceSessionManager, listenerRegistryPort, traceHookPort, commandConfig, DEFAULT_EVENT_SNAPSHOT_REGISTRY);
    }

    public TraceCommandService(
            TraceSessionManager traceSessionManager,
            ListenerRegistryPort listenerRegistryPort,
            TraceHookPort traceHookPort,
            EventLensCommandConfig commandConfig,
            EventSnapshotRegistryPort eventSnapshotRegistryPort) {
        this.traceSessionManager = traceSessionManager;
        this.listenerRegistryPort = listenerRegistryPort;
        this.traceHookPort = traceHookPort;
        this.commandConfig = commandConfig;
        this.eventSnapshotRegistryPort = eventSnapshotRegistryPort;
    }

    public EventLensCommandConfig commandConfig() {
        return commandConfig;
    }

    public TraceStartResult startTrace(String eventQuery, String ownerName, TraceStartOptions options) {
        return TraceStartExecutor.start(
                new TraceStartExecutor.StartContext(
                        traceSessionManager,
                        listenerRegistryPort,
                        traceHookPort,
                        eventSnapshotRegistryPort,
                        commandConfig,
                        options),
                eventQuery,
                ownerName);
    }

    public TraceStopResult stopTrace(String ownerName) {
        List<String> stopped = traceSessionManager.stopSessionsForOwner(ownerName, System.currentTimeMillis());
        refreshHooks();
        if (stopped.isEmpty()) {
            return new TraceStopResult.NoActiveSessions();
        }
        return new TraceStopResult.Success(stopped);
    }

    public TraceStopResult stopSession(String sessionId) {
        Optional<String> stopped = traceSessionManager.stopSession(sessionId, System.currentTimeMillis());
        refreshHooks();
        return stopped.<TraceStopResult>map(id -> new TraceStopResult.Success(List.of(id)))
                .orElseGet(() -> new TraceStopResult.NotFound(sessionId));
    }

    public TraceRestartResult restartTrace(String sessionId) {
        return TraceRestartService.restart(traceSessionManager, traceHookPort, sessionId);
    }

    public List<TraceSessionSummary> listSessions() {
        traceSessionManager.expireSessions(System.currentTimeMillis());
        return traceSessionManager.listSessions();
    }

    public TraceViewResult viewSession(
            String sessionId, int page, boolean includeUnchanged, DispatchViewFilter filter) {
        return viewSession(sessionId, page, includeUnchanged, filter, Optional.empty());
    }

    public TraceViewResult viewSession(
            String sessionId,
            int page,
            boolean includeUnchanged,
            DispatchViewFilter filter,
            Optional<Integer> generation) {
        traceSessionManager.expireSessions(System.currentTimeMillis());
        Optional<TraceSessionDetail> detail = traceSessionManager.getSessionDetail(sessionId, generation);
        if (detail.isEmpty()) {
            return new TraceViewResult.NotFound(sessionId);
        }

        List<dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord> sessionRecords =
                detail.get().records();
        List<dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord> records = sessionRecords.stream()
                .filter(dispatchRecord ->
                        filter.matches(dispatchRecord, detail.get().summary().slowThresholdNanos()))
                .toList();
        int totalRecords = records.size();
        int totalPages = totalRecords == 0 ? 1 : (int) Math.ceil((double) totalRecords / VIEW_PAGE_SIZE);
        if (page < 1 || page > totalPages) {
            return new TraceViewResult.InvalidPage(page, totalPages);
        }

        int fromIndex = (page - 1) * VIEW_PAGE_SIZE;
        int toIndex = Math.min(fromIndex + VIEW_PAGE_SIZE, totalRecords);
        TraceSessionDetail pageDetail =
                new TraceSessionDetail(detail.get().summary(), records.subList(fromIndex, toIndex));
        return new TraceViewResult.Success(
                pageDetail, page, totalPages, includeUnchanged, filter, records.size(), sessionRecords.size());
    }

    public List<String> listSessionIds() {
        return listSessions().stream().map(TraceSessionSummary::sessionId).toList();
    }

    public List<String> listOpenSessionIds() {
        return listSessions().stream()
                .filter(session -> !session.state().isTerminal())
                .map(TraceSessionSummary::sessionId)
                .toList();
    }

    public List<String> listSupportedEventSimpleNames() {
        return eventSnapshotRegistryPort.supportedTraceEventSimpleNames();
    }

    public List<String> listDispatchSequenceTokens(String sessionId) {
        return TraceCommandLookups.sequenceTokens(traceSessionManager, sessionId);
    }

    public List<String> listDispatchPluginNames(String sessionId) {
        return TraceCommandLookups.pluginNames(traceSessionManager, sessionId);
    }

    public List<String> listPresetNames() {
        return commandConfig.presets().keySet().stream().sorted().toList();
    }

    public void expireSessions() {
        traceSessionManager.expireSessions(System.currentTimeMillis());
        refreshHooks();
    }

    private void refreshHooks() {
        traceHookPort.syncWithActiveSessions(traceSessionManager);
    }

    public record TraceStartOptions(
            TraceFilter filter,
            Optional<Long> maxDurationMillis,
            Optional<Integer> maxEventCount,
            long slowThresholdNanos,
            boolean captureStacks,
            boolean confirmHot,
            Optional<OutputDetailLevel> detailLevel,
            boolean genericAllow) {

        public TraceStartOptions(
                TraceFilter filter,
                Optional<Long> maxDurationMillis,
                Optional<Integer> maxEventCount,
                long slowThresholdNanos,
                boolean captureStacks,
                boolean confirmHot,
                Optional<OutputDetailLevel> detailLevel) {
            this(
                    filter,
                    maxDurationMillis,
                    maxEventCount,
                    slowThresholdNanos,
                    captureStacks,
                    confirmHot,
                    detailLevel,
                    false);
        }

        public static TraceStartOptions parse(List<String> tokens) {
            return TraceStartOptionsParser.parse(tokens);
        }

        public static TraceStartOptions parse(List<String> tokens, EventLensCommandConfig commandConfig) {
            return TraceStartOptionsParser.parse(tokens, commandConfig);
        }

        public OutputDetailLevel resolvedDetailLevel(EventLensCommandConfig config) {
            return detailLevel.orElse(config.defaultDetailLevel());
        }
    }
}
