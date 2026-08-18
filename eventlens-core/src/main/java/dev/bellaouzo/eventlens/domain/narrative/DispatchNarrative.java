package dev.bellaouzo.eventlens.domain.narrative;

import java.util.List;
import java.util.Optional;

public record DispatchNarrative(
        String summary,
        Optional<String> cancelledBy,
        Optional<String> cancelledAtPriority,
        List<String> skippedAfterCancel,
        List<String> fieldChanges,
        Optional<String> threw,
        List<String> partialReasons) {

    public DispatchNarrative {
        skippedAfterCancel = skippedAfterCancel == null ? List.of() : List.copyOf(skippedAfterCancel);
        fieldChanges = fieldChanges == null ? List.of() : List.copyOf(fieldChanges);
        partialReasons = partialReasons == null ? List.of() : List.copyOf(partialReasons);
    }
}
