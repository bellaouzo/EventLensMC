package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.ExportCommandService;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import java.util.Locale;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class TraceExportCommandHandler {

    private static final String SESSION_NOT_FOUND_PREFIX = "No trace session \"";

    private TraceExportCommandHandler() {}

    static void handleExport(CommandSender sender, ExportCommandService exportCommandService, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace export <session> [--format json|ndjson|text|html] [--shareable|--full]",
                    NamedTextColor.YELLOW));
            return;
        }

        TraceExportOptionsParser.Result parseResult = TraceExportOptionsParser.parse(args, 3);
        Optional<String> parseError = parseResult.errorMessage();
        if (parseError.isPresent()) {
            sender.sendMessage(Component.text(parseError.get(), NamedTextColor.RED));
            return;
        }

        TraceExportOptionsParser.Parsed options = parseResult.parsed().orElseThrow();
        if (!ensureExportPermission(sender, options)) {
            return;
        }

        ExportCommandService.ExportResult result =
                exportCommandService.exportSession(args[2], options.format(), options.redactionMode());
        switch (result) {
            case ExportCommandService.ExportResult.Success(var path, var fileName, var format, var redaction) -> {
                sender.sendMessage(Component.text(
                        "Exported " + format.name().toLowerCase(Locale.ROOT) + " report (" + redaction + "): "
                                + fileName,
                        NamedTextColor.GREEN));
                ExportPathMessages.sendSavedPath(sender, path);
                if (fileName.endsWith("-bundle.zip")) {
                    sender.sendMessage(Component.text(
                            "Download that zip from the host file manager, unzip it, and open index.html"
                                    + " on your computer.",
                            NamedTextColor.GRAY));
                }
            }
            case ExportCommandService.ExportResult.Failure failure ->
                sender.sendMessage(Component.text(failure.message(), NamedTextColor.RED));
        }
    }

    static void handleCopy(CommandSender sender, ExportCommandService exportCommandService, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace copy <session> [--dispatch <n>] [--shareable|--full]",
                    NamedTextColor.YELLOW));
            return;
        }

        TraceExportOptionsParser.Result parseResult = TraceExportOptionsParser.parse(args, 3);
        Optional<String> parseError = parseResult.errorMessage();
        if (parseError.isPresent()) {
            sender.sendMessage(Component.text(parseError.get(), NamedTextColor.RED));
            return;
        }

        TraceExportOptionsParser.Parsed options = parseResult.parsed().orElseThrow();
        if (!ensureExportPermission(sender, options)) {
            return;
        }

        TraceExportOptionReaders.CopyDispatchOptionResult dispatchOptionResult =
                TraceExportOptionReaders.parseCopyDispatchOption(args, 3);
        Optional<String> dispatchOptionError = dispatchOptionResult.errorMessage();
        if (dispatchOptionError.isPresent()) {
            sender.sendMessage(Component.text(dispatchOptionError.orElseThrow(), NamedTextColor.RED));
            return;
        }

        Optional<Long> dispatchSequence = dispatchOptionResult.dispatchSequence();
        if (dispatchSequence.isPresent()) {
            ExportCommandService.CopyDispatchResult dispatchResult = exportCommandService.compactDispatchReport(
                    args[2], dispatchSequence.get(), options.redactionMode());
            switch (dispatchResult) {
                case ExportCommandService.CopyDispatchResult.Success(var report) -> {
                    sender.sendMessage(Component.text(
                            "Dispatch #" + dispatchSequence.get() + " report ready (" + options.redactionMode() + ").",
                            NamedTextColor.GOLD));
                    sender.sendMessage(Component.text(report, NamedTextColor.GRAY));
                    sender.sendMessage(Component.text("[Click to copy dispatch report]", NamedTextColor.AQUA)
                            .clickEvent(ClickEvent.copyToClipboard(report)));
                }
                case ExportCommandService.CopyDispatchResult.SessionNotFound(var sessionId) ->
                    sender.sendMessage(
                            Component.text(SESSION_NOT_FOUND_PREFIX + sessionId + "\".", NamedTextColor.RED));
                case ExportCommandService.CopyDispatchResult.DispatchNotFound(var sequence) ->
                    sender.sendMessage(
                            Component.text("No dispatch #" + sequence + " in that trace session.", NamedTextColor.RED));
            }
            return;
        }

        Optional<String> compact = exportCommandService.compactReport(args[2], options.redactionMode());
        if (compact.isEmpty()) {
            sender.sendMessage(Component.text(SESSION_NOT_FOUND_PREFIX + args[2] + "\".", NamedTextColor.RED));
            return;
        }
        String report = compact.get();
        sender.sendMessage(
                Component.text("Compact report ready (" + options.redactionMode() + ").", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(report, NamedTextColor.GRAY));
        sender.sendMessage(Component.text("[Click to copy compact report]", NamedTextColor.AQUA)
                .clickEvent(ClickEvent.copyToClipboard(report)));
    }

    static void handleCompare(CommandSender sender, ExportCommandService exportCommandService, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace compare <sessionA> <sessionB> [--plugin <name>] [--shareable|--full]",
                    NamedTextColor.YELLOW));
            return;
        }

        TraceExportOptionsParser.Result parseResult = TraceExportOptionsParser.parse(args, 4);
        Optional<String> parseError = parseResult.errorMessage();
        if (parseError.isPresent()) {
            sender.sendMessage(Component.text(parseError.get(), NamedTextColor.RED));
            return;
        }

        TraceExportOptionsParser.Parsed options = parseResult.parsed().orElseThrow();
        if (!ensureExportPermission(sender, options)) {
            return;
        }

        TraceExportOptionReaders.ComparePluginScopeResult pluginScopeResult =
                TraceExportOptionReaders.parseComparePluginScope(args, 4);
        Optional<String> pluginScopeError = pluginScopeResult.errorMessage();
        if (pluginScopeError.isPresent()) {
            sender.sendMessage(Component.text(pluginScopeError.orElseThrow(), NamedTextColor.RED));
            return;
        }

        ExportCommandService.CompareResult result = exportCommandService.compareSessions(
                args[2], args[3], options.redactionMode(), pluginScopeResult.pluginScope());
        switch (result) {
            case ExportCommandService.CompareResult.Success(var comparison) ->
                TracePluginCompareFormatter.render(sender, comparison);
            case ExportCommandService.CompareResult.LeftNotFound(var sessionId) ->
                sender.sendMessage(Component.text(SESSION_NOT_FOUND_PREFIX + sessionId + "\".", NamedTextColor.RED));
            case ExportCommandService.CompareResult.RightNotFound(var sessionId) ->
                sender.sendMessage(Component.text(SESSION_NOT_FOUND_PREFIX + sessionId + "\".", NamedTextColor.RED));
            case ExportCommandService.CompareResult.PluginNotFound(var pluginName) ->
                sender.sendMessage(Component.text(
                        "Plugin \"" + pluginName + "\" was not observed in either session.", NamedTextColor.RED));
        }
    }

    private static boolean ensureExportPermission(CommandSender sender, TraceExportOptionsParser.Parsed options) {
        if (options.redactionMode() == ExportRedactionMode.FULL
                && !EventLensPermissions.has(sender, EventLensPermissions.TRACE_EXPORT_FULL)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            sender.sendMessage(Component.text(
                    "Full (unredacted) exports require " + EventLensPermissions.TRACE_EXPORT_FULL
                            + ". Use --shareable for a redacted export.",
                    NamedTextColor.YELLOW));
            return false;
        }
        return true;
    }
}
