package dev.bellaouzo.eventlens.command.plugin;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.PluginQueryService;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.command.DetailLevelParser;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.plugin.PluginCompareResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerQueryResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginQueryResult;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class PluginCommandHandler {

    public static final String PERMISSION = EventLensPermissions.PLUGIN;
    private static final String SUBCOMMAND_COMPARE = "compare";
    private static final String SUBCOMMAND_LISTENERS = "listeners";
    private static final String NO_LOADED_PLUGIN_PREFIX = "No loaded plugin matches \"";

    private final PluginQueryService pluginQueryService;
    private final EventLensCommandConfig commandConfig;

    public PluginCommandHandler(PluginQueryService pluginQueryService, EventLensCommandConfig commandConfig) {
        this.pluginQueryService = pluginQueryService;
        this.commandConfig = commandConfig;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.has(sender, PERMISSION)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return;
        }

        if (args[1].equalsIgnoreCase(SUBCOMMAND_COMPARE)) {
            handleCompare(sender, args);
            return;
        }

        if (args.length >= 3 && args[2].equalsIgnoreCase(SUBCOMMAND_LISTENERS)) {
            handleListeners(sender, args);
            return;
        }

        handleProfile(sender, args);
    }

    public List<String> tabComplete(String[] args, String prefix) {
        return PluginCommandTabCompleter.complete(pluginQueryService, args, prefix);
    }

    private void handleProfile(CommandSender sender, String[] args) {
        OutputDetailLevel detailLevel = DetailLevelParser.resolve(args, 2, commandConfig);
        PluginQueryResult result = pluginQueryService.queryProfile(args[1]);
        switch (result) {
            case PluginQueryResult.NotFound(var query) ->
                sender.sendMessage(Component.text(NO_LOADED_PLUGIN_PREFIX + query + "\".", NamedTextColor.RED));
            case PluginQueryResult.Ambiguous(var candidateNames) -> {
                sender.sendMessage(Component.text("Multiple plugins match that query:", NamedTextColor.YELLOW));
                for (String candidate : candidateNames) {
                    renderAmbiguousCandidate(sender, candidate);
                }
            }
            case PluginQueryResult.Success(var profile) ->
                PluginProfileFormatter.renderProfile(sender, profile, detailLevel);
        }
    }

    private void handleListeners(CommandSender sender, String[] args) {
        DetailLevelParser.PluginListenersPageArgs pageArgs;
        try {
            pageArgs = DetailLevelParser.parsePluginListenersArgs(args, commandConfig);
        } catch (IllegalArgumentException _) {
            sender.sendMessage(Component.text("Page must be a positive integer.", NamedTextColor.RED));
            return;
        }

        PluginListenerQueryResult result = pluginQueryService.queryListeners(
                args[1], pageArgs.eventQuery(), pageArgs.page(), PluginQueryService.DEFAULT_PAGE_SIZE);
        switch (result) {
            case PluginListenerQueryResult.PluginNotFound(var query) ->
                sender.sendMessage(Component.text(NO_LOADED_PLUGIN_PREFIX + query + "\".", NamedTextColor.RED));
            case PluginListenerQueryResult.PluginAmbiguous(var candidateNames) -> {
                sender.sendMessage(Component.text("Multiple plugins match that query:", NamedTextColor.YELLOW));
                for (String candidate : candidateNames) {
                    renderAmbiguousCandidate(sender, candidate);
                }
            }
            case PluginListenerQueryResult.EventNotFound(var missingEvent) ->
                sender.sendMessage(Component.text("No event matches \"" + missingEvent + "\".", NamedTextColor.RED));
            case PluginListenerQueryResult.EventAmbiguous(var candidateClassNames) -> {
                sender.sendMessage(Component.text("Multiple events match that query:", NamedTextColor.YELLOW));
                for (String candidate : candidateClassNames) {
                    sender.sendMessage(Component.text("  " + candidate, NamedTextColor.WHITE));
                }
            }
            case PluginListenerQueryResult.InvalidPage(var requestedPage, var totalPages) ->
                sender.sendMessage(Component.text(
                        "Page " + requestedPage + " is out of range (1-" + totalPages + ").", NamedTextColor.RED));
            case PluginListenerQueryResult.Success(var page) ->
                PluginListenerFormatter.renderPage(sender, page, pageArgs.detailLevel());
        }
    }

    private void handleCompare(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(
                    Component.text("Usage: /eventlens plugin compare <pluginA> <pluginB>", NamedTextColor.YELLOW));
            return;
        }

        PluginQueryResult leftResult = pluginQueryService.queryProfile(args[2]);
        PluginQueryResult rightResult = pluginQueryService.queryProfile(args[3]);
        if (leftResult instanceof PluginQueryResult.NotFound(var query)) {
            sender.sendMessage(Component.text(
                    NO_LOADED_PLUGIN_PREFIX + query + "\". Compare only works with installed plugins.",
                    NamedTextColor.RED));
            return;
        }
        if (leftResult instanceof PluginQueryResult.Ambiguous(var candidateNames)) {
            sender.sendMessage(Component.text("Multiple plugins match \"" + args[2] + "\":", NamedTextColor.YELLOW));
            candidateNames.forEach(name -> renderAmbiguousCandidate(sender, name));
            return;
        }
        if (rightResult instanceof PluginQueryResult.NotFound(var query)) {
            sender.sendMessage(Component.text(
                    NO_LOADED_PLUGIN_PREFIX + query + "\". Compare only works with installed plugins.",
                    NamedTextColor.RED));
            return;
        }
        if (rightResult instanceof PluginQueryResult.Ambiguous(var candidateNames)) {
            sender.sendMessage(Component.text("Multiple plugins match \"" + args[3] + "\":", NamedTextColor.YELLOW));
            candidateNames.forEach(name -> renderAmbiguousCandidate(sender, name));
            return;
        }

        java.util.Optional<PluginCompareResult> result = pluginQueryService.comparePlugins(args[2], args[3]);
        if (result.isEmpty()) {
            sender.sendMessage(Component.text("Could not compare those plugins.", NamedTextColor.RED));
            return;
        }

        PluginCompareFormatter.render(sender, result.get());
    }

    private void renderAmbiguousCandidate(CommandSender sender, String pluginName) {
        sender.sendMessage(CommandUi.runCommand(
                "  " + pluginName, "/eventlens plugin " + pluginName, "View plugin profile for " + pluginName));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text(
                "Usage: /eventlens plugin <plugin> [--detail brief|normal|verbose]", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(
                "       /eventlens plugin <plugin> listeners [event] [page] [--detail ...]", NamedTextColor.YELLOW));
        sender.sendMessage(
                Component.text("       /eventlens plugin compare <pluginA> <pluginB>", NamedTextColor.YELLOW));
    }
}
