package dev.bellaouzo.eventlens.clientagent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClientAgentTargetsTest {

    @Test
    void rejectsMissingInvokeSignature() {
        assertFalse(ClientAgentTargets.matches(WrongTarget.class, "java.lang.String"));
        assertTrue(ClientAgentTargets.matches(RightTarget.class, "java.lang.String"));
    }

    @Test
    void forgeAllowlistTargetsAsmEventHandler() {
        assertTrue(ClientAgentBus.FORGE.classes().contains("net.minecraftforge.eventbus.ASMEventHandler"));
        assertEquals("net.minecraftforge.eventbus.api.Event", ClientAgentBus.FORGE.eventClass());
        assertTrue(ClientAgentBus.FABRIC.classes().contains("net.fabricmc.fabric.impl.base.event.ArrayBackedEvent"));
        assertNull(ClientAgentBus.FABRIC.eventClass());
    }

    public static final class WrongTarget {
        public void invoke(Object event) {}
    }

    public static final class RightTarget {
        public void invoke(String event) {}
    }
}
