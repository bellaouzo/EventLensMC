package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;

public final class ModTraceResults {

    private ModTraceResults() {}

    public record Status(
            String version,
            String platform,
            String minecraftVersion,
            boolean tracingEnabled,
            boolean agentPresent,
            int agentProtocolVersion,
            boolean agentProtocolCompatible,
            boolean snapshotsEnabled,
            int activeSessionCount,
            List<TraceSessionSummary> sessions,
            List<String> supportedEvents) {}

    public record StartResult(
            boolean success, boolean needsHotConfirm, String message, String sessionId, String eventSimpleName) {
        static StartResult success(String sessionId, String eventSimpleName, boolean hot) {
            String suffix = hot ? " (hot event, sampled/limited)" : "";
            return new StartResult(true, false, "Trace session started: " + sessionId + suffix, sessionId, eventSimpleName);
        }

        static StartResult hotConfirmation(String eventSimpleName) {
            return new StartResult(
                    false,
                    true,
                    eventSimpleName + " is a hot client event. Click confirm to start a bounded session.",
                    "",
                    eventSimpleName);
        }

        static StartResult failure(String message) {
            return new StartResult(false, false, message, "", "");
        }
    }

    public record StopResult(boolean success, String message, List<String> sessionIds) {
        static StopResult success(List<String> sessionIds) {
            return StopResult.ok("Stopped " + sessionIds.size() + " session(s).", sessionIds);
        }

        private static StopResult ok(String message, List<String> sessionIds) {
            return new StopResult(true, message, sessionIds);
        }

        static StopResult failure(String message) {
            return new StopResult(false, message, List.of());
        }
    }

    public record PauseResult(boolean success, boolean paused, String message, List<String> sessionIds) {
        static PauseResult paused(List<String> sessionIds) {
            return new PauseResult(true, true, "Paused " + sessionIds.size() + " session(s).", sessionIds);
        }

        static PauseResult resumed(List<String> sessionIds) {
            return new PauseResult(true, false, "Resumed " + sessionIds.size() + " session(s).", sessionIds);
        }

        static PauseResult failure(String message) {
            return new PauseResult(false, false, message, List.of());
        }
    }

    public record ViewResult(
            Kind kind,
            String sessionId,
            TraceSessionSummary summary,
            List<TraceDispatchRecord> records,
            int page,
            int totalPages,
            String message,
            boolean focused) {
        public enum Kind {
            SUCCESS,
            NOT_FOUND,
            INVALID_PAGE
        }

        static ViewResult success(
                TraceSessionSummary summary,
                List<TraceDispatchRecord> records,
                int page,
                int totalPages,
                boolean focused) {
            return new ViewResult(Kind.SUCCESS, summary.sessionId(), summary, records, page, totalPages, "", focused);
        }

        static ViewResult notFound(String sessionId) {
            return new ViewResult(Kind.NOT_FOUND, sessionId, null, List.of(), 0, 0, "No trace session \"" + sessionId + "\".", false);
        }

        static ViewResult dispatchNotFound(String sessionId, int sequence) {
            return new ViewResult(
                    Kind.NOT_FOUND,
                    sessionId,
                    null,
                    List.of(),
                    0,
                    0,
                    "No dispatch #" + sequence + " in session " + sessionId + ".",
                    false);
        }

        static ViewResult invalidPage(int page, int totalPages) {
            return new ViewResult(
                    Kind.INVALID_PAGE,
                    "",
                    null,
                    List.of(),
                    page,
                    totalPages,
                    "Page " + page + " is out of range (1-" + totalPages + ").",
                    false);
        }
    }

    public record RestartResult(
            boolean success, String message, String sessionId, String sourceSessionId, String eventSimpleName) {
        static RestartResult success(String sessionId, String sourceSessionId, String eventSimpleName) {
            return new RestartResult(
                    true,
                    "Restarted "
                            + sourceSessionId
                            + " as "
                            + sessionId
                            + " ("
                            + eventSimpleName
                            + "). Previous session remains available for view/export.",
                    sessionId,
                    sourceSessionId,
                    eventSimpleName);
        }

        static RestartResult failure(String message) {
            return new RestartResult(false, message, "", "", "");
        }
    }

    public record ExportResult(boolean success, String message, String path, int dispatchCount, String sessionId) {
        static ExportResult success(String path, int dispatchCount, String sessionId) {
            return new ExportResult(
                    true, "Exported " + dispatchCount + " dispatch(es) to " + path, path, dispatchCount, sessionId);
        }

        static ExportResult failure(String message) {
            return new ExportResult(false, message, "", 0, "");
        }
    }
}
