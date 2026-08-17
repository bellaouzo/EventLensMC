package dev.bellaouzo.eventlens.domain.conflict;

import dev.bellaouzo.eventlens.domain.diff.BandChange;
import dev.bellaouzo.eventlens.domain.diff.CancellationTransitionKind;
import dev.bellaouzo.eventlens.domain.observability.DurationStats;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class ListenerTimingConflictRules {

    private static final int SLOW_CHAIN_LISTENER_THRESHOLD = 3;
    private static final double SLOW_CHAIN_TOTAL_MULTIPLIER = 3.0;

    private ListenerTimingConflictRules() {}

    static List<DispatchConflict> detectPostCancelListeners(
            TraceDispatchRecord dispatch, List<BandChange> bandChanges) {
        Optional<String> cancelBand = bandChanges.stream()
                .filter(bandChange -> bandChange
                        .diff()
                        .cancellationTransition()
                        .map(transition -> transition.kind() == CancellationTransitionKind.BECAME_CANCELLED)
                        .orElse(false))
                .map(BandChange::priorityBand)
                .findFirst();

        if (cancelBand.isEmpty()) {
            return List.of();
        }

        int cancelIndex = ConflictDetectionSupport.priorityIndex(cancelBand.get());
        Map<String, TraceListenerSnapshot> chainByKey = ConflictDetectionSupport.indexChain(dispatch.listenerChain());
        List<DispatchConflict> conflicts = new ArrayList<>();

        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            TraceListenerSnapshot snapshot = chainByKey.get(ConflictDetectionSupport.listenerKey(timing));
            if (snapshot == null || snapshot.ignoreCancelled()) {
                continue;
            }
            if (ConflictDetectionSupport.priorityIndex(timing.priority()) > cancelIndex) {
                conflicts.add(new DispatchConflict(
                        ConflictKind.POST_CANCEL_LISTENER,
                        ConflictSeverity.HIGH,
                        snapshot.pluginName() + "/"
                                + ConflictDetectionSupport.simpleName(snapshot.listenerClassName()) + "#"
                                + snapshot.methodName()
                                + " ran at " + timing.priority() + " after cancellation was set at "
                                + cancelBand.get(),
                        List.of(snapshot.pluginName()),
                        Optional.of(dispatch.sequence())));
            }
        }

        return conflicts;
    }

    static List<DispatchConflict> detectListenerExceptions(TraceDispatchRecord dispatch) {
        List<DispatchConflict> conflicts = new ArrayList<>();
        for (ListenerTimingRecord timing : dispatch.listenerTimings()) {
            if (!timing.threwException()) {
                continue;
            }
            String exceptionHint = timing.exceptionType()
                    .map(type -> " (" + ConflictDetectionSupport.simpleName(type) + ")")
                    .orElse("");
            conflicts.add(new DispatchConflict(
                    ConflictKind.LISTENER_EXCEPTION,
                    ConflictSeverity.HIGH,
                    timing.pluginName() + "/"
                            + ConflictDetectionSupport.simpleName(timing.listenerClassName()) + "#"
                            + timing.methodName() + " threw an exception" + exceptionHint,
                    List.of(timing.pluginName()),
                    Optional.of(dispatch.sequence())));
        }
        return conflicts;
    }

    static List<DispatchConflict> detectSlowChain(TraceDispatchRecord dispatch, long slowThresholdNanos) {
        if (dispatch.listenerTimings().isEmpty()) {
            return List.of();
        }

        long slowThreshold = Math.max(slowThresholdNanos, 1L);
        List<ListenerTimingRecord> slowListeners = dispatch.listenerTimings().stream()
                .filter(timing -> timing.durationNanos() >= slowThreshold)
                .toList();
        long totalListenerNanos = dispatch.listenerTimings().stream()
                .mapToLong(ListenerTimingRecord::durationNanos)
                .sum();

        boolean unusuallySlow = slowListeners.size() >= SLOW_CHAIN_LISTENER_THRESHOLD
                || totalListenerNanos >= slowThreshold * SLOW_CHAIN_LISTENER_THRESHOLD
                || dispatch.durationNanos() >= slowThreshold * SLOW_CHAIN_TOTAL_MULTIPLIER;

        if (!unusuallySlow) {
            return List.of();
        }

        Set<String> plugins = new LinkedHashSet<>();
        slowListeners.forEach(timing -> plugins.add(timing.pluginName()));

        return List.of(new DispatchConflict(
                ConflictKind.SLOW_LISTENER_CHAIN,
                ConflictSeverity.MEDIUM,
                slowListeners.size() + " slow listener(s); chain total "
                        + DurationStats.formatMillis(totalListenerNanos) + " (dispatch "
                        + DurationStats.formatMillis(dispatch.durationNanos()) + ")",
                List.copyOf(plugins),
                Optional.of(dispatch.sequence())));
    }
}
