package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiff;
import dev.bellaouzo.eventlens.domain.live.LiveFeedChannel;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLine;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class LiveFeedAnalyzer {

    private LiveFeedAnalyzer() {}

    static List<LiveFeedLine> analyze(
            TraceDispatchRecord dispatch,
            long slowThresholdNanos,
            Optional<String> pluginFilter,
            boolean includeFrequency) {
        List<LiveFeedLine> lines = new ArrayList<>();
        if (includeFrequency) {
            lines.add(new LiveFeedLine(
                    LiveFeedChannel.FREQUENCY,
                    "#" + dispatch.sequence() + " " + simpleEventName(dispatch.eventClassName()) + " ("
                            + DurationStats.formatMillis(dispatch.durationNanos()) + ")",
                    false));
        }
        lines.addAll(analyzeSlowListeners(dispatch, slowThresholdNanos, pluginFilter));
        lines.addAll(analyzeCancellations(dispatch));
        lines.addAll(analyzeExceptions(dispatch, pluginFilter));
        return List.copyOf(lines);
    }

    static List<LiveFeedLine> analyzeAlerts(
            TraceDispatchRecord dispatch, long slowThresholdNanos, Optional<String> pluginFilter) {
        List<LiveFeedLine> alerts = new ArrayList<>();
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            if (!matchesPluginFilter(pluginFilter, timing.pluginName())) {
                continue;
            }
            if (timing.durationNanos() >= slowThresholdNanos || timing.exceedsSlowThreshold()) {
                alerts.add(new LiveFeedLine(
                        LiveFeedChannel.ALERT,
                        "Slow listener: " + timing.pluginName() + "#" + timing.methodName() + " "
                                + DurationStats.formatMillis(timing.durationNanos()),
                        true));
            }
        }
        return List.copyOf(alerts);
    }

    private static List<LiveFeedLine> analyzeSlowListeners(
            TraceDispatchRecord dispatch, long slowThresholdNanos, Optional<String> pluginFilter) {
        List<LiveFeedLine> lines = new ArrayList<>();
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            if (!matchesPluginFilter(pluginFilter, timing.pluginName())) {
                continue;
            }
            if (timing.durationNanos() >= slowThresholdNanos || timing.exceedsSlowThreshold()) {
                lines.add(new LiveFeedLine(
                        LiveFeedChannel.SLOW,
                        timing.pluginName() + "#" + timing.methodName() + " "
                                + DurationStats.formatMillis(timing.durationNanos()),
                        false));
            }
        }
        return lines;
    }

    private static List<LiveFeedLine> analyzeCancellations(TraceDispatchRecord dispatch) {
        if (!dispatch.cancellable()) {
            return List.of();
        }
        if (dispatch.cancelledAtStart() == dispatch.cancelledAtEnd()) {
            SnapshotDiff diff = TraceDispatchAnalyzer.overallDiff(dispatch, false);
            if (diff.cancellationTransition().isEmpty()) {
                return List.of();
            }
            CancellationTransitionKind kind =
                    diff.cancellationTransition().orElseThrow().kind();
            if (kind == CancellationTransitionKind.UNCHANGED) {
                return List.of();
            }
            return List.of(new LiveFeedLine(
                    LiveFeedChannel.CANCELLATION,
                    "#" + dispatch.sequence() + " cancellation " + kind.name().toLowerCase(Locale.ROOT),
                    false));
        }
        String transition = dispatch.cancelledAtStart() ? "remained cancelled" : "became cancelled";
        return List.of(
                new LiveFeedLine(LiveFeedChannel.CANCELLATION, "#" + dispatch.sequence() + " " + transition, false));
    }

    private static List<LiveFeedLine> analyzeExceptions(TraceDispatchRecord dispatch, Optional<String> pluginFilter) {
        List<LiveFeedLine> lines = new ArrayList<>();
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            if (!timing.threwException() || !matchesPluginFilter(pluginFilter, timing.pluginName())) {
                continue;
            }
            String exceptionType = timing.exceptionType().orElse("Exception");
            lines.add(new LiveFeedLine(
                    LiveFeedChannel.EXCEPTION,
                    timing.pluginName() + "#" + timing.methodName() + " threw " + exceptionType,
                    true));
        }
        return lines;
    }

    private static boolean matchesPluginFilter(Optional<String> pluginFilter, String pluginName) {
        return pluginFilter.map(pluginName::equalsIgnoreCase).orElse(true);
    }

    private static String simpleEventName(String eventClassName) {
        int lastDot = eventClassName.lastIndexOf('.');
        return lastDot >= 0 ? eventClassName.substring(lastDot + 1) : eventClassName;
    }
}
