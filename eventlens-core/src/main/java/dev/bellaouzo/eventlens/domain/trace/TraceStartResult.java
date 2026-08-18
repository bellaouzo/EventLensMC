package dev.bellaouzo.eventlens.domain.trace;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

public sealed interface TraceStartResult permits TraceStartResult.Success, TraceStartResult.Failure {

    record Success(@NonNull String sessionId, @NonNull String eventClassName, @NonNull List<String> eventClassNames)
            implements TraceStartResult {
        public Success(@NonNull String sessionId, @NonNull String eventClassName) {
            this(sessionId, eventClassName, List.of(eventClassName));
        }

        public Success {
            Objects.requireNonNull(sessionId, "sessionId");
            Objects.requireNonNull(eventClassName, "eventClassName");
            eventClassNames = eventClassNames == null || eventClassNames.isEmpty()
                    ? List.of(eventClassName)
                    : List.copyOf(eventClassNames);
        }
    }

    record Failure(@NonNull Reason reason, @NonNull String message, @NonNull Optional<String> confirmCommand)
            implements TraceStartResult {

        public Failure(@NonNull Reason reason, @NonNull String message) {
            this(reason, message, Optional.empty());
        }

        public Failure {
            Objects.requireNonNull(reason, "reason");
            message = Objects.requireNonNullElse(message, "Trace start failed.");
            confirmCommand = Objects.requireNonNullElse(confirmCommand, Optional.empty());
        }

        public enum Reason {
            EVENT_NOT_FOUND,
            EVENT_AMBIGUOUS,
            SESSION_LIMIT,
            UNSUPPORTED_EVENT,
            INVALID_OPTIONS,
            HOT_EVENT_CONFIRMATION
        }
    }
}
