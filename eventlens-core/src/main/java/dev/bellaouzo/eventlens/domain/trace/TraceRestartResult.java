package dev.bellaouzo.eventlens.domain.trace;

import java.util.Objects;
import org.jspecify.annotations.NonNull;

public sealed interface TraceRestartResult
        permits TraceRestartResult.Success,
                TraceRestartResult.NotFound,
                TraceRestartResult.StillOpen,
                TraceRestartResult.SessionLimit {

    record Success(@NonNull String sessionId, @NonNull String sourceSessionId, @NonNull String eventClassName)
            implements TraceRestartResult {
        public Success {
            Objects.requireNonNull(sessionId, "newSessionId");
            Objects.requireNonNull(sourceSessionId, "sourceSessionId");
            Objects.requireNonNull(eventClassName, "eventClassName");
        }
    }

    record NotFound(@NonNull String sessionId) implements TraceRestartResult {
        public NotFound {
            Objects.requireNonNull(sessionId, "missingSessionId");
        }
    }

    record StillOpen(@NonNull String sessionId, @NonNull TraceSessionState state) implements TraceRestartResult {
        public StillOpen {
            Objects.requireNonNull(sessionId, "openSessionId");
            Objects.requireNonNull(state, "state");
        }
    }

    record SessionLimit(@NonNull String message) implements TraceRestartResult {
        public SessionLimit {
            message = Objects.requireNonNullElse(message, "Concurrent session limit reached.");
        }
    }
}
