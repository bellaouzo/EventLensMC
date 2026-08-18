package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import org.junit.jupiter.api.Test;

class TraceCorrelateHelloTest {

    @Test
    void missingSessionFailsClosed() {
        TraceCorrelateService service = new TraceCorrelateService(new TraceSessionManager(), null);
        assertTrue(service.acceptHello("client01", 1L, "USE_BLOCK|p|w|1,2|10").isEmpty());
    }
}
