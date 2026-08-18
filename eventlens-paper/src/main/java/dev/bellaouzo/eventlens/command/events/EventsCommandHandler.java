package dev.bellaouzo.eventlens.command.events;

import dev.bellaouzo.eventlens.application.EventCatalogService;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class EventsCommandHandler {

    public static final String PERMISSION = EventLensPermissions.LISTENERS;

    private final EventCatalogService catalogService;

    public EventsCommandHandler(EventCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.has(sender, PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
            return;
        }
        String prefix = args.length >= 2 ? args[1] : "";
        List<EventCatalogService.EventCatalogEntry> entries = catalogService.list(prefix);
        sender.sendMessage(Component.text("Registered events (" + entries.size() + ")", NamedTextColor.GOLD));
        int shown = 0;
        for (EventCatalogService.EventCatalogEntry entry : entries) {
            if (shown++ >= 20) {
                sender.sendMessage(
                        Component.text("… truncated. Filter with /eventlens events <text>", NamedTextColor.DARK_GRAY));
                break;
            }
            sender.sendMessage(
                    Component.text(entry.simpleName() + "  " + entry.coverage(), coverageColor(entry.coverage())));
        }
    }

    public List<String> tabComplete(String prefix) {
        return CommandText.filterPrefix(
                catalogService.list("").stream()
                        .map(EventCatalogService.EventCatalogEntry::simpleName)
                        .toList(),
                prefix);
    }

    private static NamedTextColor coverageColor(String coverage) {
        return switch (coverage) {
            case "hot" -> NamedTextColor.YELLOW;
            case "traceable" -> NamedTextColor.GREEN;
            default -> NamedTextColor.GRAY;
        };
    }
}
