package dev.bellaouzo.eventlens.modcommon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.bellaouzo.eventlens.domain.trace.TraceFilter;
import dev.bellaouzo.eventlens.domain.trace.TraceSessionConfig;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ModLocalPlayerStateDetectorTest {

    @Test
    void recordsJumpAndSlotAfterSeed() {
        TraceSessionManager sessions = new TraceSessionManager();
        String jumpId = sessions.startSession(
                new TraceSessionConfig(
                        SupportedModEventTypes.CLIENT_JUMP_EVENT,
                        TraceFilter.Builder.unrestricted().build(),
                        Optional.empty(),
                        Optional.of(8)),
                "Dev",
                System.currentTimeMillis());
        String slotId = sessions.startSession(
                new TraceSessionConfig(
                        SupportedModEventTypes.CLIENT_SELECTED_SLOT_EVENT,
                        TraceFilter.Builder.unrestricted().build(),
                        Optional.empty(),
                        Optional.of(8)),
                "Dev",
                System.currentTimeMillis());
        ModDispatchRecorder recorder = new ModDispatchRecorder(sessions);
        ModLocalPlayerStateDetector detector = new ModLocalPlayerStateDetector();
        Optional<String> player = Optional.of("Dev");
        Optional<String> world = Optional.of("overworld");

        detector.observe(recorder, sample(20.0f, 0, true, 0.0d), player, world);
        assertEquals(0, sessions.getSessionDetail(jumpId).orElseThrow().records().size());
        assertEquals(0, sessions.getSessionDetail(slotId).orElseThrow().records().size());

        detector.observe(recorder, sample(20.0f, 3, false, 0.42d), player, world);
        assertEquals(1, sessions.getSessionDetail(jumpId).orElseThrow().records().size());
        assertEquals(1, sessions.getSessionDetail(slotId).orElseThrow().records().size());
    }

    private static ModLocalPlayerStateDetector.Sample sample(float health, int slot, boolean onGround, double deltaY) {
        return new ModLocalPlayerStateDetector.Sample(
                health, 20, 300, 0, 0, slot, false, false, onGround, deltaY, false, false, false);
    }
}
