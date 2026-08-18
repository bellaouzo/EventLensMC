package dev.bellaouzo.eventlens.modcommon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ModLocalPlayerHurtDetectorTest {

    @Test
    void recordsHealthDropAfterSeed() {
        TraceSessionManager sessions = new TraceSessionManager();
        String sessionId = sessions.startSession(
                new TraceSessionConfig(
                        SupportedModEventTypes.CLIENT_HURT_EVENT,
                        TraceFilter.Builder.unrestricted().build(),
                        Optional.empty(),
                        Optional.of(8)),
                "Dev",
                System.currentTimeMillis());
        ModDispatchRecorder recorder = new ModDispatchRecorder(sessions);
        ModLocalPlayerHurtDetector detector = new ModLocalPlayerHurtDetector();

        detector.observe(recorder, 20.0f, 0, "unknown", Optional.of("Dev"), Optional.of("overworld"));
        assertEquals(0, sessions.getSessionDetail(sessionId).orElseThrow().records().size());

        detector.observe(recorder, 16.0f, 10, "fall", Optional.of("Dev"), Optional.of("overworld"));
        assertEquals(1, sessions.getSessionDetail(sessionId).orElseThrow().records().size());
        assertEquals(
                SupportedModEventTypes.CLIENT_HURT_EVENT,
                sessions.getSessionDetail(sessionId).orElseThrow().records().getFirst().eventClassName());
    }
}
