package dev.bellaouzo.eventlens.command.listeners;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.ListenerQueryService;
import dev.bellaouzo.eventlens.command.ChatPagination;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.DetailLevelParser;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.command.trace.TraceConflictFormatter;
import dev.bellaouzo.eventlens.domain.conflict.DispatchConflict;
import dev.bellaouzo.eventlens.domain.listener.ListenerInventoryPage;
import dev.bellaouzo.eventlens.domain.listener.ListenerInventoryResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class ListenersCommandHandler {

    public static final String PERMISSION = EventLensPermissions.LISTENERS;

    private final ListenerQueryService listenerQueryService;
    private final EventLensCommandConfig commandConfig;

    public ListenersCommandHandler(ListenerQueryService listenerQueryService, EventLensCommandConfig commandConfig) {
        this.listenerQueryService = listenerQueryService;
        this.commandConfig = commandConfig;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!EventLensPermissions.has(sender, PERMISSION)) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens listeners <event> [page] [--detail brief|normal|verbose]",
                    NamedTextColor.YELLOW));
            return;
        }

        DetailLevelParser.PageArgs pageArgs;
        try {
            pageArgs = DetailLevelParser.parseListenersArgs(args, commandConfig);
        } catch (IllegalArgumentException _) {
            sender.sendMessage(Component.text("Page must be a positive integer.", NamedTextColor.RED));
            return;
        }

        ListenerInventoryResult result = listenerQueryService.queryListeners(args[1], pageArgs.page());
        switch (result) {
            case ListenerInventoryResult.NotFound(var query) -> {
                sender.sendMessage(Component.text("No event matches \"" + query + "\".", NamedTextColor.RED));
                sender.sendMessage(Component.text(
                        "Try a partial name like PlayerJoin or a full class name.", NamedTextColor.GRAY));
            }
            case ListenerInventoryResult.Ambiguous(var candidateClassNames) -> {
                sender.sendMessage(Component.text("Multiple events match that query:", NamedTextColor.YELLOW));
                for (String candidate : candidateClassNames) {
                    ListenerUiFormatter.renderAmbiguousCandidate(sender, candidate);
                }
                sender.sendMessage(
                        Component.text("Refine your query or click a candidate above.", NamedTextColor.GRAY));
            }
            case ListenerInventoryResult.InvalidPage(var requestedPage, var totalPages) ->
                sender.sendMessage(Component.text(
                        "Page " + requestedPage + " is out of range (1-" + totalPages + ").", NamedTextColor.RED));
            case ListenerInventoryResult.Success(var inventoryPage) ->
                renderPage(sender, inventoryPage, listenerQueryService, pageArgs.detailLevel());
        }
    }

    public List<String> tabCompleteEventNames(String prefix) {
        return CommandText.filterPrefix(listenerQueryService.listKnownEventSimpleNames(), prefix);
    }

    private static void renderPage(
            CommandSender sender,
            ListenerInventoryPage page,
            ListenerQueryService listenerQueryService,
            OutputDetailLevel detailLevel) {
        ListenerUiFormatter.renderHeader(sender, page.eventClassName(), detailLevel);
        sender.sendMessage(Component.text(
                "Listeners (page " + page.page() + "/" + page.totalPages() + ", " + page.totalListeners() + " total):",
                NamedTextColor.GRAY));

        if (page.page() == 1 && detailLevel != OutputDetailLevel.BRIEF) {
            List<DispatchConflict> inventoryConflicts = listenerQueryService.inventoryConflicts(page.eventClassName());
            TraceConflictFormatter.renderInventoryConflicts(sender, inventoryConflicts);
        }

        if (page.listeners().isEmpty()) {
            sender.sendMessage(Component.text("No listeners registered for this event.", NamedTextColor.GRAY));
            return;
        }

        String eventSimpleName = CommandText.simpleName(page.eventClassName());
        for (ListenerRegistration listener : page.listeners()) {
            ListenerUiFormatter.renderListener(sender, listener, eventSimpleName, detailLevel);
        }

        ChatPagination.sendNavigation(
                sender,
                page.page(),
                page.totalPages(),
                page.page() > 1 ? "/eventlens listeners " + eventSimpleName + " " + (page.page() - 1) : null,
                page.page() < page.totalPages()
                        ? "/eventlens listeners " + eventSimpleName + " " + (page.page() + 1)
                        : null);
    }
}
