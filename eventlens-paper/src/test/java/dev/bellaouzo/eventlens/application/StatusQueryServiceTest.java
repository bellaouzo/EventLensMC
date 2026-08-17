package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.InstrumentationTestPort;
import dev.bellaouzo.eventlens.domain.status.EventLensStatus;
import dev.bellaouzo.eventlens.paper.instrumentation.NoOpInstrumentationAdapter;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class StatusQueryServiceTest {

    @Test
    void queryStatusReflectsSessionManagerDefaults() {
        TraceSessionManager manager = new TraceSessionManager();
        InstrumentationPort instrumentationPort = new NoOpInstrumentationAdapter();
        InstrumentationTestPort testPort = new InstrumentationTestPort() {
            @Override
            public Optional<String> resolveAgentArgument() {
                return Optional.empty();
            }

            @Override
            public boolean canResolveAgentJar() {
                return false;
            }
        };
        StatusQueryService service =
                new StatusQueryService(manager, instrumentationPort, testPort, "0.1-SNAPSHOT", "Paper 26.2");

        EventLensStatus status = service.queryStatus("Paper 1.21", "1.21.1");

        assertEquals("0.1-SNAPSHOT", status.version());
        assertEquals("Paper 26.2", status.targetPlatform());
        assertFalse(status.tracingEnabled());
        assertEquals(0, status.activeSessionCount());
        assertFalse(status.agentAttached());
        assertEquals("DISPATCH_ONLY", status.timingMode());
    }
}
