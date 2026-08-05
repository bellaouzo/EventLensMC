package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.LiveFeedConfig;
import dev.bellaouzo.eventlens.application.TraceLiveFeedService;
import dev.bellaouzo.eventlens.application.TraceLiveOptionsParser;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.live.LiveFeedCommandResult;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TraceLiveCommandHandler {

    public static final String SUBCOMMAND = "live";
    public static final String SUBCOMMAND_STATUS = "status";
    public static final String SUBCOMMAND_STOP = "stop";
    public static final String SUBCOMMAND_PAUSE = "pause";
    public static final String SUBCOMMAND_RESUME = "resume";
    public static final String PERMISSION = EventLensPermissions.TRACE_LIVE;
    private static final List<String> CONTROL_SUBCOMMANDS =
            List.of(SUBCOMMAND_STATUS, SUBCOMMAND_STOP, SUBCOMMAND_PAUSE, SUBCOMMAND_RESUME);

    private final TraceLiveFeedService liveFeedService;
    private final EventLensCommandConfig commandConfig;
    private final LiveFeedConfig liveFeedConfig;

    public TraceLiveCommandHandler(
            TraceLiveFeedService liveFeedService, EventLensCommandConfig commandConfig, LiveFeedConfig liveFeedConfig) {
        this.liveFeedService = liveFeedService;
        this.commandConfig = commandConfig;
        this.liveFeedConfig = liveFeedConfig;
    }

    public static List<String> controlSubcommands() {
        return CONTROL_SUBCOMMANDS;
    }

    public static boolean isControlSubcommand(String token) {
        return CONTROL_SUBCOMMANDS.stream().anyMatch(name -> name.equalsIgnoreCase(token));
    }

    public void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.hasTrace(sender, "live")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Live feed requires an in-game player.", NamedTextColor.RED));
            return;
        }

        if (handleControlSubcommand(sender, args)) {
            return;
        }

        if (args.length < 3) {
            TraceLiveCommandFormatter.sendUsage(sender);
            return;
        }

        TraceLiveOptionsParser.ParsedLiveOptions options =
                TraceLiveOptionsParser.parse(args, liveFeedConfig, commandConfig.defaultSlowThresholdNanos());

        if (options.stop()) {
            TraceLiveCommandFormatter.renderStop(sender, liveFeedService.unsubscribe(sender.getName()));
            return;
        }

        if (options.pauseOverride() != null) {
            TraceLiveCommandFormatter.renderUpdate(
                    sender, liveFeedService.pause(sender.getName(), options.pauseOverride()));
            return;
        }

        if (options.sessionId() == null && tryFilterUpdate(sender, player, options)) {
            return;
        }

        if (options.sessionId() == null) {
            TraceLiveCommandFormatter.sendUsage(sender);
            return;
        }

        handleSubscribe(sender, player, options);
    }

    private boolean handleControlSubcommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            return false;
        }
        String viewerName = sender.getName();
        if (args[2].equalsIgnoreCase(SUBCOMMAND_STATUS)) {
            TraceLiveCommandFormatter.renderStatus(sender, liveFeedService.status(viewerName));
            return true;
        }
        if (args[2].equalsIgnoreCase(SUBCOMMAND_STOP)) {
            TraceLiveCommandFormatter.renderStop(sender, liveFeedService.unsubscribe(viewerName));
            return true;
        }
        if (args[2].equalsIgnoreCase(SUBCOMMAND_PAUSE)) {
            TraceLiveCommandFormatter.renderUpdate(sender, liveFeedService.pause(viewerName, true));
            return true;
        }
        if (args[2].equalsIgnoreCase(SUBCOMMAND_RESUME)) {
            TraceLiveCommandFormatter.renderUpdate(sender, liveFeedService.pause(viewerName, false));
            return true;
        }
        return false;
    }

    private boolean tryFilterUpdate(
            CommandSender sender, Player player, TraceLiveOptionsParser.ParsedLiveOptions options) {
        LiveFeedCommandResult current = liveFeedService.status(sender.getName());
        if (!(current instanceof LiveFeedCommandResult.Status(var existing))) {
            return false;
        }
        LiveFeedCommandResult result = liveFeedService.subscribe(
                sender.getName(), player.getUniqueId(), existing.sessionId(), options.settings());
        if (result instanceof LiveFeedCommandResult.Updated(var subscription)) {
            TraceLiveCommandFormatter.renderSubscribed(sender, subscription, true);
            return true;
        }
        sender.sendMessage(Component.text("Could not update live feed filters.", NamedTextColor.RED));
        return true;
    }

    private void handleSubscribe(
            CommandSender sender, Player player, TraceLiveOptionsParser.ParsedLiveOptions options) {
        LiveFeedCommandResult result = liveFeedService.subscribe(
                sender.getName(), player.getUniqueId(), options.sessionId(), options.settings());
        switch (result) {
            case LiveFeedCommandResult.Subscribed(var subscription) ->
                TraceLiveCommandFormatter.renderSubscribed(sender, subscription, false);
            case LiveFeedCommandResult.Updated(var subscription) ->
                TraceLiveCommandFormatter.renderSubscribed(sender, subscription, true);
            case LiveFeedCommandResult.NotFound(var sessionId) ->
                sender.sendMessage(Component.text("No trace session \"" + sessionId + "\".", NamedTextColor.RED));
            case LiveFeedCommandResult.Failure(var message) ->
                sender.sendMessage(Component.text(message, NamedTextColor.RED));
            default -> sender.sendMessage(Component.text("Unexpected live feed result.", NamedTextColor.RED));
        }
    }
}
