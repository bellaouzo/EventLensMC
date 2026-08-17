package dev.bellaouzo.eventlens.domain.trace;

import dev.bellaouzo.eventlens.domain.diff.CancellationTransition;
import dev.bellaouzo.eventlens.domain.diff.PropertyChange;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public record ListenerTimingRecord(
        int invocationOrder,
        @NonNull String pluginName,
        @NonNull String listenerClassName,
        @NonNull String methodName,
        @NonNull String priority,
        long durationNanos,
        boolean mainThread,
        boolean mainThreadBlocked,
        boolean exceedsSlowThreshold,
        Optional<String> stackTrace,
        boolean threwException,
        Optional<String> exceptionType,
        Optional<EventSnapshot> snapshotBefore,
        Optional<EventSnapshot> snapshotAfter,
        List<PropertyChange> propertyChanges,
        Optional<CancellationTransition> cancellationTransition) {

    public ListenerTimingRecord {
        propertyChanges = propertyChanges == null ? List.of() : List.copyOf(propertyChanges);
    }

    public static ListenerTimingRecord timingOnly(
            int invocationOrder,
            String pluginName,
            String listenerClassName,
            String methodName,
            String priority,
            long durationNanos,
            boolean mainThread,
            boolean mainThreadBlocked,
            boolean exceedsSlowThreshold,
            Optional<String> stackTrace,
            boolean threwException,
            Optional<String> exceptionType) {
        return new ListenerTimingRecord(
                invocationOrder,
                pluginName,
                listenerClassName,
                methodName,
                priority,
                durationNanos,
                mainThread,
                mainThreadBlocked,
                exceedsSlowThreshold,
                stackTrace,
                threwException,
                exceptionType,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                Optional.empty());
    }
}
