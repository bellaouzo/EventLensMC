package dev.bellaouzo.eventlens.observability;

import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public record ListenerObservation(
        int invocationOrder,
        @NonNull String pluginName,
        @NonNull String listenerClassName,
        @NonNull String methodName,
        @NonNull String priority,
        long durationNanos,
        boolean mainThread,
        Optional<String> stackTrace,
        boolean threwException,
        Optional<String> exceptionType,
        Optional<CompactEventSnapshot> snapshotBefore,
        Optional<CompactEventSnapshot> snapshotAfter) {

    public ListenerObservation {
        pluginName = Objects.requireNonNullElse(pluginName, "unknown");
        listenerClassName = Objects.requireNonNullElse(listenerClassName, "unknown");
        methodName = Objects.requireNonNullElse(methodName, "<unknown>");
        priority = Objects.requireNonNullElse(priority, "UNKNOWN");
        stackTrace = stackTrace == null ? Optional.empty() : stackTrace;
        exceptionType = exceptionType == null ? Optional.empty() : exceptionType;
        snapshotBefore = snapshotBefore == null ? Optional.empty() : snapshotBefore;
        snapshotAfter = snapshotAfter == null ? Optional.empty() : snapshotAfter;
    }
}
