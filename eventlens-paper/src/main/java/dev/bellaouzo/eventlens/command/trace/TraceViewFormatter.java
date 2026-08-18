package dev.bellaouzo.eventlens.command.trace;

import dev.bellaouzo.eventlens.application.DispatchViewFilter;
import dev.bellaouzo.eventlens.command.ChatPagination;
import dev.bellaouzo.eventlens.command.CommandText;
import dev.bellaouzo.eventlens.command.CommandUi;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.preferences.OutputDetailLevel;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionSummary;
import dev.bellaouzo.eventlens.domain.trace.TraceViewResult;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public final class TraceViewFormatter {

    private TraceViewFormatter() {}

    public static void render(CommandSender sender, TraceViewResult.Success success, OutputDetailLevel detailLevel) {
        TraceSessionSummary summary = success.detail().summary();
        sender.sendMessage(
                Component.text("Trace " + summary.sessionId() + " [" + summary.state() + "]", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(
                summary.eventClassNames().stream()
                        .map(CommandText::simpleName)
                        .reduce((left, right) -> left + ", " + right)
                        .orElse(CommandText.simpleName(summary.eventClassName())),
                NamedTextColor.GRAY));
        sender.sendMessage(Component.text(
                dev.bellaouzo.eventlens.domain.narrative.NarrativeBuilder.session(
                                success.detail().records())
                        .summary(),
                NamedTextColor.AQUA));
        sender.sendMessage(Component.text(
                summary.capturedEvents() + " captured · " + summary.droppedEvents() + " dropped · "
                        + summary.sampledOutEvents() + " sampled · page "
                        + success.page() + "/" + success.totalPages(),
                NamedTextColor.DARK_GRAY));
        if (success.filter().hasPredicates()) {
            sender.sendMessage(Component.text(
                    "Showing " + success.totalMatchedDispatches() + " of " + success.totalSessionDispatches()
                            + " dispatches (filters applied)",
                    NamedTextColor.DARK_GRAY));
        }

        if (detailLevel != OutputDetailLevel.BRIEF) {
            TraceTimingFormatter.renderSessionHeader(sender, summary, success.page());
            TraceConflictFormatter.renderSessionSummary(sender, summary.conflictSummary(), success.page());
        }

        if (success.detail().records().isEmpty()) {
            sender.sendMessage(Component.text("No captured dispatches yet.", NamedTextColor.GRAY));
            return;
        }

        boolean firstRecord = true;
        for (TraceDispatchRecord dispatchRecord : success.detail().records()) {
            if (!firstRecord) {
                sender.sendMessage(Component.empty());
            }
            firstRecord = false;
            renderDispatchRecord(
                    sender,
                    summary.sessionId(),
                    dispatchRecord,
                    success.includeUnchanged(),
                    summary.slowThresholdNanos(),
                    detailLevel);
        }

        ChatPagination.sendNavigation(
                sender,
                success.page(),
                success.totalPages(),
                success.page() > 1
                        ? traceViewCommand(
                                summary.sessionId(), success.page() - 1, success.includeUnchanged(), success.filter())
                        : null,
                success.page() < success.totalPages()
                        ? traceViewCommand(
                                summary.sessionId(), success.page() + 1, success.includeUnchanged(), success.filter())
                        : null);
    }

    private static String traceViewCommand(
            String sessionId, int page, boolean includeUnchanged, DispatchViewFilter filter) {
        String command = "/eventlens trace view " + sessionId + " " + page;
        List<String> tokens = new ArrayList<>();
        if (includeUnchanged) {
            tokens.add("--unchanged");
        }
        tokens.addAll(filter.toCommandTokens());
        if (tokens.isEmpty()) {
            return command;
        }
        return command + " " + String.join(" ", tokens);
    }

    private static void renderDispatchRecord(
            CommandSender sender,
            String sessionId,
            TraceDispatchRecord dispatchRecord,
            boolean includeUnchanged,
            long slowThresholdNanos,
            OutputDetailLevel detailLevel) {
        String dispatchOnlyCommand = "/eventlens trace view " + sessionId + " --dispatch " + dispatchRecord.sequence()
                + (includeUnchanged ? " --unchanged" : "");
        Component dispatchId = CommandUi.runCommand(
                "#" + dispatchRecord.sequence(), dispatchOnlyCommand, "View dispatch #" + dispatchRecord.sequence());
        Component summary = dispatchId.append(Component.text(
                " · " + dispatchRecord.listenerChain().size() + " listeners · "
                        + DurationStats.formatMillis(dispatchRecord.durationNanos()),
                NamedTextColor.YELLOW));
        sender.sendMessage(summary);
        if (dispatchRecord.correlation().linked()) {
            sender.sendMessage(Component.text(
                    "Linked "
                            + dispatchRecord.correlation().peerSessionId().orElse("?")
                            + " #"
                            + dispatchRecord
                                    .correlation()
                                    .peerSequence()
                                    .map(String::valueOf)
                                    .orElse("?"),
                    NamedTextColor.AQUA));
        }

        if (detailLevel != OutputDetailLevel.BRIEF) {
            TraceTimingFormatter.renderDispatchTiming(sender, dispatchRecord, slowThresholdNanos);
        }

        TraceDispatchDetailFormatter.renderDetails(
                sender, dispatchRecord, includeUnchanged, slowThresholdNanos, detailLevel);
    }
}
