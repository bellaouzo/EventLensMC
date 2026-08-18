package dev.bellaouzo.eventlens.domain.narrative;

import java.util.List;

public record SessionNarrative(String summary, List<DispatchNarrative> dispatches) {

    public SessionNarrative {
        dispatches = dispatches == null ? List.of() : List.copyOf(dispatches);
    }
}
