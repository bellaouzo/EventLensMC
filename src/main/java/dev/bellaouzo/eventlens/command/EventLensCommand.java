package dev.bellaouzo.eventlens.command;

import dev.bellaouzo.eventlens.application.EventLensCommandContext;
import dev.bellaouzo.eventlens.command.instrumentation.InstrumentationCommandHandler;
import dev.bellaouzo.eventlens.command.listeners.ListenersCommandHandler;
import dev.bellaouzo.eventlens.command.plugin.PluginCommandHandler;
import dev.bellaouzo.eventlens.command.status.StatusCommandHandler;
import dev.bellaouzo.eventlens.command.trace.TraceCommandHandler;
import java.util.ArrayList;
import java.util.Collections;
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

    private final StatusCommandHandler statusCommandHandler;
    private final ListenersCommandHandler listenersCommandHandler;
    private final PluginCommandHandler pluginCommandHandler;
    private final TraceCommandHandler traceCommandHandler;
    private final InstrumentationCommandHandler instrumentationCommandHandler;

    public EventLensCommand(EventLensCommandContext context) {
        this.statusCommandHandler = new StatusCommandHandler(context.statusQueryService(), context.commandConfig());
        this.listenersCommandHandler =
                new ListenersCommandHandler(context.listenerQueryService(), context.commandConfig());
        this.pluginCommandHandler = new PluginCommandHandler(context.pluginQueryService(), context.commandConfig());
        this.traceCommandHandler = new TraceCommandHandler(
                context.traceCommandService(),
                context.traceLiveFeedService(),
                context.exportCommandService(),
                context.baselineCommandService(),
                context.playerPreferencesService(),
                context.commandConfig(),
                context.liveFeedConfig());
        this.instrumentationCommandHandler = new InstrumentationCommandHandler(context.instrumentationTestService());
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
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
            default -> false;
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0 || args.length == 1) {
            String prefix = args.length == 0 ? "" : args[0];
            return completeRootSubcommands(sender, prefix);
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase(SUBCOMMAND_LISTENERS)
                && EventLensPermissions.has(sender, ListenersCommandHandler.PERMISSION)) {
            return listenersCommandHandler.tabCompleteEventNames(args[1]);
        }

        if (args.length >= 2
                && args[0].equalsIgnoreCase(SUBCOMMAND_PLUGIN)
                && EventLensPermissions.has(sender, PluginCommandHandler.PERMISSION)) {
            return pluginCommandHandler.tabComplete(args, args[args.length - 1]);
        }

        if (args.length >= 2
                && args[0].equalsIgnoreCase(SUBCOMMAND_TRACE)
                && EventLensPermissions.has(sender, TraceCommandHandler.PERMISSION)) {
            return traceCommandHandler.tabComplete(args, args[args.length - 1]);
        }

        if (args.length >= 2
                && args[0].equalsIgnoreCase(SUBCOMMAND_INSTRUMENTATION)
                && EventLensPermissions.has(sender, InstrumentationCommandHandler.PERMISSION)) {
            return instrumentationCommandHandler.tabComplete(args, args[args.length - 1]);
        }

        return Collections.emptyList();
    }

    private List<String> completeRootSubcommands(CommandSender sender, String prefix) {
        List<String> subcommands = new ArrayList<>();
        if (EventLensPermissions.has(sender, StatusCommandHandler.PERMISSION)) {
            subcommands.add(SUBCOMMAND_STATUS);
        }
        if (EventLensPermissions.has(sender, ListenersCommandHandler.PERMISSION)) {
            subcommands.add(SUBCOMMAND_LISTENERS);
        }
        if (EventLensPermissions.has(sender, PluginCommandHandler.PERMISSION)) {
            subcommands.add(SUBCOMMAND_PLUGIN);
        }
        if (EventLensPermissions.has(sender, TraceCommandHandler.PERMISSION)) {
            subcommands.add(SUBCOMMAND_TRACE);
        }
        if (EventLensPermissions.has(sender, InstrumentationCommandHandler.PERMISSION)) {
            subcommands.add(SUBCOMMAND_INSTRUMENTATION);
        }
        return CommandText.filterPrefix(subcommands, prefix);
    }
}
