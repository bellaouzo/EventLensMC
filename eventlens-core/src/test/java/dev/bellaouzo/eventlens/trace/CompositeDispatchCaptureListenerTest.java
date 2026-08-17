package dev.bellaouzo.eventlens.trace;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompositeDispatchCaptureListenerTest {

    @Test
    void forwardsDispatchCaptureToAllCaptureListeners() {
        RecordingDispatchListener first = new RecordingDispatchListener();
        RecordingDispatchListener second = new RecordingDispatchListener();
        CompositeDispatchCaptureListener composite =
                new CompositeDispatchCaptureListener(List.of(first, second), List.of());

        TraceDispatchRecord dispatch = minimalDispatch();
        composite.onDispatchCaptured("sess-1", dispatch);

        assertEquals(List.of("sess-1"), first.sessionIds);
        assertEquals(List.of(dispatch), first.dispatches);
        assertEquals(List.of("sess-1"), second.sessionIds);
        assertEquals(List.of(dispatch), second.dispatches);
    }

    private static TraceDispatchRecord minimalDispatch() {
        return new TraceDispatchRecord(
                1L,
                1_000L,
                0L,
                500_000L,
                10_000L,
                "org.bukkit.event.player.PlayerInteractEvent",
                true,
                false,
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                Set.of());
    }

    private static final class RecordingDispatchListener implements DispatchCaptureListener {
        private final List<String> sessionIds = new ArrayList<>();
        private final List<TraceDispatchRecord> dispatches = new ArrayList<>();

        @Override
        public void onDispatchCaptured(String sessionId, TraceDispatchRecord dispatchRecord) {
            sessionIds.add(sessionId);
            dispatches.add(dispatchRecord);
        }
    }
}
