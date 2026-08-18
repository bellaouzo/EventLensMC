package dev.bellaouzo.eventlens.command;

import dev.bellaouzo.eventlens.application.EventLensCommandContext;
import dev.bellaouzo.eventlens.command.events.EventsCommandHandler;
import dev.bellaouzo.eventlens.command.exceptions.ExceptionsCommandHandler;
import dev.bellaouzo.eventlens.command.instrumentation.InstrumentationCommandHandler;
import dev.bellaouzo.eventlens.command.listeners.ListenersCommandHandler;
import dev.bellaouzo.eventlens.command.plugin.PluginCommandHandler;
import dev.bellaouzo.eventlens.command.status.StatusCommandHandler;
import dev.bellaouzo.eventlens.command.trace.TraceCommandHandler;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EventLensCommand implements CommandExecutor, TabCompleter {

    private static final String SUBCOMMAND_STATUS = "status";
    private static final String SUBCOMMAND_LISTENERS = "listeners";
    private static final String SUBCOMMAND_TRACE = "trace";
    private static final String SUBCOMMAND_PLUGIN = "plugin";
    private static final String SUBCOMMAND_INSTRUMENTATION = "instrumentation";
    private static final String SUBCOMMAND_EVENTS = "events";
    private static final String SUBCOMMAND_EXCEPTIONS = "exceptions";

    private final StatusCommandHandler statusCommandHandler;
    private final ListenersCommandHandler listenersCommandHandler;
    private final PluginCommandHandler pluginCommandHandler;
    private final TraceCommandHandler traceCommandHandler;
    private final InstrumentationCommandHandler instrumentationCommandHandler;
    private final EventsCommandHandler eventsCommandHandler;
    private final ExceptionsCommandHandler exceptionsCommandHandler;

    public EventLensCommand(EventLensCommandContext context) {
        this.statusCommandHandler = new StatusCommandHandler(context.statusQueryService(), context.commandConfig());
        this.listenersCommandHandler =
                new ListenersCommandHandler(context.listenerQueryService(), context.commandConfig());
        this.pluginCommandHandler = new PluginCommandHandler(context.pluginQueryService(), context.commandConfig());
        this.traceCommandHandler = new TraceCommandHandler(context);
        this.instrumentationCommandHandler = new InstrumentationCommandHandler(context.instrumentationTestService());
        this.eventsCommandHandler = new EventsCommandHandler(context.eventCatalogService());
        this.exceptionsCommandHandler = new ExceptionsCommandHandler(context.exceptionInboxService());
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            statusCommandHandler.handle(sender);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case SUBCOMMAND_STATUS -> {
                statusCommandHandler.handle(sender);
                yield true;
            }
            case SUBCOMMAND_LISTENERS -> {
                listenersCommandHandler.handle(sender, args);
                yield true;
            }
            case SUBCOMMAND_PLUGIN -> {
                pluginCommandHandler.handle(sender, args);
                yield true;
            }
            case SUBCOMMAND_TRACE -> {
                traceCommandHandler.handle(sender, args);
                yield true;
            }
            case SUBCOMMAND_INSTRUMENTATION -> {
                instrumentationCommandHandler.handle(sender, args);
                yield true;
            }
            case SUBCOMMAND_EVENTS -> {
                eventsCommandHandler.handle(sender, args);
                yield true;
            }
            case SUBCOMMAND_EXCEPTIONS -> {
                exceptionsCommandHandler.handle(sender, args);
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return EventLensCommandTabs.complete(
                sender,
                args,
                new EventLensCommandTabs.Handlers(
                        listenersCommandHandler,
                        eventsCommandHandler,
                        exceptionsCommandHandler,
                        pluginCommandHandler,
                        traceCommandHandler,
                        instrumentationCommandHandler));
    }
}
