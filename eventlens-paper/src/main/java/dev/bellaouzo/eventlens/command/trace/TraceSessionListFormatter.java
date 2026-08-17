package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TraceSessionListFormatter {

    private TraceSessionListFormatter() {}

    static void render(CommandSender sender, List<TraceSessionSummary> sessions, EventLensCommandConfig commandConfig) {
        if (sessions.isEmpty()) {
            sender.sendMessage(Component.text("No trace sessions.", NamedTextColor.GRAY));
            return;
        }

        sender.sendMessage(Component.text("Trace sessions:", NamedTextColor.GOLD));
        for (TraceSessionSummary session : sessions) {
            String eventSimpleName = CommandText.simpleName(session.eventClassName());
            String viewCommand = "/eventlens trace view " + session.sessionId();
            Component line = Component.text(session.sessionId(), NamedTextColor.YELLOW)
                    .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                            "Event: " + eventSimpleName,
                            "State: " + session.state(),
                            "Owner: " + session.ownerName(),
                            "Captured: " + session.capturedEvents(),
                            "Dropped: " + session.droppedEvents(),
                            "Sampled out: " + session.sampledOutEvents(),
                            "",
                            "Click to view trace")))
                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(viewCommand))
                    .append(Component.text(" [" + session.state() + "] ", NamedTextColor.GRAY))
                    .append(CommandUi.runCommand(
                            eventSimpleName,
                            "/eventlens listeners " + eventSimpleName,
                            "Inspect listeners for " + eventSimpleName))
                    .append(Component.text(
                            " captured=" + session.capturedEvents() + " dropped=" + session.droppedEvents()
                                    + " sampled=" + session.sampledOutEvents() + " owner=" + session.ownerName(),
                            NamedTextColor.DARK_GRAY));
            sender.sendMessage(line);
        }

        if (commandConfig.showPerformanceWarnings()) {
            sender.sendMessage(Component.text(
                    "Active traces add per-dispatch overhead. Stop sessions when finished.", NamedTextColor.GRAY));
        }
    }
}
