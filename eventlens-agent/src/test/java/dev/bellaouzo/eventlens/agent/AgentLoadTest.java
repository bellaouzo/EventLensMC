package dev.bellaouzo.eventlens.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class AgentLoadTest {

    @Test
    void agentJarSetsLoadedFlagInForkedJvm() throws Exception {
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .anyMatch(argument -> argument.startsWith("-javaagent:"))) {
            assertTrue(AgentRuntime.isAgentLoaded());
            return;
        }

        File agentJar = resolveAgentJar();
        ProcessBuilder builder = new ProcessBuilder(
                System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
                "-javaagent:" + agentJar.getAbsolutePath(),
                "-cp",
                System.getProperty("java.class.path"),
                "dev.bellaouzo.eventlens.agent.AgentLoadTest");
        Process process = builder.start();
        assertTrue(process.waitFor(30, TimeUnit.SECONDS), "agent forked JVM timed out");
        assertEquals(0, process.waitFor(), "agent forked JVM failed: " + new String(process.getErrorStream().readAllBytes()));
    }

    public static void main(String[] args) {
        if (!AgentRuntime.isAgentLoaded()) {
            throw new IllegalStateException("agent did not mark itself loaded");
        }
    }

    private static File resolveAgentJar() {
        File localBuild = new File("build/libs");
        File[] localJars =
                localBuild.listFiles((dir, name) -> name.startsWith("eventlens-agent") && name.endsWith(".jar"));
        if (localJars != null && localJars.length > 0) {
            return localJars[0];
        }

        File rootBuild = new File("../eventlens-agent/build/libs");
        File[] rootJars =
                rootBuild.listFiles((dir, name) -> name.startsWith("eventlens-agent") && name.endsWith(".jar"));
        if (rootJars == null || rootJars.length == 0) {
            throw new IllegalStateException("Agent jar not found under eventlens-agent/build/libs");
        }
        return rootJars[0];
    }
}
