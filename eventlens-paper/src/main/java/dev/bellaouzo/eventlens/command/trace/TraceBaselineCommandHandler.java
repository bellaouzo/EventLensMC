package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.BaselineCommandService;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.report.ExportRedactionMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TraceBaselineCommandHandler {

    static final String SUBCOMMAND = "baseline";
    private static final String BASELINE_NOT_FOUND_PREFIX = "Baseline \"";
    private static final String BASELINE_NOT_FOUND_SUFFIX = "\" was not found.";

    private final BaselineCommandService baselineCommandService;

    TraceBaselineCommandHandler(BaselineCommandService baselineCommandService) {
        this.baselineCommandService = baselineCommandService;
    }

    void compareStoppedSession(CommandSender sender, String sessionId, String baselineName) {
        BaselineCommandService.CompareResult result =
                baselineCommandService.compareSession(sessionId, baselineName, Optional.empty());
        switch (result) {
            case BaselineCommandService.CompareResult.Success(var report) ->
                TracePluginCompareFormatter.render(sender, report);
            case BaselineCommandService.CompareResult.LeftNotFound(var missing) ->
                sender.sendMessage(Component.text("No trace session \"" + missing + "\".", NamedTextColor.RED));
            case BaselineCommandService.CompareResult.RightNotFound(var missing) ->
                sender.sendMessage(Component.text(
                        BASELINE_NOT_FOUND_PREFIX + missing + BASELINE_NOT_FOUND_SUFFIX, NamedTextColor.RED));
        }
    }

    void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, "export")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sendUsage(sender);
            return;
        }
        switch (args[2].toLowerCase(Locale.ROOT)) {
            case "save" -> handleSave(sender, args);
            case "list" -> handleList(sender);
            case "compare" -> handleCompare(sender, args);
            case "delete" -> handleDelete(sender, args);
            default -> sendUsage(sender);
        }
    }

    private void handleSave(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace baseline save <session> <name> [--shareable|--full]",
                    NamedTextColor.YELLOW));
            return;
        }
        ExportRedactionMode redactionMode = resolveRedactionMode(args, 5).orElse(ExportRedactionMode.SHARE_SAFE);
        BaselineCommandService.SaveResult result = baselineCommandService.save(args[3], args[4], redactionMode);
        switch (result) {
            case BaselineCommandService.SaveResult.Success(var path, var baselineName) ->
                sender.sendMessage(Component.text("Saved baseline \"" + baselineName + "\".", NamedTextColor.GREEN)
                        .append(Component.text(" " + path, NamedTextColor.GRAY)));
            case BaselineCommandService.SaveResult.SessionNotFound(var sessionId) ->
                sender.sendMessage(Component.text("No trace session \"" + sessionId + "\".", NamedTextColor.RED));
            case BaselineCommandService.SaveResult.Failure(var message) ->
                sender.sendMessage(Component.text(message, NamedTextColor.RED));
        }
    }

    private void handleList(CommandSender sender) {
        List<String> baselines = baselineCommandService.list();
        sender.sendMessage(Component.text("Baselines (" + baselines.size() + "):", NamedTextColor.GOLD));
        if (baselines.isEmpty()) {
            sender.sendMessage(Component.text("  none", NamedTextColor.GRAY));
            return;
        }
        for (String baseline : baselines) {
            sender.sendMessage(Component.text("  " + baseline, NamedTextColor.WHITE));
        }
    }

    private void handleCompare(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace baseline compare <left> <right> [--plugin <name>]",
                    NamedTextColor.YELLOW));
            return;
        }
        Optional<String> plugin = parsePluginFlag(args, 5);
        BaselineCommandService.CompareResult result = baselineCommandService.compare(args[3], args[4], plugin);
        switch (result) {
            case BaselineCommandService.CompareResult.Success(var report) ->
                TracePluginCompareFormatter.render(sender, report);
            case BaselineCommandService.CompareResult.LeftNotFound(var baselineName) ->
                sender.sendMessage(Component.text(
                        BASELINE_NOT_FOUND_PREFIX + baselineName + BASELINE_NOT_FOUND_SUFFIX, NamedTextColor.RED));
            case BaselineCommandService.CompareResult.RightNotFound(var baselineName) ->
                sender.sendMessage(Component.text(
                        BASELINE_NOT_FOUND_PREFIX + baselineName + BASELINE_NOT_FOUND_SUFFIX, NamedTextColor.RED));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /eventlens trace baseline delete <name>", NamedTextColor.YELLOW));
            return;
        }
        BaselineCommandService.DeleteResult result = baselineCommandService.delete(args[3]);
        switch (result) {
            case BaselineCommandService.DeleteResult.Success(var baselineName) ->
                sender.sendMessage(Component.text("Deleted baseline \"" + baselineName + "\".", NamedTextColor.GREEN));
            case BaselineCommandService.DeleteResult.NotFound(var baselineName) ->
                sender.sendMessage(Component.text(
                        BASELINE_NOT_FOUND_PREFIX + baselineName + BASELINE_NOT_FOUND_SUFFIX, NamedTextColor.RED));
            case BaselineCommandService.DeleteResult.Failure(var message) ->
                sender.sendMessage(Component.text(message, NamedTextColor.RED));
        }
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text(
                "Usage: /eventlens trace baseline <save|list|compare|delete> ...", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  save <session> <name> [--shareable|--full]", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  compare <left> <right> [--plugin <name>]", NamedTextColor.YELLOW));
    }

    private static Optional<ExportRedactionMode> resolveRedactionMode(String[] args, int fromIndex) {
        for (int index = fromIndex; index < args.length; index++) {
            String token = args[index].toLowerCase(Locale.ROOT);
            if (token.equals("--full")) {
                return Optional.of(ExportRedactionMode.FULL);
            }
            if (token.equals("--shareable") || token.equals("--redacted")) {
                return Optional.of(ExportRedactionMode.SHARE_SAFE);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> parsePluginFlag(String[] args, int fromIndex) {
        for (int index = fromIndex; index < args.length; index++) {
            String token = args[index];
            if (token.equalsIgnoreCase("--plugin") && index + 1 < args.length) {
                String value = args[index + 1].trim();
                if (!value.isEmpty()) {
                    return Optional.of(value);
                }
            }
            if (token.toLowerCase(Locale.ROOT).startsWith("--plugin=")) {
                String value = token.substring("--plugin=".length()).trim();
                if (!value.isEmpty()) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }
}
