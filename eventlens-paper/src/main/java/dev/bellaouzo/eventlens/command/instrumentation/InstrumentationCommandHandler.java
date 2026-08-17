package dev.bellaouzo.eventlens.command.instrumentation;

import dev.bellaouzo.eventlens.application.InstrumentationTestService;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class InstrumentationCommandHandler {

    public static final String PERMISSION = EventLensPermissions.INSTRUMENTATION;
    private static final String SUBCOMMAND_TEST = "test";

    private final InstrumentationTestService instrumentationTestService;

    public InstrumentationCommandHandler(InstrumentationTestService instrumentationTestService) {
        this.instrumentationTestService = instrumentationTestService;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.has(sender, PERMISSION)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase(SUBCOMMAND_TEST)) {
            sender.sendMessage(Component.text("Usage: /eventlens instrumentation test", NamedTextColor.YELLOW));
            return;
        }

        InstrumentationTestService.TestResult result = instrumentationTestService.run();
        sender.sendMessage(Component.text("Instrumentation test:", NamedTextColor.GOLD));
        sender.sendMessage(CommandUi.labeledLine(
                "Agent attached",
                Component.text(
                        result.agentPresent() ? "yes" : "no",
                        result.agentPresent() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        sender.sendMessage(CommandUi.labeledLine(
                "Protocol",
                Component.text(
                        result.protocolVersion() + (result.protocolCompatible() ? " (compatible)" : " (incompatible)"),
                        result.protocolCompatible() ? NamedTextColor.GREEN : NamedTextColor.YELLOW)));
        sender.sendMessage(CommandUi.labeledLine(
                "Snapshots",
                Component.text(
                        result.snapshotsEnabled() ? "enabled" : "disabled",
                        result.snapshotsEnabled() ? NamedTextColor.GREEN : NamedTextColor.YELLOW)));
        sender.sendMessage(CommandUi.labeledLine(
                "Agent jar",
                Component.text(
                        result.resolvableAgentJar() ? "resolved" : "not found",
                        result.resolvableAgentJar() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        result.agentArgument()
                .ifPresentOrElse(
                        arg -> sender.sendMessage(Component.text("[Copy agent arg]", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.copyToClipboard(arg))),
                        () -> sender.sendMessage(
                                Component.text("Could not resolve -javaagent argument.", NamedTextColor.YELLOW)));
    }

    public List<String> tabComplete(String[] args, String prefix) {
        if (args.length == 2) {
            return dev.bellaouzo.eventlens.command.CommandText.filterPrefix(List.of(SUBCOMMAND_TEST), prefix);
        }
        if (args.length > 2 && args[1].toLowerCase(Locale.ROOT).startsWith(SUBCOMMAND_TEST)) {
            return List.of();
        }
        return List.of();
    }
}
