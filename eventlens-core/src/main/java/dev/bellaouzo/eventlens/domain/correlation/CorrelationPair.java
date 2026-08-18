package dev.bellaouzo.eventlens.domain.correlation;

public record CorrelationPair(
        String leftSessionId, long leftSequence, String rightSessionId, long rightSequence, String correlationKey) {}
