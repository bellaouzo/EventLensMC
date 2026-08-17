package dev.bellaouzo.eventlens.clientagent;

import java.lang.instrument.Instrumentation;
import java.util.List;

final class ClientAgentBus {

    static final TargetSet NEOFORGE = new TargetSet(
            "NeoForge",
            "net.neoforged.bus.api.Event",
            List.of(
                    "net.neoforged.bus.ConsumerEventHandler",
                    "net.neoforged.bus.ConsumerEventHandler$WithPredicate",
                    "net.neoforged.bus.SubscribeEventListener"));

    static final TargetSet FORGE = new TargetSet(
            "Forge",
            "net.minecraftforge.eventbus.api.Event",
            List.of("net.minecraftforge.eventbus.ASMEventHandler"));

    private ClientAgentBus() {}

    record TargetSet(String name, String eventClass, List<String> classes) {
        boolean verify(Instrumentation instrumentation) {
            for (Class<?> loadedClass : instrumentation.getAllLoadedClasses()) {
                if (!classes.contains(loadedClass.getName())) {
                    continue;
                }
                if (!ClientAgentTargets.matches(loadedClass, eventClass)) {
                    return false;
                }
            }
            return true;
        }
    }
}
