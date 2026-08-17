package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.port.ModListenerRegistryPort;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

public final class ForgeListenerRegistry implements ModListenerRegistryPort {

    private static final int MAX_HANDLERS = 32;
    private Map<String, List<ModHandlerRegistration>> byEvent;

    @Override
    public List<ModHandlerRegistration> listHandlers(String eventClassName) {
        return index().getOrDefault(eventClassName, List.of());
    }

    private Map<String, List<ModHandlerRegistration>> index() {
        if (byEvent != null) {
            return byEvent;
        }
        Map<String, List<ModHandlerRegistration>> index = new LinkedHashMap<>();
        try {
            ModList.get().getModFiles().forEach(fileInfo -> {
                String modId = fileInfo.getMods().isEmpty() ? "unknown" : fileInfo.getMods().getFirst().getModId();
                ModFileScanData scan = fileInfo.getFile().getScanResult();
                if (scan == null) {
                    return;
                }
                for (ModFileScanData.AnnotationData annotation : scan.getAnnotations()) {
                    if (annotation.targetType() != ElementType.METHOD) {
                        continue;
                    }
                    if (!SubscribeEvent.class.getName().equals(annotation.annotationType().getClassName())) {
                        continue;
                    }
                    addAnnotation(index, modId, annotation);
                }
            });
        } catch (RuntimeException ignored) {
            byEvent = Map.of();
            return byEvent;
        }
        byEvent = Map.copyOf(index);
        return byEvent;
    }

    private static void addAnnotation(
            Map<String, List<ModHandlerRegistration>> index, String modId, ModFileScanData.AnnotationData annotation) {
        String member = annotation.memberName();
        int paren = member.indexOf('(');
        String method = paren < 0 ? member : member.substring(0, paren);
        String descriptor = paren < 0 ? "" : member.substring(paren);
        String handlerClass = annotation.clazz().getClassName();
        int priority = priorityOrdinal(annotation.annotationData().get("priority"));
        for (String type : descriptorTypes(descriptor)) {
            String synthetic = ForgeEventTypes.syntheticClassName(type);
            if (synthetic == null) {
                continue;
            }
            List<ModHandlerRegistration> handlers = index.computeIfAbsent(synthetic, ignored -> new ArrayList<>());
            if (handlers.size() >= MAX_HANDLERS) {
                continue;
            }
            handlers.add(new ModHandlerRegistration(modId, handlerClass, method, priority));
        }
    }

    private static List<String> descriptorTypes(String descriptor) {
        List<String> types = new ArrayList<>();
        int index = 0;
        while (index < descriptor.length()) {
            int start = descriptor.indexOf('L', index);
            if (start < 0) {
                break;
            }
            int end = descriptor.indexOf(';', start);
            if (end < 0) {
                break;
            }
            types.add(descriptor.substring(start + 1, end));
            index = end + 1;
        }
        return types;
    }

    private static int priorityOrdinal(Object priority) {
        if (priority instanceof EventPriority eventPriority) {
            return eventPriority.ordinal();
        }
        if (priority instanceof String name) {
            try {
                return EventPriority.valueOf(name).ordinal();
            } catch (IllegalArgumentException ignored) {
                return EventPriority.NORMAL.ordinal();
            }
        }
        return EventPriority.NORMAL.ordinal();
    }
}
