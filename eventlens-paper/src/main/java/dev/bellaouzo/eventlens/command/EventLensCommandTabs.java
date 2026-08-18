package dev.bellaouzo.eventlens.command;

import dev.bellaouzo.eventlens.command.events.EventsCommandHandler;
import dev.bellaouzo.eventlens.command.exceptions.ExceptionsCommandHandler;
import dev.bellaouzo.eventlens.command.instrumentation.InstrumentationCommandHandler;
import dev.bellaouzo.eventlens.command.listeners.ListenersCommandHandler;
import dev.bellaouzo.eventlens.command.plugin.PluginCommandHandler;
import dev.bellaouzo.eventlens.command.status.StatusCommandHandler;
import dev.bellaouzo.eventlens.command.trace.TraceCommandHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.CommandSender;

final class EventLensCommandTabs {

    private EventLensCommandTabs() {}

    static List<String> complete(CommandSender sender, String[] args, Handlers handlers) {
        if (args.length == 0 || args.length == 1) {
            return root(sender, args.length == 0 ? "" : args[0]);
        }
        String prefix = args[args.length - 1];
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "listeners" ->
                permitted(sender, ListenersCommandHandler.PERMISSION) && args.length == 2
                        ? handlers.listeners().tabCompleteEventNames(args[1])
                        : Collections.emptyList();
            case "events" ->
                permitted(sender, EventsCommandHandler.PERMISSION) && args.length == 2
                        ? handlers.events().tabComplete(args[1])
                        : Collections.emptyList();
            case "exceptions" ->
                permitted(sender, ExceptionsCommandHandler.PERMISSION) && args.length == 2
                        ? handlers.exceptions().tabComplete(args[1])
                        : Collections.emptyList();
            case "plugin" ->
                permitted(sender, PluginCommandHandler.PERMISSION)
                        ? handlers.plugin().tabComplete(args, prefix)
                        : Collections.emptyList();
            case "trace" ->
                permitted(sender, TraceCommandHandler.PERMISSION)
                        ? handlers.trace().tabComplete(args, prefix)
                        : Collections.emptyList();
            case "instrumentation" ->
                permitted(sender, InstrumentationCommandHandler.PERMISSION)
                        ? handlers.instrumentation().tabComplete(args, prefix)
                        : Collections.emptyList();
            default -> Collections.emptyList();
        };
    }

    static List<String> root(CommandSender sender, String prefix) {
        List<String> subcommands = new ArrayList<>();
        if (permitted(sender, StatusCommandHandler.PERMISSION)) {
            subcommands.add("status");
        }
        if (permitted(sender, ListenersCommandHandler.PERMISSION)) {
            subcommands.add("listeners");
            subcommands.add("events");
        }
        if (permitted(sender, PluginCommandHandler.PERMISSION)) {
            subcommands.add("plugin");
        }
        if (permitted(sender, TraceCommandHandler.PERMISSION)) {
            subcommands.add("trace");
            subcommands.add("exceptions");
        }
        if (permitted(sender, InstrumentationCommandHandler.PERMISSION)) {
            subcommands.add("instrumentation");
        }
        return CommandText.filterPrefix(subcommands, prefix);
    }

    private static boolean permitted(CommandSender sender, String permission) {
        return EventLensPermissions.has(sender, permission);
    }

    record Handlers(
            ListenersCommandHandler listeners,
            EventsCommandHandler events,
            ExceptionsCommandHandler exceptions,
            PluginCommandHandler plugin,
            TraceCommandHandler trace,
            InstrumentationCommandHandler instrumentation) {}
}
