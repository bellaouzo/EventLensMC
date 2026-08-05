package dev.bellaouzo.eventlens.trace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TraceSessionManagerTest {

    @Test
    void startsWithTracingDisabledAndNoSessions() {
        TraceSessionManager manager = new TraceSessionManager();

        assertFalse(manager.isTracingEnabled());
        assertEquals(0, manager.getActiveSessionCount());
    }

    @Test
    void closeAllResetsState() {
        TraceSessionManager manager = new TraceSessionManager();

        manager.closeAll();

        assertFalse(manager.isTracingEnabled());
        assertEquals(0, manager.getActiveSessionCount());
    }
}
