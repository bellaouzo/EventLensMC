package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.TraceCorrelateService;
import dev.bellaouzo.eventlens.command.EventLensPermissions;
import dev.bellaouzo.eventlens.domain.correlation.CorrelationPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TraceCorrelateCommandHandler {

    static final String SUBCOMMAND = "correlate";

    private TraceCorrelateCommandHandler() {}

    static void handle(CommandSender sender, String[] args, TraceCorrelateService correlateService) {
        if (!EventLensPermissions.hasTrace(sender, "export")) {
            sender.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(Component.text(
                    "Usage: /eventlens trace correlate <serverSession> <clientSession>", NamedTextColor.YELLOW));
            return;
        }
        TraceCorrelateService.CorrelateResult result = correlateService.correlate(args[2], args[3]);
        if (!result.found()) {
            sender.sendMessage(
                    Component.text("No trace session \"" + result.missingSessionId() + "\".", NamedTextColor.RED));
            return;
        }
        sender.sendMessage(
                Component.text("Linked " + result.pairs().size() + " dispatch pair(s).", NamedTextColor.GREEN));
        int shown = 0;
        for (CorrelationPair pair : result.pairs()) {
            if (shown++ >= 8) {
                break;
            }
            sender.sendMessage(
                    Component.text("#" + pair.leftSequence() + " <-> #" + pair.rightSequence(), NamedTextColor.GRAY));
        }
    }
}
