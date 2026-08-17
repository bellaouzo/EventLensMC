package dev.bellaouzo.eventlens.command.plugin;

import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.plugin.PluginAttributedChange;
import dev.bellaouzo.eventlens.domain.plugin.PluginTraceStatistics;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class PluginProfileTraceFormatter {

    private PluginProfileTraceFormatter() {}

    static void renderTraceStatistics(
            CommandSender sender, PluginTraceStatistics statistics, OutputDetailLevel detailLevel) {
        if (detailLevel == OutputDetailLevel.BRIEF && statistics.invocationCount() == 0) {
            return;
        }

        sender.sendMessage(Component.text("Trace statistics (in-memory sessions):", NamedTextColor.YELLOW));
        if (statistics.tracedDispatchCount() == 0) {
            sender.sendMessage(
                    Component.text("  No traced dispatches involving this plugin yet.", NamedTextColor.GRAY));
            return;
        }

        sender.sendMessage(Component.text(
                "  Dispatches: " + statistics.tracedDispatchCount() + ", invocations: " + statistics.invocationCount(),
                NamedTextColor.WHITE));

        if (statistics.invocationCount() > 0) {
            sender.sendMessage(Component.text(
                    "  Timing avg "
                            + statistics.listenerTiming().formatAverageMillis()
                            + ", p95 "
                            + statistics.listenerTiming().formatP95Millis()
                            + ", max "
                            + statistics.listenerTiming().formatMaxMillis(),
                    NamedTextColor.WHITE));
        } else if (!statistics.agentAttached()) {
            sender.sendMessage(
                    Component.text("  Timing unavailable (EventLens agent not attached).", NamedTextColor.DARK_GRAY));
        }

        sender.sendMessage(Component.text("  Exceptions: " + statistics.exceptionCount(), NamedTextColor.WHITE));
        if (detailLevel != OutputDetailLevel.BRIEF
                && !statistics.exceptionCountByType().isEmpty()) {
            statistics
                    .exceptionCountByType()
                    .forEach((type, count) ->
                            sender.sendMessage(Component.text("    " + type + ": " + count, NamedTextColor.GRAY)));
        }
    }

    static void renderRecentChanges(
            CommandSender sender, PluginTraceStatistics statistics, OutputDetailLevel detailLevel) {
        if (statistics.recentChanges().isEmpty()) {
            if (detailLevel == OutputDetailLevel.VERBOSE) {
                sender.sendMessage(CommandUi.labeledLine(
                        "Recent changes", Component.text("none in trace sessions", NamedTextColor.GRAY)));
            }
            return;
        }

        sender.sendMessage(Component.text("Recent changes attributed to plugin:", NamedTextColor.YELLOW));
        int limit = detailLevel == OutputDetailLevel.BRIEF
                ? 3
                : statistics.recentChanges().size();
        for (int index = 0; index < Math.min(statistics.recentChanges().size(), limit); index++) {
            renderChange(sender, statistics.recentChanges().get(index));
        }
    }

    private static void renderChange(CommandSender sender, PluginAttributedChange change) {
        String simpleEvent = CommandText.simpleName(change.eventClassName());
        Component line = Component.text(
                        "  #" + change.dispatchSequence() + " " + simpleEvent + " @ " + change.priorityBand(),
                        NamedTextColor.WHITE)
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                        "Session: " + change.sessionId(),
                        "Event: " + change.eventClassName(),
                        "Properties: " + String.join(", ", change.changedProperties()),
                        change.conflictingAttribution() ? "Conflicting attribution" : "Single attribution")));
        sender.sendMessage(line);
    }
}
