package dev.bellaouzo.eventlens.domain.narrative;

import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NarrativeBuilder {

    private NarrativeBuilder() {}

    public static SessionNarrative session(List<TraceDispatchRecord> records) {
        List<DispatchNarrative> narratives =
                records.stream().map(NarrativeBuilder::dispatch).toList();
        if (narratives.isEmpty()) {
            return new SessionNarrative("No dispatches captured.", List.of());
        }
        long cancelled =
                records.stream().filter(TraceDispatchRecord::cancelledAtEnd).count();
        long threw = records.stream()
                .filter(dispatch -> dispatch.listenerTimings().stream().anyMatch(ListenerTimingRecord::threwException))
                .count();
        return new SessionNarrative(
                records.size() + " dispatch(es); " + cancelled + " cancelled; " + threw + " with exceptions.",
                narratives);
    }

    public static DispatchNarrative dispatch(TraceDispatchRecord dispatch) {
        Optional<ListenerTimingRecord> canceller = findCanceller(dispatch);
        List<String> skipped = skippedAfter(dispatch, canceller);
        List<String> changes = fieldChanges(dispatch);
        Optional<ListenerTimingRecord> thrown = dispatch.listenerTimings().stream()
                .filter(ListenerTimingRecord::threwException)
                .findFirst();
        List<String> partials =
                dispatch.partialReasons().stream().map(TracePartialReason::name).toList();
        return new DispatchNarrative(
                buildSummary(dispatch, canceller, thrown, partials),
                canceller.map(timing -> timing.pluginName() + "#" + timing.methodName()),
                canceller.map(ListenerTimingRecord::priority),
                skipped,
                changes,
                thrown.map(timing -> timing.pluginName()
                        + "#"
                        + timing.methodName()
                        + " threw "
                        + timing.exceptionType().orElse("Exception")),
                partials);
    }

    private static String buildSummary(
            TraceDispatchRecord dispatch,
            Optional<ListenerTimingRecord> canceller,
            Optional<ListenerTimingRecord> thrown,
            List<String> partials) {
        StringBuilder text = new StringBuilder();
        if (canceller.isPresent()) {
            ListenerTimingRecord timing = canceller.get();
            text.append(timing.pluginName()).append(" cancelled at ").append(timing.priority());
        } else if (dispatch.cancelledAtEnd()) {
            text.append("Cancelled");
        } else {
            text.append("Completed");
        }
        thrown.ifPresent(timing -> text.append("; ")
                .append(timing.pluginName())
                .append(" threw ")
                .append(timing.exceptionType().orElse("Exception")));
        if (!partials.isEmpty()) {
            text.append(" (").append(String.join(", ", partials)).append(')');
        }
        return text.toString();
    }

    private static Optional<ListenerTimingRecord> findCanceller(TraceDispatchRecord dispatch) {
        return dispatch.listenerTimings().stream()
                .filter(timing -> timing.cancellationTransition()
                        .map(transition -> !transition.before() && transition.after())
                        .orElse(false))
                .findFirst();
    }

    private static List<String> skippedAfter(TraceDispatchRecord dispatch, Optional<ListenerTimingRecord> canceller) {
        if (canceller.isEmpty() || dispatch.listenerChain().isEmpty()) {
            return List.of();
        }
        String plugin = canceller.get().pluginName();
        String method = canceller.get().methodName();
        boolean after = false;
        List<String> skipped = new ArrayList<>();
        for (TraceListenerSnapshot listener : dispatch.listenerChain()) {
            if (after) {
                skipped.add(listener.pluginName() + "#" + listener.methodName());
            }
            if (listener.pluginName().equals(plugin) && listener.methodName().equals(method)) {
                after = true;
            }
        }
        return List.copyOf(skipped);
    }

    private static List<String> fieldChanges(TraceDispatchRecord dispatch) {
        return dispatch.listenerTimings().stream()
                .flatMap(timing -> timing.propertyChanges().stream().map(change -> formatChange(timing, change)))
                .toList();
    }

    private static String formatChange(ListenerTimingRecord timing, PropertyChange change) {
        return timing.pluginName()
                + " changed "
                + change.property()
                + " "
                + display(change.before())
                + " -> "
                + display(change.after());
    }

    private static String display(dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue value) {
        if (value instanceof dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue.Present present) {
            return present.display();
        }
        return "?";
    }
}
