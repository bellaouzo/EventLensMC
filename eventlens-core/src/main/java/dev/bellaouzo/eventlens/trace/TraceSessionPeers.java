package dev.bellaouzo.eventlens.trace;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.List;
import java.util.Optional;

final class TraceSessionPeers {

    private TraceSessionPeers() {}

    static Optional<TraceDispatchRecord> stampOpen(
            TraceSession session, String correlationKey, String peerSessionId, long peerSequence) {
        return session == null ? Optional.empty() : session.stampPeer(correlationKey, peerSessionId, peerSequence);
    }

    static Optional<TraceDispatchRecord> stamp(
            List<TraceDispatchRecord> records, String correlationKey, String peerSessionId, long peerSequence) {
        for (int index = 0; index < records.size(); index++) {
            TraceDispatchRecord dispatch = records.get(index);
            if (dispatch.correlation()
                    .correlationKey()
                    .filter(correlationKey::equals)
                    .isPresent()) {
                TraceDispatchRecord linked =
                        dispatch.withCorrelation(dispatch.correlation().withPeer(peerSessionId, peerSequence));
                records.set(index, linked);
                return Optional.of(linked);
            }
        }
        return Optional.empty();
    }
}
