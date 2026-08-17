package dev.bellaouzo.eventlens.trace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

final class TraceSessionControl {

    private TraceSessionControl() {}

    static List<String> changeOwner(
            Collection<TraceSession> sessions,
            String ownerName,
            long nowMillis,
            BiPredicate<TraceSession, Long> change,
            Consumer<String> afterEach) {
        List<String> changed = new ArrayList<>();
        for (TraceSession session : sessions) {
            if (session.getOwnerName().equalsIgnoreCase(ownerName) && change.test(session, nowMillis)) {
                afterEach.accept(session.getSessionId());
                changed.add(session.getSessionId());
            }
        }
        return changed;
    }

    static Optional<String> changeOne(TraceSession session, long nowMillis, BiPredicate<TraceSession, Long> change) {
        if (session == null || !change.test(session, nowMillis)) {
            return Optional.empty();
        }
        return Optional.of(session.getSessionId());
    }
}
