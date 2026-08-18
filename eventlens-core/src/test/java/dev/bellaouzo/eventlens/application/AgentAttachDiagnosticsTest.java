package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentAttachDiagnosticsTest {

    @TempDir
    Path tempDir;

    @Test
    void explainsInstantCrashWhenNoJvmArgConfigured() {
        var result = AgentAttachDiagnostics.diagnose(AgentAttachDiagnostics.Role.CLIENT, false, List.of("-Xmx4G"));

        assertTrue(result.lines().stream().anyMatch(line -> line.message().contains("Instant crash")));
    }

    @Test
    void detectsMissingAbsoluteClientAgentPath() {
        var result = AgentAttachDiagnostics.diagnose(
                AgentAttachDiagnostics.Role.CLIENT,
                false,
                List.of("-javaagent:C:/missing/eventlens-client-agent-1.10.6-beta.jar"));

        assertTrue(result.lines().stream().anyMatch(line -> line.message().contains("missing file")));
        assertTrue(result.lines().stream().anyMatch(line -> line.message().contains("instant crash")));
    }

    @Test
    void warnsWhenPaperAgentUsedOnClient() {
        var result = AgentAttachDiagnostics.diagnose(
                AgentAttachDiagnostics.Role.CLIENT,
                false,
                List.of("-javaagent:C:/agents/eventlens-agent-1.10.6-beta.jar"));

        assertTrue(result.lines().stream().anyMatch(line -> line.message().contains("Paper server agent")));
    }

    @Test
    void warnsWhenJarExistsButAgentDidNotAttach() throws Exception {
        Path agentJar = tempDir.resolve("eventlens-agent-1.10.6-beta.jar");
        Files.writeString(agentJar, "placeholder");
        var result = AgentAttachDiagnostics.diagnose(
                AgentAttachDiagnostics.Role.PAPER, false, List.of("-javaagent:" + agentJar.toAbsolutePath()));

        assertTrue(result.lines().stream().anyMatch(line -> line.message().contains("exists at")));
        assertTrue(result.lines().stream().anyMatch(line -> line.message().contains("dispatch-only")));
    }

    @Test
    void returnsNoLinesWhenAgentAttached() {
        var result = AgentAttachDiagnostics.diagnose(
                AgentAttachDiagnostics.Role.CLIENT,
                true,
                List.of("-javaagent:C:/missing/eventlens-client-agent-1.10.6-beta.jar"));

        assertEquals(0, result.lines().size());
    }
}
