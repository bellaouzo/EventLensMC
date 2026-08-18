package dev.bellaouzo.eventlens.modcommon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.DispatchCorrelation;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ModHudFindingsTest {

    @Test
    void statusBitsIncludeCancelAndLinked() {
        assertEquals("ok", ModHudFindings.statusBits(dispatch(false)));
        TraceDispatchRecord linked = dispatch(true)
                .withCorrelation(DispatchCorrelation.empty().withPeer("peer-1", 3L));
        assertEquals("cancelled  ·  linked", ModHudFindings.statusBits(linked));
    }

    @Test
    void exportPeerLabel() {
        assertEquals("Peer found", ModHudFindings.exportPeerLabel(true));
        assertEquals("No peer", ModHudFindings.exportPeerLabel(false));
    }

    private static TraceDispatchRecord dispatch(boolean cancelled) {
        EventSnapshot snapshot = new EventSnapshot("use", "DISPATCH", 1L, List.of());
        return new TraceDispatchRecord(
                1L,
                1L,
                1L,
                1_000_000L,
                0L,
                "use",
                true,
                true,
                false,
                cancelled,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                snapshot,
                snapshot,
                List.of(),
                List.of(),
                List.of(),
                EnumSet.noneOf(TracePartialReason.class));
    }
}
