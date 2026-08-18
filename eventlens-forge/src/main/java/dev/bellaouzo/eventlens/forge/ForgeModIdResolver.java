package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.observability.AgentRuntime;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

final class ForgeModIdResolver implements AgentRuntime.OwnerIdResolver {

    private final Map<String, String> classOwners = new ConcurrentHashMap<>();

    @Override
    public String resolve(Object listener) {
        Object target = firstPresent(listener, "instance", "consumer", "handler");
        Class<?> type = (target != null ? target : listener).getClass();
        return modIdForClass(type);
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
            if (moduleName != null && ModList.isLoaded(moduleName)) {
                return moduleName;
            }
            String className = type.getName();
            for (var fileInfo : ModList.getModFiles()) {
                if (fileInfo.getMods().isEmpty()) {
                    continue;
                }
                String modId = fileInfo.getMods().getFirst().getModId();
                ModFileScanData scan = fileInfo.getFile().getScanResult();
                if (scan == null) {
                    continue;
                }
                boolean match =
                        scan.getClasses().stream().anyMatch(data -> ownsClass(data.clazz().getClassName(), className));
                if (match) {
                    return modId;
                }
            }
        } catch (RuntimeException ignored) {
            return "unknown";
        }
        return "unknown";
    }

    private static boolean ownsClass(String scanned, String actual) {
        return actual.equals(scanned) || actual.startsWith(scanned + "$") || actual.startsWith(scanned + "$$");
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

    private static Object firstPresent(Object listener, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = fieldValue(listener, fieldName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static Object fieldValue(Object target, String fieldName) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }
}
