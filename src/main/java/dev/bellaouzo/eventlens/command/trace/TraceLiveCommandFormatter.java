package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.domain.live.LiveFeedCommandResult;
import dev.bellaouzo.eventlens.domain.live.LiveFeedSubscription;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TraceLiveCommandFormatter {

    private static final String NO_SUBSCRIPTION = "No active live feed subscription.";

    private TraceLiveCommandFormatter() {}

    static void renderSubscribed(CommandSender sender, LiveFeedSubscription subscription, boolean updated) {
        sender.sendMessage(Component.text(
                (updated ? "Updated live feed for session " : "Live feed attached to session ")
                        + subscription.sessionId()
                        + " (display="
                        + subscription.settings().displayMode().name().toLowerCase(Locale.ROOT)
                        + ", paused="
                        + subscription.settings().paused()
                        + ")",
                NamedTextColor.GREEN));
        sender.sendMessage(Component.text(
                "Channels: " + subscription.settings().channels()
                        + ". Use live pause, live resume, --filter-plugin, or live stop without restarting.",
                NamedTextColor.GRAY));
    }

    static void renderStop(CommandSender sender, LiveFeedCommandResult result) {
        if (result instanceof LiveFeedCommandResult.Unsubscribed(var sessionId)) {
            sender.sendMessage(
                    Component.text("Live feed stopped for session " + sessionId + ".", NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text(NO_SUBSCRIPTION, NamedTextColor.YELLOW));
    }

    static void renderUpdate(CommandSender sender, LiveFeedCommandResult result) {
        if (result instanceof LiveFeedCommandResult.Updated(var subscription)) {
            sender.sendMessage(Component.text(
                    "Live feed "
                            + (subscription.settings().paused() ? "paused" : "resumed")
                            + " for session "
                            + subscription.sessionId()
                            + ".",
                    NamedTextColor.GREEN));
            return;
        }
        sender.sendMessage(Component.text(NO_SUBSCRIPTION, NamedTextColor.YELLOW));
    }

    static void renderStatus(CommandSender sender, LiveFeedCommandResult result) {
        if (result instanceof LiveFeedCommandResult.Status(var subscription)) {
            sender.sendMessage(Component.text(
                    "Live feed: session "
                            + subscription.sessionId()
                            + ", display="
                            + subscription.settings().displayMode()
                            + ", paused="
                            + subscription.settings().paused()
                            + ", channels="
                            + subscription.settings().channels(),
                    NamedTextColor.AQUA));
            return;
        }
        sender.sendMessage(Component.text(NO_SUBSCRIPTION, NamedTextColor.YELLOW));
    }

    static void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text(
                "Usage: /eventlens trace live <session> [--channels frequency,slow,cancel,exception,alert] [--display chat|actionbar|bossbar] [--filter-plugin <name>] [--threshold 1ms] [--burst 50] [--aggregate 3s]",
                NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(
                "       /eventlens trace live status|stop|pause|resume  (control without a session id)",
                NamedTextColor.YELLOW));
        sender.sendMessage(Component.text(
                "       /eventlens trace live [--filter-plugin <name>]  (update filters on current subscription)",
                NamedTextColor.YELLOW));
    }
}
