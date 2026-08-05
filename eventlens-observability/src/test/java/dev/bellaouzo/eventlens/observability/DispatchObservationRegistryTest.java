package dev.bellaouzo.eventlens.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DispatchObservationRegistryTest {

    @Test
    void recordsAndFinishesListenerObservations() {
        long eventKey = 42L;
        DispatchObservationRegistry.beginDispatch(eventKey);
        DispatchObservationRegistry.recordListener(
                eventKey, "PluginA", "com.example.Listener", "onEvent", "NORMAL", 1_000_000L, true, null, null, null, null);

        List<ListenerObservation> observations = DispatchObservationRegistry.finishDispatch(eventKey);

        assertEquals(1, observations.size());
        assertEquals("PluginA", observations.getFirst().pluginName());
        assertEquals(1_000_000L, observations.getFirst().durationNanos());
    }
}
