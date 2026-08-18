package dev.bellaouzo.eventlens.command.status;

import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.instrumentation.AgentInstallHints;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationCapabilities;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationDiagnosticLine;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationDiagnostics;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.domain.status.EventLensStatus;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class StatusInstrumentationFormatter {

    private StatusInstrumentationFormatter() {}

    static void renderSection(
            CommandSender sender, EventLensStatus status, InstrumentationDiagnostics instrumentation) {
        sender.sendMessage(modeLine(instrumentation));
        sender.sendMessage(agentLine(status, instrumentation));
        if (!status.agentAttached()) {
            renderAgentSetup(sender, status);
        }
        sender.sendMessage(capabilitiesLine(instrumentation.capabilities()));
        sender.sendMessage(CommandUi.divider());
        sender.sendMessage(CommandUi.sectionTitle("Diagnostics"));
        renderDiagnostics(sender, instrumentation);
    }

    private static Component modeLine(InstrumentationDiagnostics instrumentation) {
        return CommandUi.labeledLine("Mode", modeValue(instrumentation.mode()))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                        describeMode(instrumentation.mode()),
                        "",
                        instrumentation.capabilities().priorityBandFallback()
                                ? "Priority-band fallback available"
                                : "No fallback available")));
    }

    private static Component modeValue(InstrumentationMode mode) {
        NamedTextColor color =
                switch (mode) {
                    case PRECISE -> NamedTextColor.GREEN;
                    case DEGRADED -> NamedTextColor.GOLD;
                    case DISPATCH_ONLY -> NamedTextColor.YELLOW;
                };
        String label = mode.name().toLowerCase(Locale.ROOT).replace('_', '-');
        return Component.text(label, color);
    }

    private static Component agentLine(EventLensStatus status, InstrumentationDiagnostics instrumentation) {
        if (status.agentAttached()) {
            Component attached = Component.text("attached", NamedTextColor.GREEN)
                    .append(Component.text(" (protocol " + status.agentProtocolVersion() + ")", NamedTextColor.GRAY));
            if (!instrumentation.agentProtocolCompatible()) {
                attached = attached.append(Component.text(" \u26a0 incompatible", NamedTextColor.RED));
            }
            return CommandUi.labeledLine("Agent", attached);
        }

        return CommandUi.labeledLine(
                "Agent",
                Component.text("not attached", NamedTextColor.RED)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                                "Per-listener timing requires the Paper Java agent.",
                                "Dispatch timing and priority-band snapshots still work.",
                                "",
                                "See Agent setup below or README on GitHub"))));
    }

    private static void renderAgentSetup(CommandSender sender, EventLensStatus status) {
        sender.sendMessage(Component.text("Agent setup", NamedTextColor.YELLOW));
        String suggested = status.agentArgument().orElse(AgentInstallHints.paperJvmArgument(status.version()));
        sender.sendMessage(Component.text("  JVM arg: ", NamedTextColor.GRAY)
                .append(Component.text(suggested, NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.copyToClipboard(suggested))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                Component.text("Click to copy. Add to Paper startup, then restart.", NamedTextColor.GRAY)))));
        sender.sendMessage(Component.text(
                "  Download eventlens-agent-" + status.version() + ".jar from GitHub releases.",
                NamedTextColor.GRAY));
        sender.sendMessage(Component.text(
                "  Restart the server (stop, then start). Status should show precise.",
                NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  Guide: ", NamedTextColor.GRAY)
                .append(Component.text(AgentInstallHints.README_URL, NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.copyToClipboard(AgentInstallHints.README_URL))
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(
                                Component.text("Click to copy the README link.", NamedTextColor.GRAY)))));
    }

    private static Component capabilitiesLine(InstrumentationCapabilities capabilities) {
        return Component.text("Capabilities: ", NamedTextColor.GRAY)
                .append(CommandUi.capabilityChip(
                        "Duration",
                        capabilities.perListenerDuration(),
                        "Per-listener execution timing",
                        "Dispatch-level timing only"))
                .append(Component.text("  ", NamedTextColor.DARK_GRAY))
                .append(CommandUi.capabilityChip(
                        "Snapshots",
                        capabilities.perListenerSnapshots(),
                        "Before/after state per listener",
                        "Priority-band snapshots only"))
                .append(Component.text("  ", NamedTextColor.DARK_GRAY))
                .append(CommandUi.capabilityChip(
                        "Diffs",
                        capabilities.perListenerPropertyDiffs(),
                        "Property changes attributed to listeners",
                        "Band-level attribution only"))
                .append(Component.text("  ", NamedTextColor.DARK_GRAY))
                .append(CommandUi.capabilityChip(
                        "Timeline",
                        capabilities.exactCancellationTimeline(),
                        "Exact cancellation order",
                        "Band-level cancellation only"));
    }

    private static void renderDiagnostics(CommandSender sender, InstrumentationDiagnostics instrumentation) {
        for (InstrumentationDiagnosticLine line : instrumentation.lines()) {
            sender.sendMessage(Component.text("  ", NamedTextColor.GRAY)
                    .append(CommandUi.codeBadge(line.code(), colorFor(line.level())))
                    .append(Component.text(" ", NamedTextColor.GRAY))
                    .append(Component.text(line.message(), messageColor(line.level()))));
        }
    }

    private static NamedTextColor colorFor(String level) {
        return switch (level) {
            case "error" -> NamedTextColor.RED;
            case "warn" -> NamedTextColor.YELLOW;
            default -> NamedTextColor.AQUA;
        };
    }

    private static NamedTextColor messageColor(String level) {
        return switch (level) {
            case "error" -> NamedTextColor.RED;
            case "warn" -> NamedTextColor.YELLOW;
            default -> NamedTextColor.GRAY;
        };
    }

    private static String describeMode(InstrumentationMode mode) {
        return switch (mode) {
            case PRECISE -> "Full per-listener timing, snapshots, diffs, and cancellation timeline.";
            case DISPATCH_ONLY -> "Dispatch-level capture with priority-band fallback.";
            case DEGRADED -> "Agent attached but some precise capabilities are unavailable.";
        };
    }
}
