package dev.bellaouzo.eventlens.domain.conflict;

import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

public record SessionConflictSummary(
        int dispatchesAnalyzed,
        int dispatchesWithConflicts,
        @NonNull String likelyConflictSummary,
        @NonNull Map<ConflictKind, Integer> countsByKind,
        @NonNull List<InvestigationTarget> investigationTargets,
        @NonNull List<String> suggestions) {

    public SessionConflictSummary {
        countsByKind = countsByKind == null ? Map.of() : Map.copyOf(countsByKind);
        investigationTargets = investigationTargets == null ? List.of() : List.copyOf(investigationTargets);
        suggestions = suggestions == null ? List.of() : List.copyOf(suggestions);
    }

    public static SessionConflictSummary empty() {
        return new SessionConflictSummary(0, 0, "No conflicts detected.", Map.of(), List.of(), List.of());
    }
}
