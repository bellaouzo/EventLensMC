package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import dev.bellaouzo.eventlens.observability.FabricListenerTimingWrapper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

final class FabricModIdResolver implements AgentRuntime.OwnerIdResolver {

    private final Map<String, String> classOwners = new ConcurrentHashMap<>();

    @Override
    public String resolve(Object listener) {
        Object target = FabricListenerTimingWrapper.unwrap(listener);
        return modIdForClass(target.getClass());
    }

    String modIdForClass(Class<?> type) {
        if (type == null) {
            return "unknown";
        }
        Class<?> resolved = unwrapGenerated(type);
        return classOwners.computeIfAbsent(resolved.getName(), ignored -> lookup(resolved));
    }

    private static String lookup(Class<?> type) {
        try {
            String moduleName = type.getModule().getName();
            if (moduleName != null && FabricLoader.getInstance().isModLoaded(moduleName)) {
                return moduleName;
            }
            String classResource = type.getName().replace('.', '/') + ".class";
            for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
                if (container.findPath(classResource).isPresent()) {
                    return container.getMetadata().getId();
                }
            }
        } catch (RuntimeException ignored) {
            return "unknown";
        }
        return "unknown";
    }

    private static Class<?> unwrapGenerated(Class<?> type) {
        String name = type.getName();
        int lambda = name.indexOf("$$Lambda");
        if (lambda < 0) {
            return type;
        }
        try {
            return Class.forName(name.substring(0, lambda), false, type.getClassLoader());
        } catch (ClassNotFoundException ignored) {
            return type;
        }
    }
}
