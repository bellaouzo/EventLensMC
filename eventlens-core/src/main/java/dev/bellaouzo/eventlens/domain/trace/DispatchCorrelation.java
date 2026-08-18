package dev.bellaouzo.eventlens.domain.trace;

import java.util.Optional;

public record DispatchCorrelation(
        Optional<String> correlationKey,
        Optional<String> actionKind,
        Optional<String> peerSessionId,
        Optional<Long> peerSequence) {

    public static DispatchCorrelation empty() {
        return new DispatchCorrelation(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public DispatchCorrelation withPeer(String sessionId, long sequence) {
        return new DispatchCorrelation(correlationKey, actionKind, Optional.of(sessionId), Optional.of(sequence));
    }

    public boolean linked() {
        return peerSessionId.isPresent();
    }
}
