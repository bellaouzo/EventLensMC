package dev.bellaouzo.eventlens.clientagent;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.EventListenerTimingAdvice;
import dev.bellaouzo.eventlens.observability.ProtocolVersion;
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

public final class EventLensClientAgent {

    private static final String INVOKE = "invoke";
    private static final String FORGE_LISTENER_LAMBDA = "net.minecraftforge.eventbus.EventBus$$Lambda";

    private EventLensClientAgent() {}

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        appendObservabilityToBootstrap(instrumentation);
        boolean neoForge = install(instrumentation, ClientAgentBus.NEOFORGE, false);
        boolean forge = install(instrumentation, ClientAgentBus.FORGE, true);
        if (neoForge || forge) {
            AgentRuntime.markAgentLoaded();
            System.out.println("[EventLens] Client agent loaded (protocol " + ProtocolVersion.CURRENT + ").");
            return;
        }
        System.err.println("[EventLens] Client agent found no matching event-bus targets; dispatch-only mode.");
    }

    private static boolean install(Instrumentation instrumentation, ClientAgentBus.TargetSet bus, boolean forgeLambdas) {
        if (!bus.verify(instrumentation)) {
            System.err.println("[EventLens] " + bus.name() + " EventListener.invoke signature mismatch; skipped.");
            return false;
        }
        try {
            var matcher = ElementMatchers.namedOneOf(bus.classes().toArray(String[]::new));
            if (forgeLambdas) {
                matcher = matcher.or(ElementMatchers.nameStartsWith(FORGE_LISTENER_LAMBDA));
            }
            new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(matcher)
                    .transform((builder, type, classLoader, module, domain) ->
                            installAdvice(builder, type, classLoader, module, domain, bus.eventClass()))
                    .installOn(instrumentation);
            System.out.println("[EventLens] Client agent installed " + bus.name() + " targets.");
            return true;
        } catch (Exception ex) {
            System.err.println("[EventLens] " + bus.name() + " client agent installation failed: " + ex.getMessage());
            ex.printStackTrace(System.err);
            return false;
        }
    }

    private static void appendObservabilityToBootstrap(Instrumentation instrumentation) {
        File agentJar = locateAgentJar();
        if (agentJar == null) {
            System.err.println("[EventLens] Could not locate client agent jar; observability bootstrap append skipped.");
            return;
        }
        File[] candidates = agentJar.getParentFile().listFiles((dir, name) ->
                name.startsWith("eventlens-observability") && name.endsWith(".jar"));
        if (candidates == null || candidates.length == 0) {
            System.err.println(
                    "[EventLens] eventlens-observability jar not found beside client agent; place it next to the agent jar.");
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
            var location = EventLensClientAgent.class.getProtectionDomain().getCodeSource().getLocation();
            if (location == null) {
                return null;
            }
            return new File(location.toURI());
        } catch (URISyntaxException ex) {
            return null;
        }
    }

    private static DynamicType.Builder<?> installAdvice(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassLoader classLoader,
            JavaModule module,
            java.security.ProtectionDomain protectionDomain,
            String eventClass) {
        return builder.visit(Advice.to(EventListenerTimingAdvice.class)
                .on(ElementMatchers.named(INVOKE)
                        .and(ElementMatchers.takesArguments(1))
                        .and(ElementMatchers.takesArgument(0, ElementMatchers.named(eventClass)))));
    }
}
