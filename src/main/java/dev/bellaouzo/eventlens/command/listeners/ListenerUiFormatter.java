package dev.bellaouzo.eventlens.command.listeners;

import dev.bellaouzo.eventlens.command.CommandLiterals;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class ListenerUiFormatter {

    private ListenerUiFormatter() {}

    static void renderHeader(CommandSender sender, String eventClassName, OutputDetailLevel detailLevel) {
        String simpleName = CommandText.simpleName(eventClassName);
        Component eventLine = CommandUi.labeledLine(
                "Event",
                CommandUi.textWithHover(
                        eventClassName,
                        CommandUi.hoverBlock(
                                "Simple name: " + simpleName,
                                supportedTraceHint(eventClassName),
                                "",
                                "Click actions below to inspect or trace")));
        sender.sendMessage(eventLine);

        if (SupportedEventTypes.isSupported(eventClassName)) {
            sender.sendMessage(CommandUi.labeledLine(
                    "Trace",
                    CommandUi.runCommand(
                            "[Start trace]",
                            CommandLiterals.TRACE_START_PREFIX + simpleName,
                            "Start tracing " + simpleName)));
        } else if (detailLevel != OutputDetailLevel.BRIEF) {
            sender.sendMessage(Component.text("Tracing not supported for this event type.", NamedTextColor.DARK_GRAY));
        }
        sender.sendMessage(CommandUi.labeledLine("Trace support", traceSupportBadge(eventClassName)));
    }

    static void renderListener(
            CommandSender sender,
            ListenerRegistration listener,
            String eventSimpleName,
            OutputDetailLevel detailLevel) {
        String inspectCommand =
                CommandLiterals.TRACE_START_PREFIX + eventSimpleName + " --plugin " + listener.pluginName();
        Component pluginComponent = SupportedEventTypes.isSupportedSimpleName(eventSimpleName)
                ? CommandUi.runCommand(
                        listener.pluginName(),
                        inspectCommand,
                        CommandUi.hoverBlock(
                                "Plugin: " + listener.pluginName(),
                                "Priority: " + listener.priority(),
                                "Order: #" + listener.registrationOrder(),
                                CommandLiterals.IGNORE_CANCELLED_PREFIX + listener.ignoreCancelled(),
                                "",
                                "Click to trace dispatches involving this plugin"))
                : CommandUi.textWithHover(
                        listener.pluginName(),
                        CommandUi.hoverBlock(
                                "Plugin: " + listener.pluginName(),
                                "Priority: " + listener.priority(),
                                "Order: #" + listener.registrationOrder(),
                                CommandLiterals.IGNORE_CANCELLED_PREFIX + listener.ignoreCancelled()));

        sender.sendMessage(Component.text(
                        "#" + listener.registrationOrder() + " [" + listener.priority() + "] ", NamedTextColor.GRAY)
                .append(pluginComponent)
                .append(Component.text(" "))
                .append(traceSupportBadge(eventSimpleName)));

        if (detailLevel == OutputDetailLevel.BRIEF) {
            return;
        }

        Component methodLine = Component.text(
                        "    " + CommandText.simpleName(listener.listenerClassName()) + "#" + listener.methodName(),
                        NamedTextColor.GRAY)
                .hoverEvent(net.kyori.adventure.text.event.HoverEvent.showText(CommandUi.hoverBlock(
                        "Class: " + listener.listenerClassName(),
                        "Method: " + listener.methodName(),
                        CommandLiterals.IGNORE_CANCELLED_PREFIX + listener.ignoreCancelled())));

        if (detailLevel == OutputDetailLevel.VERBOSE) {
            methodLine = methodLine.append(Component.text(
                    " (" + CommandLiterals.IGNORE_CANCELLED_PREFIX + listener.ignoreCancelled() + ")",
                    NamedTextColor.DARK_GRAY));
        }

        sender.sendMessage(methodLine);
    }

    static void renderAmbiguousCandidate(CommandSender sender, String candidateClassName) {
        String simpleName = CommandText.simpleName(candidateClassName);
        sender.sendMessage(CommandUi.runCommand(
                        "  " + candidateClassName,
                        "/eventlens listeners " + simpleName,
                        CommandUi.hoverBlock("Inspect listeners", "", "[trace] start trace if supported"))
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(CommandUi.runCommand(
                        "[trace]", CommandLiterals.TRACE_START_PREFIX + simpleName, "Start tracing " + simpleName)));
    }

    private static String supportedTraceHint(String eventClassName) {
        if (SupportedEventTypes.isSupported(eventClassName)) {
            return "Tracing supported";
        }
        return "Tracing not supported for this event";
    }

    private static Component traceSupportBadge(String eventName) {
        boolean supported =
                SupportedEventTypes.isSupported(eventName) || SupportedEventTypes.isSupportedSimpleName(eventName);
        return supported
                ? Component.text("[trace-ready]", NamedTextColor.GREEN)
                : Component.text("[no-trace]", NamedTextColor.DARK_GRAY);
    }
}
