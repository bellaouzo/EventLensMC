package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.preferences.RecentTraceEntry;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TraceHistoryFormatter {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private TraceHistoryFormatter() {}

    static void renderRecent(CommandSender sender, List<RecentTraceEntry> entries) {
        if (entries.isEmpty()) {
            sender.sendMessage(Component.text("No recent traces recorded for you.", NamedTextColor.GRAY));
            return;
        }

        sender.sendMessage(Component.text("Recent traces:", NamedTextColor.GOLD));
        for (RecentTraceEntry entry : entries) {
            String viewCommand = "/eventlens trace view " + entry.sessionId();
            sender.sendMessage(CommandUi.labeledLine(
                    TIME_FORMAT.format(Instant.ofEpochMilli(entry.startedAtMillis())),
                    CommandUi.runCommand(
                                    entry.eventSimpleName() + " · " + entry.sessionId(),
                                    viewCommand,
                                    CommandUi.hoverBlock(
                                            "Event: " + entry.eventSimpleName(),
                                            "Session: " + entry.sessionId(),
                                            "",
                                            "Click to view trace"))
                            .append(Component.text(" ", NamedTextColor.GRAY))
                            .append(CommandUi.runCommand(
                                    "[trace again]",
                                    "/eventlens trace start " + entry.eventSimpleName(),
                                    "Start a new trace for " + entry.eventSimpleName()))));
        }
    }

    static void renderFavorites(CommandSender sender, List<String> favorites) {
        if (favorites.isEmpty()) {
            sender.sendMessage(Component.text("No favorite events.", NamedTextColor.GRAY));
            sender.sendMessage(
                    Component.text("Add one with /eventlens trace favorite add <event>", NamedTextColor.DARK_GRAY));
            return;
        }

        sender.sendMessage(Component.text("Favorite events:", NamedTextColor.GOLD));
        for (String event : favorites) {
            sender.sendMessage(CommandUi.runCommand(
                            event,
                            "/eventlens trace start " + event,
                            CommandUi.hoverBlock(
                                    "Start tracing " + event, "", "[listeners] inspect registered listeners"))
                    .append(Component.text(" · ", NamedTextColor.DARK_GRAY))
                    .append(CommandUi.runCommand(
                            "[listeners]", "/eventlens listeners " + event, "Inspect listeners for " + event))
                    .append(Component.text(" · ", NamedTextColor.DARK_GRAY))
                    .append(CommandUi.runCommand(
                            "[remove]",
                            "/eventlens trace favorite remove " + event,
                            "Remove " + event + " from favorites")));
        }
    }
}
