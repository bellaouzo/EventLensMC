package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.EventLensCommandContext;
import dev.bellaouzo.eventlens.application.ExportCommandService;
import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.application.TraceCorrelateService;
import dev.bellaouzo.eventlens.command.CommandLiterals;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.trace.TraceRestartResult;
import dev.bellaouzo.eventlens.domain.trace.TraceStopResult;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class TraceCommandHandler {

    public static final String PERMISSION = EventLensPermissions.TRACE;

    private final TraceCommandService traceCommandService;
    private final ExportCommandService exportCommandService;
    private final TraceStartCommandHandler traceStartCommandHandler;
    private final TracePreferenceCommandHandler preferenceCommandHandler;
    private final TraceLiveCommandHandler liveCommandHandler;
    private final TraceBaselineCommandHandler baselineCommandHandler;
    private final EventLensCommandConfig commandConfig;
    private final TraceCorrelateService correlateService;

    public TraceCommandHandler(EventLensCommandContext context) {
        this.traceCommandService = context.traceCommandService();
        this.exportCommandService = context.exportCommandService();
        this.commandConfig = context.commandConfig();
        this.traceStartCommandHandler = new TraceStartCommandHandler(
                context.traceCommandService(), context.commandConfig(), context.playerPreferencesService());
        this.preferenceCommandHandler = new TracePreferenceCommandHandler(context.playerPreferencesService());
        this.liveCommandHandler = new TraceLiveCommandHandler(
                context.traceLiveFeedService(), context.commandConfig(), context.liveFeedConfig());
        this.baselineCommandHandler = new TraceBaselineCommandHandler(context.baselineCommandService());
        this.correlateService = context.traceCorrelateService();
    }

    public void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.has(sender, PERMISSION)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace <start|stop|restart|list|view|live|export|copy|compare|correlate|baseline|history|favorite|presets>",
                    NamedTextColor.YELLOW));
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case TraceCommandTabCompleter.SUBCOMMAND_START -> traceStartCommandHandler.handle(sender, args);
            case "stop" -> handleStop(sender);
            case TraceCommandTabCompleter.SUBCOMMAND_RESTART -> handleRestart(sender, args);
            case "list" -> handleList(sender);
            case "view" -> handleView(sender, args);
            case TraceLiveCommandHandler.SUBCOMMAND -> liveCommandHandler.handle(sender, args);
            case CommandLiterals.SUBCOMMAND_EXPORT -> handleExport(sender, args);
            case "copy" -> handleCopy(sender, args);
            case "compare" -> handleCompare(sender, args);
            case TraceCorrelateCommandHandler.SUBCOMMAND ->
                TraceCorrelateCommandHandler.handle(sender, args, correlateService);
            case TraceBaselineCommandHandler.SUBCOMMAND -> baselineCommandHandler.handle(sender, args);
            case "history" -> preferenceCommandHandler.handleHistory(sender);
            case "favorite" -> preferenceCommandHandler.handleFavorite(sender, args);
            case "presets" -> preferenceCommandHandler.handlePresets(sender, commandConfig);
            default ->
                sender.sendMessage(Component.text(
                        "Usage: /eventlens trace <start|stop|restart|list|view|live|export|copy|compare|correlate|baseline|history|favorite|presets>",
                        NamedTextColor.YELLOW));
        }
    }

    public List<String> tabComplete(String[] args, String prefix) {
        return TraceCommandTabCompleter.complete(traceCommandService, args, prefix);
    }

    private void handleStop(CommandSender sender) {
        if (!EventLensPermissions.hasTrace(sender, "stop")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        TraceStopResult result = traceCommandService.stopTrace(sender.getName());
        switch (result) {
            case TraceStopResult.Success(var stoppedSessionIds) ->
                sender.sendMessage(Component.text(
                        "Stopped trace session(s): " + String.join(", ", stoppedSessionIds), NamedTextColor.GREEN));
            case TraceStopResult.NoActiveSessions _ ->
                sender.sendMessage(Component.text("No active trace sessions to stop.", NamedTextColor.YELLOW));
        }
    }

    private void handleRestart(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, TraceCommandTabCompleter.SUBCOMMAND_RESTART)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /eventlens trace restart <session>", NamedTextColor.YELLOW));
            return;
        }
        switch (traceCommandService.restartTrace(args[2])) {
            case TraceRestartResult.Success success ->
                sender.sendMessage(Component.text(
                        (success.restartCount() <= 1 ? "RESTARTED " : "RESTARTED ×" + success.restartCount() + " ")
                                + success.sessionId()
                                + ". Same session id; previous run kept as run "
                                + success.restartCount()
                                + ".",
                        NamedTextColor.GREEN));
            case TraceRestartResult.NotFound(var sessionId) ->
                sender.sendMessage(Component.text("No trace session \"" + sessionId + "\".", NamedTextColor.RED));
            case TraceRestartResult.StillOpen(var sessionId, var state) ->
                sender.sendMessage(Component.text(
                        "Session " + sessionId + " is still " + state + ". Use resume or stop first.",
                        NamedTextColor.YELLOW));
            case TraceRestartResult.SessionLimit(var message) ->
                sender.sendMessage(Component.text(message, NamedTextColor.RED));
        }
    }

    private void handleList(CommandSender sender) {
        if (!EventLensPermissions.hasTrace(sender, "list")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        TraceSessionListFormatter.render(sender, traceCommandService.listSessions(), commandConfig);
    }

    private void handleView(CommandSender sender, String[] args) {
        TraceViewCommandHandler.handle(sender, args, traceCommandService, commandConfig);
    }

    private void handleExport(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, CommandLiterals.SUBCOMMAND_EXPORT)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        TraceExportCommandHandler.handleExport(sender, exportCommandService, args);
    }

    private void handleCopy(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, CommandLiterals.SUBCOMMAND_EXPORT)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        TraceExportCommandHandler.handleCopy(sender, exportCommandService, args);
    }

    private void handleCompare(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, CommandLiterals.SUBCOMMAND_EXPORT)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        TraceExportCommandHandler.handleCompare(sender, exportCommandService, args);
    }
}
