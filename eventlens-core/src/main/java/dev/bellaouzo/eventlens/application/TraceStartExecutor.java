package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.EventSnapshotRegistryPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.TraceHookPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchOutcome;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.observability.SamplingPolicy;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.domain.trace.TraceStartResult;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class TraceStartExecutor {

    private TraceStartExecutor() {}

    static TraceStartResult start(StartContext context, String eventQuery, String ownerName) {
        List<String> classNames = resolveClassNames(context, eventQuery);
        if (context.failure() != null) {
            return context.failure();
        }
        if (classNames.isEmpty()) {
            return notFound(eventQuery);
        }
        Optional<TraceStartResult.Failure> invalid = validate(context, classNames);
        if (invalid.isPresent()) {
            return invalid.get();
        }
        return commit(context, classNames, ownerName);
    }

    private static List<String> resolveClassNames(StartContext context, String eventQuery) {
        List<String> classNames = new ArrayList<>();
        for (String query : eventQuery.split(",", -1)) {
            String trimmed = query.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            Optional<String> className = resolveOne(context, trimmed);
            if (className.isEmpty()) {
                return List.of();
            }
            classNames.add(className.get());
        }
        return classNames;
    }

    private static Optional<String> resolveOne(StartContext context, String trimmed) {
        EventSearchResult search = context.listenerRegistryPort().searchEvents(trimmed);
        if (search.outcome() == EventSearchOutcome.NOT_FOUND) {
            context.failure(notFound(trimmed));
            return Optional.empty();
        }
        if (search.outcome() == EventSearchOutcome.AMBIGUOUS) {
            context.failure(new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.EVENT_AMBIGUOUS,
                    "Multiple events match that query: " + String.join(", ", search.candidateClassNames())));
            return Optional.empty();
        }
        String className = search.resolvedEventClassName();
        if (!context.snapshotRegistry().supportsTrace(className)
                && !context.options().genericAllow()) {
            context.failure(new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.UNSUPPORTED_EVENT,
                    SupportedEventTypes.displaySimpleName(className)
                            + " is not supported for tracing. Use --generic for common fields only. Supported: "
                            + SupportedEventTypes.formatSimpleNameList()));
            return Optional.empty();
        }
        return Optional.of(className);
    }

    private static Optional<TraceStartResult.Failure> validate(StartContext context, List<String> classNames) {
        boolean hotEvent = classNames.stream().anyMatch(SamplingPolicy::requiresNarrowingFilter);
        TraceCommandService.TraceStartOptions options = context.options();
        if (hotEvent && !SamplingPolicy.hasNarrowingFilter(options.filter())) {
            return Optional.of(new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.INVALID_OPTIONS,
                    SamplingPolicy.hotEventDisplayName()
                            + " requires a narrowing filter (--plugin, --player, --world, or --region)."));
        }
        if (hotEvent && context.commandConfig().requireHotEventConfirmation() && !options.confirmHot()) {
            String simpleName = SupportedEventTypes.displaySimpleName(classNames.getFirst());
            return Optional.of(new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.HOT_EVENT_CONFIRMATION,
                    simpleName
                            + " is a hot event. Tracing it can affect server performance even with a narrowing filter.",
                    Optional.of(TraceStartConfirmCommands.hotEventConfirmCommand(simpleName, options)
                            .orElseThrow())));
        }
        if (options.captureStacks() && options.slowThresholdNanos() <= 0L) {
            return Optional.of(new TraceStartResult.Failure(
                    TraceStartResult.Failure.Reason.INVALID_OPTIONS,
                    "--capture-stacks requires a positive --slow-threshold."));
        }
        return Optional.empty();
    }

    private static TraceStartResult commit(StartContext context, List<String> classNames, String ownerName) {
        TraceCommandService.TraceStartOptions options = context.options();
        try {
            TraceSessionConfig config = new TraceSessionConfig(
                    classNames,
                    options.filter(),
                    options.maxDurationMillis(),
                    options.maxEventCount(),
                    options.slowThresholdNanos(),
                    options.captureStacks());
            String sessionId = context.sessionManager().startSession(config, ownerName, System.currentTimeMillis());
            for (String className : classNames) {
                context.traceHookPort().registerHooksForEvent(className);
            }
            context.traceHookPort().syncWithActiveSessions(context.sessionManager());
            return new TraceStartResult.Success(sessionId, config.eventClassName(), classNames);
        } catch (IllegalStateException ex) {
            return new TraceStartResult.Failure(TraceStartResult.Failure.Reason.SESSION_LIMIT, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return new TraceStartResult.Failure(TraceStartResult.Failure.Reason.INVALID_OPTIONS, ex.getMessage());
        }
    }

    private static TraceStartResult.Failure notFound(String query) {
        return new TraceStartResult.Failure(
                TraceStartResult.Failure.Reason.EVENT_NOT_FOUND, "No event matches \"" + query + "\".");
    }

    static final class StartContext {
        private final TraceSessionManager sessionManager;
        private final ListenerRegistryPort listenerRegistryPort;
        private final TraceHookPort traceHookPort;
        private final EventSnapshotRegistryPort snapshotRegistry;
        private final EventLensCommandConfig commandConfig;
        private final TraceCommandService.TraceStartOptions options;
        private TraceStartResult.Failure failure;

        StartContext(
                TraceSessionManager sessionManager,
                ListenerRegistryPort listenerRegistryPort,
                TraceHookPort traceHookPort,
                EventSnapshotRegistryPort snapshotRegistry,
                EventLensCommandConfig commandConfig,
                TraceCommandService.TraceStartOptions options) {
            this.sessionManager = sessionManager;
            this.listenerRegistryPort = listenerRegistryPort;
            this.traceHookPort = traceHookPort;
            this.snapshotRegistry = snapshotRegistry;
            this.commandConfig = commandConfig;
            this.options = options;
        }

        TraceSessionManager sessionManager() {
            return sessionManager;
        }

        ListenerRegistryPort listenerRegistryPort() {
            return listenerRegistryPort;
        }

        TraceHookPort traceHookPort() {
            return traceHookPort;
        }

        EventSnapshotRegistryPort snapshotRegistry() {
            return snapshotRegistry;
        }

        EventLensCommandConfig commandConfig() {
            return commandConfig;
        }

        TraceCommandService.TraceStartOptions options() {
            return options;
        }

        TraceStartResult.Failure failure() {
            return failure;
        }

        void failure(TraceStartResult.Failure failure) {
            this.failure = failure;
        }
    }
}
