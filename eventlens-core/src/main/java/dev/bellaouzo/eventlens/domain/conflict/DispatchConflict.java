package dev.bellaouzo.eventlens.domain.conflict;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public record DispatchConflict(
        @NonNull ConflictKind kind,
        @NonNull ConflictSeverity severity,
        @NonNull String message,
        @NonNull List<String> involvedPlugins,
        Optional<Long> dispatchSequence) {

    public DispatchConflict {
        involvedPlugins = involvedPlugins == null ? List.of() : List.copyOf(involvedPlugins);
    }
}
