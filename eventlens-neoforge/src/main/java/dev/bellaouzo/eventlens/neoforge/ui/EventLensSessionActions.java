package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionState;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;

final class EventLensSessionActions {

    private EventLensSessionActions() {}

    static String captureLabel(TraceSessionSummary session) {
        if (session == null) {
            return "Pause";
        }
        if (session.state().isTerminal()) {
            return "Restart";
        }
        return session.state() == TraceSessionState.PAUSED ? "Resume" : "Pause";
    }

    static boolean captureActive(TraceSessionSummary session) {
        return session != null && (isOpen(session) || session.state().isTerminal());
    }

    static boolean isOpen(TraceSessionSummary session) {
        if (session == null) {
            return false;
        }
        TraceSessionState state = session.state();
        return state == TraceSessionState.ACTIVE
                || state == TraceSessionState.THROTTLED
                || state == TraceSessionState.PAUSED;
    }

    static void applyCapture(EventLensScreen screen, String sessionId, String label) {
        if ("Restart".equals(label)) {
            ModTraceResults.RestartResult result = screen.coordinator().restartSession(sessionId);
            EventLensNotices.action(result.message());
            if (result.success()) {
                screen.showSession(result.sessionId());
                return;
            }
        } else if ("Resume".equals(label)) {
            EventLensNotices.action(screen.coordinator().resumeSession(sessionId).message());
        } else {
            EventLensNotices.action(screen.coordinator().pauseSession(sessionId).message());
        }
        screen.showSession(sessionId);
    }
}
