package dev.bellaouzo.eventlens.clientagent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ClientAgentLoadTest {

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
                "dev.bellaouzo.eventlens.clientagent.ClientAgentLoadTest");
        Process process = builder.start();
        assertTrue(process.waitFor(30, TimeUnit.SECONDS), "client agent forked JVM timed out");
        assertEquals(
                0,
                process.waitFor(),
                "client agent forked JVM failed: " + new String(process.getErrorStream().readAllBytes()));
    }

    public static void main(String[] args) {
        if (!AgentRuntime.isAgentLoaded()) {
            throw new IllegalStateException("client agent did not mark itself loaded");
        }
    }

    private static File resolveAgentJar() {
        File localBuild = new File("build/libs");
        File[] localJars = localBuild.listFiles(
                (dir, name) -> name.startsWith("eventlens-client-agent") && name.endsWith(".jar"));
        if (localJars != null && localJars.length > 0) {
            return localJars[0];
        }
        File rootBuild = new File("../eventlens-client-agent/build/libs");
        File[] rootJars = rootBuild.listFiles(
                (dir, name) -> name.startsWith("eventlens-client-agent") && name.endsWith(".jar"));
        if (rootJars == null || rootJars.length == 0) {
            throw new IllegalStateException("Client agent jar not found under eventlens-client-agent/build/libs");
        }
        return rootJars[0];
    }
}
