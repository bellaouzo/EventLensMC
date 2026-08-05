package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.domain.conflict.ConflictSeverity;
import dev.bellaouzo.eventlens.domain.conflict.DispatchConflict;
import dev.bellaouzo.eventlens.domain.conflict.InvestigationTarget;
import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class TraceConflictFormatter {

    private TraceConflictFormatter() {}

    public static void renderSessionSummary(CommandSender sender, SessionConflictSummary summary, int page) {
        if (page != 1 || summary.dispatchesAnalyzed() == 0) {
            return;
        }

        NamedTextColor headlineColor =
                summary.dispatchesWithConflicts() > 0 ? NamedTextColor.GOLD : NamedTextColor.GRAY;
        sender.sendMessage(Component.text("Conflicts: " + summary.likelyConflictSummary(), headlineColor));

        if (summary.dispatchesWithConflicts() == 0) {
            return;
        }

        if (!summary.investigationTargets().isEmpty()) {
            sender.sendMessage(Component.text("Investigate first:", NamedTextColor.YELLOW));
            for (InvestigationTarget target : summary.investigationTargets()) {
                sender.sendMessage(Component.text(
                        "  " + target.displayName() + " (" + target.occurrenceCount() + " · "
                                + target.maxSeverity().name().toLowerCase(java.util.Locale.ROOT) + ")",
                        severityColor(target.maxSeverity())));
            }
        }

        if (!summary.suggestions().isEmpty()) {
            sender.sendMessage(Component.text("Suggestions:", NamedTextColor.AQUA));
            for (String suggestion : summary.suggestions()) {
                sender.sendMessage(Component.text("  " + suggestion, NamedTextColor.GRAY));
            }
        }
    }

    public static void renderDispatchConflicts(CommandSender sender, List<DispatchConflict> conflicts) {
        if (conflicts.isEmpty()) {
            return;
        }

        sender.sendMessage(Component.text("  Conflicts (" + conflicts.size() + "):", NamedTextColor.GOLD));
        for (DispatchConflict conflict : conflicts) {
            sender.sendMessage(Component.text(
                    "    [" + conflict.kind().name() + "] " + conflict.message(), severityColor(conflict.severity())));
        }
    }

    public static void renderInventoryConflicts(CommandSender sender, List<DispatchConflict> conflicts) {
        if (conflicts.isEmpty()) {
            return;
        }

        sender.sendMessage(Component.text("Registration issues (" + conflicts.size() + "):", NamedTextColor.GOLD));
        for (DispatchConflict conflict : conflicts) {
            sender.sendMessage(Component.text(
                    "  [" + conflict.kind().name() + "] " + conflict.message(), severityColor(conflict.severity())));
        }
    }

    private static NamedTextColor severityColor(ConflictSeverity severity) {
        return switch (severity) {
            case HIGH -> NamedTextColor.RED;
            case MEDIUM -> NamedTextColor.YELLOW;
            case LOW -> NamedTextColor.GRAY;
        };
    }
}
