package dev.bellaouzo.eventlens.command.plugin;

import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.plugin.PluginCompareResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginProfile;
import dev.bellaouzo.eventlens.domain.plugin.PluginTraceStatistics;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class PluginCompareFormatter {

    private PluginCompareFormatter() {}

    static void render(CommandSender sender, PluginCompareResult result) {
        PluginProfile left = result.left();
        PluginProfile right = result.right();

        sender.sendMessage(Component.text(
                "Plugin comparison: " + left.descriptor().name() + " vs "
                        + right.descriptor().name(),
                NamedTextColor.GOLD));

        renderMetricRow(sender, "State", formatState(left), formatState(right));
        renderMetricRow(
                sender,
                "Version",
                left.descriptor().version(),
                right.descriptor().version());
        renderMetricRow(
                sender,
                "Events",
                String.valueOf(left.inventory().eventClassNames().size()),
                String.valueOf(right.inventory().eventClassNames().size()));
        renderMetricRow(
                sender,
                "Listeners",
                String.valueOf(left.inventory().bindings().size()),
                String.valueOf(right.inventory().bindings().size()));
        renderTimingRow(sender, left.traceStatistics(), right.traceStatistics());
        renderMetricRow(
                sender,
                "Exceptions",
                String.valueOf(left.traceStatistics().exceptionCount()),
                String.valueOf(right.traceStatistics().exceptionCount()));

        renderEventList(sender, "Shared events", result.sharedEvents());
        renderEventList(sender, left.descriptor().name() + " only", result.leftOnlyEvents());
        renderEventList(sender, right.descriptor().name() + " only", result.rightOnlyEvents());
        renderSharedInteractions(sender, result.sharedCoPlugins());
    }

    private static void renderMetricRow(CommandSender sender, String label, String left, String right) {
        sender.sendMessage(CommandUi.labeledLine(
                label,
                Component.text(left + " | " + right, NamedTextColor.WHITE)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                CommandUi.hoverBlock("Left: " + left, "Right: " + right)))));
    }

    private static void renderTimingRow(
            CommandSender sender, PluginTraceStatistics leftStats, PluginTraceStatistics rightStats) {
        sender.sendMessage(CommandUi.labeledLine(
                "Timing p95",
                Component.text(formatTiming(leftStats) + " | " + formatTiming(rightStats), NamedTextColor.WHITE)));
    }

    private static String formatTiming(PluginTraceStatistics statistics) {
        if (statistics.invocationCount() == 0) {
            return statistics.agentAttached() ? "no samples" : "agent absent";
        }
        return statistics.listenerTiming().formatP95Millis();
    }

    private static String formatState(PluginProfile profile) {
        return profile.descriptor().enabled() ? "enabled" : "disabled";
    }

    private static void renderEventList(CommandSender sender, String title, List<String> events) {
        sender.sendMessage(Component.text(title + " (" + events.size() + "):", NamedTextColor.YELLOW));
        if (events.isEmpty()) {
            sender.sendMessage(Component.text("  none", NamedTextColor.GRAY));
            return;
        }

        int limit = Math.min(events.size(), 10);
        for (int index = 0; index < limit; index++) {
            sender.sendMessage(Component.text("  " + CommandText.simpleName(events.get(index)), NamedTextColor.WHITE));
        }
        if (events.size() > limit) {
            sender.sendMessage(Component.text("  ... and " + (events.size() - limit) + " more.", NamedTextColor.GRAY));
        }
    }

    private static void renderSharedInteractions(CommandSender sender, List<String> sharedCoPlugins) {
        sender.sendMessage(
                Component.text("Shared interaction partners (" + sharedCoPlugins.size() + "):", NamedTextColor.YELLOW));
        if (sharedCoPlugins.isEmpty()) {
            sender.sendMessage(Component.text("  none", NamedTextColor.GRAY));
            return;
        }

        sender.sendMessage(Component.text(String.join(", ", sharedCoPlugins), NamedTextColor.WHITE));
    }
}
