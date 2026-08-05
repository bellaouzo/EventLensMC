package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.instrumentation.InstrumentationMode;
import dev.bellaouzo.eventlens.paper.instrumentation.NoOpInstrumentationAdapter;
import org.junit.jupiter.api.Test;

class InstrumentationQueryServiceTest {

    @Test
    void dispatchOnlyWhenAgentAbsent() {
        InstrumentationQueryService service =
                new InstrumentationQueryService(new NoOpInstrumentationAdapter(), "Paper 26.2");

        var diagnostics = service.query("Paper 1.21", "1.21.1");

        assertEquals(InstrumentationMode.DISPATCH_ONLY, diagnostics.mode());
        assertFalse(diagnostics.capabilities().perListenerDuration());
        assertTrue(diagnostics.capabilities().priorityBandFallback());
    }

    @Test
    void detectsPaperVersionMismatch() {
        InstrumentationQueryService service =
                new InstrumentationQueryService(new NoOpInstrumentationAdapter(), "Paper 26.2");

        var diagnostics = service.query("Paper 1.20", "1.20.6");

        assertFalse(diagnostics.paperVersionCompatible());
    }
}
