package dev.bellaouzo.eventlens.domain.trace;

import java.util.Optional;

public record DispatchTickContext(
        Optional<Long> serverTick, Optional<Double> tps, Optional<Double> msptMillis, Optional<Long> clientTick) {

    public static DispatchTickContext empty() {
        return new DispatchTickContext(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static DispatchTickContext paper(long serverTick, double tps, double msptMillis) {
        return new DispatchTickContext(
                Optional.of(serverTick), Optional.of(tps), Optional.of(msptMillis), Optional.empty());
    }

    public static DispatchTickContext client(long clientTick) {
        return new DispatchTickContext(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(clientTick));
    }
}
