package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.observability.CompactEventSnapshot;
import dev.bellaouzo.eventlens.observability.CompactField;
import dev.bellaouzo.eventlens.observability.ListenerSnapshotBridge;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

final class ForgeListenerSnapshotBridge implements ListenerSnapshotBridge {

    private static final int MAX_FIELDS = 8;
    private static final int MAX_STRING = 512;
    private static final List<String> ACCESSORS = List.of("getMessage", "getScreen", "getHand", "getTarget");

    @Override
    public CompactEventSnapshot capture(Object event, String checkpoint) {
        if (event == null) {
            return CompactEventSnapshot.empty(checkpoint);
        }
        List<CompactField> fields = new ArrayList<>();
        for (String accessor : ACCESSORS) {
            if (fields.size() >= MAX_FIELDS) {
                break;
            }
            addAccessor(fields, event, accessor);
        }
        return new CompactEventSnapshot(event.getClass().getName(), checkpoint, System.nanoTime(), fields);
    }

    private static void addAccessor(List<CompactField> fields, Object event, String accessor) {
        try {
            Method method = event.getClass().getMethod(accessor);
            Object value = method.invoke(event);
            if (value == null) {
                return;
            }
            String display = clip(displayValue(value));
            fields.add(new CompactField(fieldName(accessor), "string", display));
        } catch (ReflectiveOperationException ignored) {
            // Event type does not expose this accessor.
        }
    }

    private static String displayValue(Object value) {
        if (value instanceof Enum<?> enumerated) {
            return enumerated.name();
        }
        String text = String.valueOf(value);
        int newline = text.indexOf('\n');
        return newline < 0 ? text : text.substring(0, newline);
    }

    private static String fieldName(String accessor) {
        if (accessor.startsWith("get") && accessor.length() > 3) {
            return Character.toLowerCase(accessor.charAt(3)) + accessor.substring(4);
        }
        return accessor;
    }

    private static String clip(String value) {
        return value.length() <= MAX_STRING ? value : value.substring(0, MAX_STRING);
    }
}
