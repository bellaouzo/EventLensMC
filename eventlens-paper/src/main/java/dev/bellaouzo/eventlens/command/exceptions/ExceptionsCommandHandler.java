package dev.bellaouzo.eventlens.command.exceptions;

import dev.bellaouzo.eventlens.application.ExceptionInboxService;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.exceptions.ExceptionInboxEntry;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class ExceptionsCommandHandler {

    public static final String PERMISSION = EventLensPermissions.TRACE;

    private final ExceptionInboxService inboxService;

    public ExceptionsCommandHandler(ExceptionInboxService inboxService) {
        this.inboxService = inboxService;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.has(sender, PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
            return;
        }
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(Component.text("Page must be a number.", NamedTextColor.RED));
                return;
            }
        }
        int totalPages = inboxService.totalPages();
        page = Math.clamp(page, 1, totalPages);
        sender.sendMessage(Component.text(
                "Exceptions " + inboxService.size() + "  page " + page + "/" + totalPages, NamedTextColor.GOLD));
        List<ExceptionInboxEntry> entries = inboxService.page(page);
        if (entries.isEmpty()) {
            sender.sendMessage(Component.text("No attributed exceptions yet.", NamedTextColor.GRAY));
            return;
        }
        for (ExceptionInboxEntry entry : entries) {
            sender.sendMessage(Component.text(
                    entry.pluginName()
                            + "#"
                            + entry.methodName()
                            + "  "
                            + entry.exceptionType()
                            + "  "
                            + SupportedEventTypes.displaySimpleName(entry.eventClassName()),
                    NamedTextColor.YELLOW));
        }
    }

    public List<String> tabComplete(String prefix) {
        return CommandText.filterPrefix(List.of("1"), prefix);
    }
}
