package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.command.CommandMessages;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.trace.TraceViewResult;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TraceViewCommandHandler {

    private TraceViewCommandHandler() {}

    static void handle(
            CommandSender sender,
            String[] args,
            TraceCommandService traceCommandService,
            EventLensCommandConfig commandConfig) {
        if (!EventLensPermissions.hasTrace(sender, "view")) {
            sender.sendMessage(Component.text(CommandMessages.PERMISSION_DENIED, NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace view <session> [page] [--run <n>] [--unchanged] "
                            + "[--detail brief|normal|verbose] [--dispatch <n>] [--plugin <name>] "
                            + "[--changed] [--slow] [--conflict]",
                    NamedTextColor.YELLOW));
            return;
        }
        TraceViewRunParser.Result runResult = TraceViewRunParser.extract(args);
        Optional<String> runError = runResult.errorMessage();
        if (runError.isPresent()) {
            sender.sendMessage(Component.text(runError.orElseThrow(), NamedTextColor.RED));
            return;
        }
        String[] viewArgs = runResult.args().orElseThrow();
        TraceViewOptionsParser.Result parseResult =
                TraceViewOptionsParser.parse(viewArgs, commandConfig.defaultDetailLevel());
        Optional<String> parseError = parseResult.errorMessage();
        if (parseError.isPresent()) {
            sender.sendMessage(Component.text(parseError.orElseThrow(), NamedTextColor.RED));
            return;
        }
        TraceViewOptionsParser.Parsed parsed = parseResult.parsed().orElseThrow();
        TraceViewResult result = traceCommandService.viewSession(
                viewArgs[2], parsed.page(), parsed.includeUnchanged(), parsed.filter(), runResult.generation());
        switch (result) {
            case TraceViewResult.NotFound(var sessionId) ->
                sender.sendMessage(Component.text("No trace session \"" + sessionId + "\".", NamedTextColor.RED));
            case TraceViewResult.InvalidPage(var requestedPage, var totalPages) ->
                sender.sendMessage(Component.text(
                        "Page " + requestedPage + " is out of range (1-" + totalPages + ").", NamedTextColor.RED));
            case TraceViewResult.Success success -> TraceViewFormatter.render(sender, success, parsed.detailLevel());
        }
    }
}
