package dev.bellaouzo.eventlens.domain.conflict;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class ConflictDetectionSupport {

    private static final List<String> PRIORITY_ORDER = List.of("LOWEST", "LOW", "NORMAL", "HIGH", "HIGHEST", "MONITOR");

    private ConflictDetectionSupport() {}

    static List<DispatchConflict> tagSequence(List<DispatchConflict> conflicts, long sequence) {
        return conflicts.stream()
                .map(conflict -> new DispatchConflict(
                        conflict.kind(),
                        conflict.severity(),
                        conflict.message(),
                        conflict.involvedPlugins(),
                        Optional.of(sequence)))
                .toList();
    }

    static List<DispatchConflict> dedupe(List<DispatchConflict> conflicts) {
        Set<String> seen = new LinkedHashSet<>();
        List<DispatchConflict> unique = new ArrayList<>();
        for (DispatchConflict conflict : conflicts) {
            String key = conflict.kind() + "|" + conflict.message();
            if (seen.add(key)) {
                unique.add(conflict);
            }
        }
        return List.copyOf(unique);
    }

    static Map<String, SnapshotValue> valueMap(EventSnapshot snapshot) {
        Map<String, SnapshotValue> values = new LinkedHashMap<>();
        snapshot.fields().forEach(field -> values.putIfAbsent(field.name(), field.value()));
        return values;
    }

    static boolean valuesEqual(SnapshotValue left, SnapshotValue right) {
        return displayText(left).equals(displayText(right));
    }

    static int priorityIndex(String priority) {
        for (int index = 0; index < PRIORITY_ORDER.size(); index++) {
            if (PRIORITY_ORDER.get(index).equalsIgnoreCase(priority)) {
                return index;
            }
        }
        return PRIORITY_ORDER.size();
    }

    static Map<String, TraceListenerSnapshot> indexChain(List<TraceListenerSnapshot> chain) {
        Map<String, TraceListenerSnapshot> indexed = new LinkedHashMap<>();
        for (TraceListenerSnapshot listener : chain) {
            indexed.putIfAbsent(listenerKey(listener), listener);
        }
        return indexed;
    }

    static String listenerKey(ListenerTimingRecord timing) {
        return timing.pluginName() + "|" + timing.listenerClassName() + "|" + timing.methodName() + "|"
                + timing.priority();
    }

    static String listenerKey(TraceListenerSnapshot listener) {
        return listener.pluginName() + "|" + listener.listenerClassName() + "|" + listener.methodName() + "|"
                + listener.priority();
    }

    static String simpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    private static String displayText(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present(var type, var display) -> type + ":" + display;
            case SnapshotValue.Unsupported(var reason) -> "unsupported:" + reason;
            case SnapshotValue.Truncated(var display, var reason) -> "truncated:" + display + ":" + reason;
        };
    }
}
