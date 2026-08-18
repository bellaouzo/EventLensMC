package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.ListenerChangeAnalyzer;
import dev.bellaouzo.eventlens.application.SessionTimingAnalyzer;
import dev.bellaouzo.eventlens.application.TraceDispatchAnalyzer;
import dev.bellaouzo.eventlens.domain.instrumentation.AgentInstallHints;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.observability.RankedListenerTiming;
import dev.bellaouzo.eventlens.domain.observability.RankedPluginTiming;
import dev.bellaouzo.eventlens.domain.observability.SessionTimingSummary;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class TraceTimingFormatter {

    private TraceTimingFormatter() {}

    public static void renderSessionHeader(CommandSender sender, TraceSessionSummary summary, int page) {
        if (page != 1 || summary.timingSummary() == null) {
            return;
        }

        SessionTimingSummary timing = summary.timingSummary();
        DurationStats dispatchStats = timing.dispatchStats();
        if (dispatchStats.count() > 0) {
            sender.sendMessage(Component.text(
                    "Timing: " + dispatchStats.count() + " dispatches · avg "
                            + dispatchStats.formatAverageMillis() + " · p95 "
                            + dispatchStats.formatP95Millis() + " · max "
                            + dispatchStats.formatMaxMillis(),
                    NamedTextColor.GOLD));
        }

        sender.sendMessage(Component.text(
                "Agent: "
                        + (timing.agentAttached() ? "attached" : "not attached")
                        + " · slow threshold "
                        + DurationStats.formatMillis(summary.slowThresholdNanos()),
                NamedTextColor.GRAY));

        if (!timing.slowestPlugins().isEmpty()) {
            sender.sendMessage(Component.text(
                    "Slowest plugins: " + formatPluginRankings(timing.slowestPlugins()), NamedTextColor.YELLOW));
        }

        if (!timing.slowestListeners().isEmpty()) {
            sender.sendMessage(Component.text(
                    "Slowest listeners: " + formatListenerRankings(timing.slowestListeners()), NamedTextColor.YELLOW));
        }

        if (summary.sampledOutEvents() > 0) {
            sender.sendMessage(Component.text(
                    "Sampled out: " + summary.sampledOutEvents() + " hot-event dispatches", NamedTextColor.GRAY));
        }

        if (timing.sessionPartialReasons().contains(TracePartialReason.AGENT_ABSENT)) {
            sender.sendMessage(Component.text(
                    "Per-listener timing needs the Paper Java agent. Run /eventlens status for JVM args.",
                    NamedTextColor.RED));
            sender.sendMessage(Component.text("Install guide: " + AgentInstallHints.README_URL, NamedTextColor.GRAY));
        }

        if (timing.sessionPartialReasons().contains(TracePartialReason.THROTTLED)) {
            sender.sendMessage(
                    Component.text("Session throttled due to EventLens overhead budget.", NamedTextColor.RED));
        }

        if (!timing.frequentListenerWarnings().isEmpty()) {
            sender.sendMessage(Component.text("Frequent listeners:", NamedTextColor.GOLD));
            for (String warning : timing.frequentListenerWarnings()) {
                sender.sendMessage(Component.text("  " + warning, NamedTextColor.YELLOW));
            }
        }
    }

    public static void renderDispatchTiming(
            CommandSender sender, TraceDispatchRecord dispatch, long slowThresholdNanos) {
        sender.sendMessage(Component.text(
                "  Dispatch: " + DurationStats.formatMillis(dispatch.durationNanos())
                        + " · EventLens "
                        + DurationStats.formatMillis(dispatch.eventLensOverheadNanos())
                        + " · "
                        + dispatch.listenerTimings().size()
                        + " timed listeners",
                NamedTextColor.GRAY));

        List<ListenerTimingRecord> slowListeners =
                SessionTimingAnalyzer.slowListenersForDispatch(dispatch, slowThresholdNanos);
        if (!slowListeners.isEmpty()) {
            sender.sendMessage(Component.text("  Slow listeners:", NamedTextColor.RED));
            for (ListenerTimingRecord timing : slowListeners) {
                NamedTextColor color = timing.mainThreadBlocked() ? NamedTextColor.RED : NamedTextColor.GOLD;
                String flags = timing.mainThreadBlocked() ? " [MAIN_THREAD_BLOCK]" : "";
                sender.sendMessage(Component.text(
                        "    "
                                + timing.pluginName()
                                + "/"
                                + simpleClassName(timing.listenerClassName())
                                + "."
                                + timing.methodName()
                                + " "
                                + DurationStats.formatMillis(timing.durationNanos())
                                + flags,
                        color));
                timing.stackTrace()
                        .ifPresent(stack -> sender.sendMessage(
                                Component.text(Objects.requireNonNull(stack), NamedTextColor.DARK_GRAY)));
            }
        }

        DurationStats listenerStats = SessionTimingAnalyzer.listenerStatsForDispatch(dispatch);
        if (listenerStats.count() > 0) {
            sender.sendMessage(Component.text(
                    "  Listener stats: avg "
                            + listenerStats.formatAverageMillis()
                            + " · max "
                            + listenerStats.formatMaxMillis(),
                    NamedTextColor.DARK_GRAY));
        }

        renderListenerChanges(sender, dispatch);
    }

    private static void renderListenerChanges(CommandSender sender, TraceDispatchRecord dispatch) {
        var changedListeners = ListenerChangeAnalyzer.listenersWithChanges(dispatch);
        if (changedListeners.isEmpty()) {
            return;
        }
        sender.sendMessage(Component.text("  Per-listener changes:", NamedTextColor.GOLD));
        for (var timing : changedListeners) {
            sender.sendMessage(Component.text(
                    "    #" + timing.invocationOrder() + " " + timing.pluginName() + "/"
                            + simpleClassName(timing.listenerClassName()) + "."
                            + timing.methodName() + " "
                            + DurationStats.formatMillis(timing.durationNanos()),
                    NamedTextColor.YELLOW));
            timing.cancellationTransition()
                    .filter(transition -> transition.kind()
                            != dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind.UNCHANGED)
                    .ifPresent(transition -> sender.sendMessage(Component.text(
                            "      Cancellation: " + transition.kind().name().toLowerCase(Locale.ROOT),
                            NamedTextColor.RED)));
            for (var change : timing.propertyChanges()) {
                sender.sendMessage(Component.text(
                        "      " + TraceDispatchAnalyzer.formatPropertyChange(change), NamedTextColor.AQUA));
            }
            if (timing.threwException()) {
                sender.sendMessage(Component.text(
                        "      Exception: " + timing.exceptionType().orElse("unknown"), NamedTextColor.RED));
            }
        }
    }

    private static String formatPluginRankings(List<RankedPluginTiming> rankings) {
        return rankings.stream()
                .map(ranked -> ranked.pluginName() + " (p95 " + ranked.stats().formatP95Millis() + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String formatListenerRankings(List<RankedListenerTiming> rankings) {
        return rankings.stream()
                .map(ranked -> ranked.identity().displayName() + " (p95 "
                        + ranked.stats().formatP95Millis() + ")")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private static String simpleClassName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }
}
