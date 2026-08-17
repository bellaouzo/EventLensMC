package dev.bellaouzo.eventlens.modcommon.command;

import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatSpan;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class ModCommandNotices {

    private static final Set<String> TOAST_SUBCOMMANDS =
            Set.of("export", "start", "stop", "restart", "pause", "resume");

    private ModCommandNotices() {}

    public static Optional<String> toastMessage(List<String> args, List<ModChatLine> lines) {
        if (args.size() < 2 || !"trace".equalsIgnoreCase(args.getFirst())) {
            return Optional.empty();
        }
        if (!TOAST_SUBCOMMANDS.contains(args.get(1).toLowerCase(Locale.ROOT))) {
            return Optional.empty();
        }
        for (ModChatLine line : lines) {
            StringBuilder text = new StringBuilder();
            for (ModChatSpan span : line.spans()) {
                text.append(span.text());
            }
            String message = text.toString().trim();
            if (message.isBlank() || isPathLine(message)) {
                continue;
            }
            return Optional.of(message);
        }
        return Optional.empty();
    }

    private static boolean isPathLine(String message) {
        return message.startsWith("Saved to") || message.startsWith("Folder:");
    }
}
