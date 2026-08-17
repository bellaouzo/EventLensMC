package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.domain.trace.TraceSessionGeneration;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;

final class EventLensSessionRuns {

    private EventLensSessionRuns() {}

    static List<TraceSessionGeneration> list(EventLensScreen screen) {
        return screen.coordinator().listGenerations(screen.sessionId());
    }

    static int selectedIndex(EventLensScreen screen, List<TraceSessionGeneration> generations) {
        if (generations.isEmpty()) {
            return 0;
        }
        int generation = screen.sessionGeneration();
        if (generation < 0) {
            return generations.size() - 1;
        }
        for (int i = 0; i < generations.size(); i++) {
            if (generations.get(i).generation() == generation) {
                return i;
            }
        }
        return generations.size() - 1;
    }

    static String label(EventLensScreen screen) {
        List<TraceSessionGeneration> generations = list(screen);
        if (generations.size() <= 1) {
            return "";
        }
        int index = selectedIndex(screen, generations);
        return "Run " + (index + 1) + "/" + generations.size();
    }

    static String searchHint(EventLensScreen screen) {
        ModTraceResults.ViewResult result = screen.dispatchSequence() >= 0
                ? screen.coordinator()
                        .viewDispatch(screen.sessionId(), screen.dispatchSequence(), screen.sessionGenerationOption())
                : screen.coordinator().viewSession(screen.sessionId(), 1, screen.sessionGenerationOption());
        if (result.summary() == null) {
            return "Search this session";
        }
        String badge = result.summary().restarted() ? "  ·  " + result.summary().restartBadge() : "";
        String run = label(screen);
        return SupportedModEventTypes.displaySimpleName(result.summary().eventClassName())
                + badge
                + (run.isBlank() ? "" : "  ·  " + run)
                + "  ·  filter";
    }

    static void cycle(EventLensScreen screen) {
        List<TraceSessionGeneration> generations = list(screen);
        if (generations.size() <= 1) {
            return;
        }
        int next = (selectedIndex(screen, generations) + 1) % generations.size();
        screen.showSessionRun(screen.sessionId(), generations.get(next).generation());
    }
}
