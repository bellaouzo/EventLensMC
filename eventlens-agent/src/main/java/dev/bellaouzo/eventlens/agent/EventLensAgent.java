package dev.bellaouzo.eventlens.agent;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.ObservabilityBootstrap;
import dev.bellaouzo.eventlens.observability.ProtocolVersion;
import dev.bellaouzo.eventlens.observability.RegisteredListenerTimingAdvice;
import java.lang.instrument.Instrumentation;
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
        try {
            appendObservabilityToBootstrap(instrumentation);

            if (!verifyTarget(instrumentation)) {
                System.err.println("[EventLens] RegisteredListener.callEvent signature mismatch; agent disabled.");
                return;
            }

            new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(ElementMatchers.named(TARGET_CLASS))
                    .transform(EventLensAgent::installAdvice)
                    .installOn(instrumentation);
            AgentRuntime.markAgentLoaded();
            System.out.println("[EventLens] Agent loaded (protocol " + ProtocolVersion.CURRENT + ").");
        } catch (Throwable ex) {
            System.err.println("[EventLens] Agent failed to start; continuing without precise timing: "
                    + ex.getMessage());
            ex.printStackTrace(System.err);
        }
    }

    private static void appendObservabilityToBootstrap(Instrumentation instrumentation) {
        ObservabilityBootstrap.appendFromAgentDirectory(instrumentation, EventLensAgent.class);
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
