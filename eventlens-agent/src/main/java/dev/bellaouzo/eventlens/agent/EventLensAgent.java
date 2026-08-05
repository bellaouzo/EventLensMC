package dev.bellaouzo.eventlens.agent;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.ProtocolVersion;
import dev.bellaouzo.eventlens.observability.RegisteredListenerTimingAdvice;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.jar.JarFile;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public final class EventLensAgent {

    private static final String TARGET_CLASS = "org.bukkit.plugin.RegisteredListener";
    private static final String TARGET_METHOD = "callEvent";
    private static final String TARGET_EVENT = "org.bukkit.event.Event";

    private EventLensAgent() {}

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        appendObservabilityToBootstrap(instrumentation);

        if (!verifyTarget(instrumentation)) {
            System.err.println("[EventLens] RegisteredListener.callEvent signature mismatch; agent disabled.");
            return;
        }

        try {
            new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(ElementMatchers.named(TARGET_CLASS))
                    .transform(EventLensAgent::installAdvice)
                    .installOn(instrumentation);
            AgentRuntime.markAgentLoaded();
            System.out.println("[EventLens] Agent loaded (protocol " + ProtocolVersion.CURRENT + ").");
        } catch (Exception ex) {
            System.err.println("[EventLens] Agent installation failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private static void appendObservabilityToBootstrap(Instrumentation instrumentation) {
        File agentJar = locateAgentJar();
        if (agentJar == null) {
            System.err.println("[EventLens] Could not locate agent jar; observability bootstrap append skipped.");
            return;
        }

        File[] candidates = agentJar.getParentFile().listFiles((dir, name) ->
                name.startsWith("eventlens-observability") && name.endsWith(".jar"));
        if (candidates == null || candidates.length == 0) {
            System.err.println(
                    "[EventLens] eventlens-observability jar not found beside agent; place it next to the agent jar.");
            return;
        }

        Arrays.sort(candidates, Comparator.comparing(File::getName));
        try {
            JarFile observabilityJar = new JarFile(candidates[0]);
            instrumentation.appendToBootstrapClassLoaderSearch(observabilityJar);
            System.out.println("[EventLens] Observability appended to bootstrap classpath.");
        } catch (IOException ex) {
            System.err.println("[EventLens] Failed to append observability to bootstrap: " + ex.getMessage());
        }
    }

    private static File locateAgentJar() {
        try {
            var location = EventLensAgent.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            return new File(location.toURI());
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private static boolean verifyTarget(Instrumentation instrumentation) {
        for (Class<?> loadedClass : instrumentation.getAllLoadedClasses()) {
            if (!TARGET_CLASS.equals(loadedClass.getName())) {
                continue;
            }
            try {
                loadedClass.getMethod(TARGET_METHOD, Class.forName(TARGET_EVENT));
                return true;
            } catch (ReflectiveOperationException ex) {
                return false;
            }
        }
        return true;
    }

    private static DynamicType.Builder<?> installAdvice(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassLoader classLoader,
            JavaModule module,
            java.security.ProtectionDomain protectionDomain) {
        return builder.visit(Advice.to(RegisteredListenerTimingAdvice.class)
                .on(ElementMatchers.named(TARGET_METHOD)
                        .and(ElementMatchers.takesArguments(1))
                        .and(ElementMatchers.takesArgument(0, ElementMatchers.named(TARGET_EVENT)))));
    }
}
