package dev.bellaouzo.eventlens.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JavaRuntimesTest {

    @Test
    void parsesLegacyAndModernSpecificationVersions() {
        assertEquals(8, JavaRuntimes.parseSpecification("1.8"));
        assertEquals(8, JavaRuntimes.parseSpecification("1.8.0"));
        assertEquals(21, JavaRuntimes.parseSpecification("21"));
        assertEquals(25, JavaRuntimes.parseSpecification("25.0.1"));
        assertEquals(0, JavaRuntimes.parseSpecification(""));
    }

    @Test
    void parsesJavaDashVersionOutput() {
        assertEquals(8, JavaRuntimes.parseVersionOutput("java version \"1.8.0_411\""));
        assertEquals(21, JavaRuntimes.parseVersionOutput("openjdk version \"21.0.5\" 2024-10-15"));
        assertEquals(25, JavaRuntimes.parseVersionOutput("openjdk version \"25\" 2025-09-16"));
    }

    @Test
    void currentToolchainIsNewEnoughForTheWizard() {
        assertTrue(JavaRuntimes.isNewEnough(JavaRuntimes.runtimeMajor()));
        assertFalse(JavaRuntimes.isNewEnough(8));
        assertTrue(JavaRuntimes.requiredRuntimeMessage().contains("OpenJDK Platform Binary"));
        assertTrue(JavaRuntimes.requiredRuntimeMessage().contains("Java SE Platform Binary"));
    }
}
