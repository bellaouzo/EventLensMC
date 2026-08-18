package dev.bellaouzo.eventlens.observability;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.jar.JarFile;

public final class ObservabilityBootstrap {

    private ObservabilityBootstrap() {}

    public static void appendFromAgentDirectory(Instrumentation instrumentation, Class<?> agentAnchorClass) {
        File agentJar = locateAgentJar(agentAnchorClass);
        if (agentJar == null) {
            System.err.println("[EventLens] Could not locate agent jar; observability bootstrap append skipped.");
            return;
        }

        File sibling = firstSiblingObservabilityJar(agentJar);
        if (sibling != null) {
            appendJar(instrumentation, sibling, "Observability appended to bootstrap classpath.");
            return;
        }

        if (appendJar(instrumentation, agentJar, "Observability appended from agent fat jar.")) {
            return;
        }

        System.err.println(
                "[EventLens] eventlens-observability jar not found beside agent and agent fat jar could not be appended.");
    }

    private static File firstSiblingObservabilityJar(File agentJar) {
        File parent = agentJar.getParentFile();
        if (parent == null) {
            return null;
        }
        File[] candidates = parent.listFiles((dir, name) ->
                name.startsWith("eventlens-observability") && name.endsWith(".jar") && !name.contains("-sources"));
        if (candidates == null || candidates.length == 0) {
            return null;
        }
        Arrays.sort(candidates, Comparator.comparing(File::getName));
        return candidates[0];
    }

    private static boolean appendJar(Instrumentation instrumentation, File jarFile, String successMessage) {
        try {
            JarFile jar = new JarFile(jarFile);
            instrumentation.appendToBootstrapClassLoaderSearch(jar);
            System.out.println("[EventLens] " + successMessage);
            return true;
        } catch (IOException ex) {
            System.err.println("[EventLens] Failed to append " + jarFile.getName() + " to bootstrap: " + ex.getMessage());
            return false;
        }
    }

    private static File locateAgentJar(Class<?> agentAnchorClass) {
        try {
            var location = agentAnchorClass.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            return new File(location.toURI());
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}
