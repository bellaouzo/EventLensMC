package dev.bellaouzo.eventlens.domain.listener;

import java.util.List;

public record EventSearchResult(
        EventSearchOutcome outcome, String resolvedEventClassName, List<String> candidateClassNames) {

    public static EventSearchResult found(String eventClassName) {
        return new EventSearchResult(EventSearchOutcome.FOUND, eventClassName, List.of());
    }

    public static EventSearchResult ambiguous(List<String> candidateClassNames) {
        return new EventSearchResult(EventSearchOutcome.AMBIGUOUS, null, List.copyOf(candidateClassNames));
    }

    public static EventSearchResult notFound() {
        return new EventSearchResult(EventSearchOutcome.NOT_FOUND, null, List.of());
    }
}
