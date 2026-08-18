package dev.bellaouzo.eventlens.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SetupPathsTest {

    @TempDir
    Path temp;

    @Test
    void paperAgentDefaultsBesideServer() {
        Path plugins = temp.resolve("plugins");
        assertEquals(temp.resolve("eventlens-agents"), SetupPaths.defaultAgentDirectory(SetupTarget.PAPER, plugins));
    }

    @Test
    void payloadNamesMatchReleaseArtifacts() {
        assertEquals("EventLens-1.12.0.jar", SetupTarget.PAPER.payloadFileName("1.12.0"));
        assertEquals("eventlens-agent-1.12.0.jar", SetupTarget.PAPER.agentFileName("1.12.0"));
        assertEquals("eventlens-fabric-1.12.0.jar", SetupTarget.FABRIC.payloadFileName("1.12.0"));
        assertEquals("eventlens-client-agent-1.12.0.jar", SetupTarget.FORGE.agentFileName("1.12.0"));
    }

    @Test
    void installRequestRequiresAgentFolderWhenEnabled() {
        Path destination = temp;
        assertThrows(
                IllegalArgumentException.class,
                () -> new InstallRequest(SetupTarget.PAPER, destination, true, null, "1.12.0"));
    }
}
