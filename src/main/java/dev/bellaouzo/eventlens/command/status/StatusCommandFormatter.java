package dev.bellaouzo.eventlens.command.status;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationDiagnostics;
import dev.bellaouzo.eventlens.domain.status.EventLensStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

final class StatusCommandFormatter {

    private StatusCommandFormatter() {}

    static void render(CommandSender sender, EventLensStatus status, EventLensCommandConfig commandConfig) {
        InstrumentationDiagnostics instrumentation = status.instrumentation();

        sender.sendMessage(header(status));
        sender.sendMessage(CommandUi.divider());
        sender.sendMessage(overviewLine(status, instrumentation));
        sender.sendMessage(tracingLine(status));
        sender.sendMessage(sessionsLine(status));
        sender.sendMessage(CommandUi.divider());
        sender.sendMessage(CommandUi.sectionTitle("Instrumentation"));
        StatusInstrumentationFormatter.renderSection(sender, status, instrumentation);
        sender.sendMessage(CommandUi.divider());
        sender.sendMessage(StatusCommandActions.render(sender, status, commandConfig));
    }

    private static Component header(EventLensStatus status) {
        return Component.text("\u25c6 ", NamedTextColor.GOLD)
                .append(Component.text("EventLens", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("  ", NamedTextColor.GRAY))
                .append(Component.text("v" + status.version(), NamedTextColor.YELLOW))
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                        "EventLens diagnostics plugin",
                        "Target: " + status.targetPlatform(),
                        "",
                        "Click actions below to explore commands")));
    }

    private static Component overviewLine(EventLensStatus status, InstrumentationDiagnostics instrumentation) {
        NamedTextColor platformColor =
                instrumentation.paperVersionCompatible() ? NamedTextColor.AQUA : NamedTextColor.YELLOW;
        return CommandUi.labeledLine(
                "Platform",
                Component.text(status.targetPlatform(), platformColor)
                        .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                                "Reported: " + instrumentation.paperVersionReported(),
                                "Bukkit: " + instrumentation.bukkitVersionReported(),
                                instrumentation.paperVersionCompatible()
                                        ? "Version check passed"
                                        : "Version may differ from tested platform"))));
    }

    private static Component tracingLine(EventLensStatus status) {
        return CommandUi.labeledLine("Tracing", CommandUi.enabledState(status.tracingEnabled()));
    }

    private static Component sessionsLine(EventLensStatus status) {
        Component count = status.activeSessionCount() > 0
                ? CommandUi.runCommand(
                        Integer.toString(status.activeSessionCount()),
                        "/eventlens trace list",
                        CommandUi.hoverBlock(
                                status.activeSessionCount() + " active session(s)", "", "Click to list trace sessions"))
                : Component.text("0", NamedTextColor.GRAY);

        return CommandUi.labeledLine("Active sessions", count);
    }
}
