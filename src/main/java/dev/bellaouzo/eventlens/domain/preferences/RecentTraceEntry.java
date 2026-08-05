package dev.bellaouzo.eventlens.domain.preferences;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

public record RecentTraceEntry(@NonNull String sessionId, @NonNull String eventSimpleName, long startedAtMillis) {

    public RecentTraceEntry {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(eventSimpleName, "eventSimpleName");
    }
}
