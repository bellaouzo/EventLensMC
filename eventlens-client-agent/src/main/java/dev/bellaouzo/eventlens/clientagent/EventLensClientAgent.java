package dev.bellaouzo.eventlens.clientagent;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.EventListenerTimingAdvice;
import dev.bellaouzo.eventlens.observability.ObservabilityBootstrap;
import dev.bellaouzo.eventlens.observability.ProtocolVersion;
import java.lang.instrument.Instrumentation;
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
        try {
            appendObservabilityToBootstrap(instrumentation);
            boolean neoForge = install(instrumentation, ClientAgentBus.NEOFORGE, false);
            boolean forge = install(instrumentation, ClientAgentBus.FORGE, true);
            boolean fabric = installFabric(instrumentation);
            if (neoForge || forge || fabric) {
                AgentRuntime.markAgentLoaded();
                System.out.println("[EventLens] Client agent loaded (protocol " + ProtocolVersion.CURRENT + ").");
                return;
            }
            System.err.println("[EventLens] Client agent found no matching event-bus targets; dispatch-only mode.");
        } catch (Throwable ex) {
            System.err.println("[EventLens] Client agent failed to start; continuing without precise timing: "
                    + ex.getMessage());
            ex.printStackTrace(System.err);
        }
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

    private static boolean installFabric(Instrumentation instrumentation) {
        ClientAgentBus.TargetSet bus = ClientAgentBus.FABRIC;
        if (!bus.verify(instrumentation)) {
            System.err.println("[EventLens] " + bus.name() + " listener registration signature mismatch; skipped.");
            return false;
        }
        try {
            new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(ElementMatchers.namedOneOf(bus.classes().toArray(String[]::new)))
                    .transform((builder, type, classLoader, module, domain) -> builder.visit(
                            Advice.to(FabricListenerRegisterAdvice.class)
                                    .on(ElementMatchers.named("register")
                                            .and(ElementMatchers.takesArguments(1)
                                                    .or(ElementMatchers.takesArguments(2))))))
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
        ObservabilityBootstrap.appendFromAgentDirectory(instrumentation, EventLensClientAgent.class);
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
