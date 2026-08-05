package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.ListenerChangeAnalyzer;
import dev.bellaouzo.eventlens.application.SessionConflictAnalyzer;
import dev.bellaouzo.eventlens.application.TraceDispatchAnalyzer;
import dev.bellaouzo.eventlens.domain.conflict.DispatchConflict;
import dev.bellaouzo.eventlens.domain.diff.BandChange;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.diff.SnapshotDiff;
import dev.bellaouzo.eventlens.domain.diff.TraceDispatchWarning;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

final class TraceDispatchDetailFormatter {

    private TraceDispatchDetailFormatter() {}

    static void renderDetails(
            CommandSender sender,
            TraceDispatchRecord dispatchRecord,
            boolean includeUnchanged,
            long slowThresholdNanos,
            OutputDetailLevel detailLevel) {
        SnapshotDiff overallDiff = TraceDispatchAnalyzer.overallDiff(dispatchRecord, includeUnchanged);
        renderCancellationSummary(sender, overallDiff);
        renderSnapshotSummary(sender, dispatchRecord, detailLevel);
        renderWarningsAndConflicts(sender, dispatchRecord, detailLevel, slowThresholdNanos);
        renderPropertyChanges(sender, overallDiff, includeUnchanged, detailLevel);
        renderCancellationTimeline(sender, dispatchRecord, detailLevel);
        renderBandChanges(sender, dispatchRecord, includeUnchanged, detailLevel);
    }

    private static void renderCancellationTimeline(
            CommandSender sender, TraceDispatchRecord dispatchRecord, OutputDetailLevel detailLevel) {
        if (detailLevel == OutputDetailLevel.BRIEF) {
            return;
        }
        var timeline = ListenerChangeAnalyzer.cancellationTimeline(dispatchRecord);
        if (timeline.isEmpty()) {
            return;
        }
        sender.sendMessage(Component.text("  Cancellation timeline:", NamedTextColor.RED));
        for (var entry : timeline) {
            sender.sendMessage(Component.text(
                    "    #" + entry.invocationOrder() + " " + entry.pluginName() + "/" + entry.listenerClassName()
                            + " -> " + entry.kind().name().toLowerCase(Locale.ROOT),
                    NamedTextColor.RED));
        }
    }

    private static void renderCancellationSummary(CommandSender sender, SnapshotDiff overallDiff) {
        String cancellationSummary = TraceDispatchAnalyzer.formatCancellationTransition(overallDiff);
        if (cancellationSummary != null) {
            sender.sendMessage(Component.text("  Cancellation: " + cancellationSummary, NamedTextColor.GRAY));
        }
    }

    private static void renderSnapshotSummary(
            CommandSender sender, TraceDispatchRecord dispatchRecord, OutputDetailLevel detailLevel) {
        if (detailLevel != OutputDetailLevel.VERBOSE) {
            return;
        }
        sender.sendMessage(Component.text(
                "  Snapshots: " + dispatchRecord.snapshotBefore().checkpoint()
                        + " (" + dispatchRecord.snapshotBefore().fields().size() + ")"
                        + " → "
                        + dispatchRecord.snapshotAfter().checkpoint()
                        + " (" + dispatchRecord.snapshotAfter().fields().size() + ")",
                NamedTextColor.GRAY));
    }

    private static void renderWarningsAndConflicts(
            CommandSender sender,
            TraceDispatchRecord dispatchRecord,
            OutputDetailLevel detailLevel,
            long slowThresholdNanos) {
        if (detailLevel == OutputDetailLevel.BRIEF) {
            return;
        }
        List<TraceDispatchWarning> warnings = TraceDispatchAnalyzer.warnings(dispatchRecord);
        if (!warnings.isEmpty()) {
            sender.sendMessage(Component.text("  Warnings (" + warnings.size() + "):", NamedTextColor.GOLD));
            for (TraceDispatchWarning warning : warnings) {
                sender.sendMessage(Component.text("    " + warning.code(), NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("      " + warning.message(), NamedTextColor.GRAY));
            }
        }

        List<DispatchConflict> conflicts =
                SessionConflictAnalyzer.detectForDispatch(dispatchRecord, slowThresholdNanos);
        TraceConflictFormatter.renderDispatchConflicts(sender, conflicts);
    }

    private static void renderPropertyChanges(
            CommandSender sender, SnapshotDiff overallDiff, boolean includeUnchanged, OutputDetailLevel detailLevel) {
        if (detailLevel != OutputDetailLevel.BRIEF && !overallDiff.changed().isEmpty()) {
            sender.sendMessage(Component.text("  Changes:", NamedTextColor.AQUA));
            for (PropertyChange change : overallDiff.changed()) {
                sender.sendMessage(Component.text(
                        "    " + TraceDispatchAnalyzer.formatPropertyChange(change), NamedTextColor.AQUA));
            }
        }

        if (detailLevel == OutputDetailLevel.VERBOSE
                && includeUnchanged
                && !overallDiff.unchanged().isEmpty()) {
            sender.sendMessage(Component.text("  Unchanged:", NamedTextColor.DARK_GRAY));
            for (PropertyChange change : overallDiff.unchanged()) {
                sender.sendMessage(Component.text(
                        "    " + TraceDispatchAnalyzer.formatPropertyChange(change), NamedTextColor.DARK_GRAY));
            }
        }
    }

    private static void renderBandChanges(
            CommandSender sender,
            TraceDispatchRecord dispatchRecord,
            boolean includeUnchanged,
            OutputDetailLevel detailLevel) {
        if (detailLevel == OutputDetailLevel.BRIEF) {
            return;
        }
        List<BandChange> bandChanges = TraceDispatchAnalyzer.bandChanges(dispatchRecord, includeUnchanged);
        if (bandChanges.isEmpty()) {
            return;
        }
        sender.sendMessage(Component.text("  Priority bands:", NamedTextColor.GOLD));
        for (BandChange bandChange : bandChanges) {
            renderBandChange(sender, bandChange);
        }
    }

    private static void renderBandChange(CommandSender sender, BandChange bandChange) {
        String attribution = bandChange.attributedPlugins().isEmpty()
                ? "unknown"
                : String.join(", ", bandChange.attributedPlugins());
        if (bandChange.conflictingAttribution()) {
            attribution = attribution + " [conflicting]";
        }
        sender.sendMessage(
                Component.text("    " + bandChange.priorityBand() + " · " + attribution, NamedTextColor.YELLOW));

        for (PropertyChange change : bandChange.diff().changed()) {
            sender.sendMessage(
                    Component.text("      " + TraceDispatchAnalyzer.formatPropertyChange(change), NamedTextColor.AQUA));
        }

        bandChange
                .diff()
                .cancellationTransition()
                .filter(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                .map(ignored -> TraceDispatchAnalyzer.formatCancellationTransition(bandChange.diff()))
                .ifPresent(bandCancellation -> sender.sendMessage(
                        Component.text("      Cancellation: " + bandCancellation, NamedTextColor.RED)));
    }
}
