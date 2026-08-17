package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.EventSnapshotRegistryPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchOutcome;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.observability.SamplingPolicy;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
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
        EventSearchResult search = listenerRegistryPort.searchEvents(eventQuery);
        if (search.outcome() == EventSearchOutcome.NOT_FOUND) {
            return new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.EVENT_NOT_FOUND, "No event matches \"" + eventQuery + "\".");
        }
        if (search.outcome() == EventSearchOutcome.AMBIGUOUS) {
            return new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.EVENT_AMBIGUOUS,
                    "Multiple events match that query: " + String.join(", ", search.candidateClassNames()));
        }

        if (!eventSnapshotRegistryPort.supportsTrace(search.resolvedEventClassName())) {
            return new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.UNSUPPORTED_EVENT,
                    SupportedEventTypes.displaySimpleName(search.resolvedEventClassName())
                            + " is not supported for tracing. Supported events: "
                            + SupportedEventTypes.formatSimpleNameList());
        }

        boolean hotEvent = SamplingPolicy.requiresNarrowingFilter(search.resolvedEventClassName());
        boolean narrowed = hasNarrowingFilter(options.filter());
        if (hotEvent && !narrowed) {
            return new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.INVALID_OPTIONS,
                    SamplingPolicy.hotEventDisplayName()
                            + " requires a narrowing filter (--plugin, --player, --world, or --region).");
        }

        if (hotEvent && commandConfig.requireHotEventConfirmation() && !options.confirmHot()) {
            String simpleName = SupportedEventTypes.displaySimpleName(search.resolvedEventClassName());
            return new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.HOT_EVENT_CONFIRMATION,
                    simpleName
                            + " is a hot event. Tracing it can affect server performance even with a narrowing filter.",
                    Optional.of(TraceStartConfirmCommands.hotEventConfirmCommand(simpleName, options)
                            .orElseThrow()));
        }

        if (options.captureStacks() && options.slowThresholdNanos() <= 0L) {
            return new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.INVALID_OPTIONS,
                    "--capture-stacks requires a positive --slow-threshold.");
        }

        try {
            TraceSessionConfig config = new TraceSessionConfig(
                    search.resolvedEventClassName(),
                    options.filter(),
                    options.maxDurationMillis(),
                    options.maxEventCount(),
                    options.slowThresholdNanos(),
                    options.captureStacks());
            var sessionId = traceSessionManager.startSession(config, ownerName, System.currentTimeMillis());
            traceHookPort.registerHooksForEvent(config.eventClassName());
            traceHookPort.syncWithActiveSessions(traceSessionManager);
            return new TraceStartResult.Success(sessionId, config.eventClassName());
        } catch (IllegalStateException ex) {
            return new TraceStartResult.Failure(TraceStartResult.Failure.Reason.SESSION_LIMIT, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return new TraceStartResult.Failure(TraceStartResult.Failure.Reason.INVALID_OPTIONS, ex.getMessage());
        }
    }

    public TraceStopResult stopTrace(String ownerName) {
        List<String> stopped = traceSessionManager.stopSessionsForOwner(ownerName, System.currentTimeMillis());
        refreshHooks();
        if (stopped.isEmpty()) {
            return new TraceStopResult.NoActiveSessions();
        }
        return new TraceStopResult.Success(stopped);
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

    private static boolean hasNarrowingFilter(TraceFilter filter) {
        return filter.pluginName().isPresent()
                || filter.playerName().isPresent()
                || filter.worldName().isPresent()
                || filter.region().isPresent();
    }

    public record TraceStartOptions(
            TraceFilter filter,
            Optional<Long> maxDurationMillis,
            Optional<Integer> maxEventCount,
            long slowThresholdNanos,
            boolean captureStacks,
            boolean confirmHot,
            Optional<OutputDetailLevel> detailLevel) {

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
