package dev.bellaouzo.eventlens.domain.diff;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import dev.bellaouzo.eventlens.domain.trace.TraceListenerSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SnapshotDiffEngine {

    private static final String CANCELLED_PROPERTY = "cancelled";
    private static final List<String> PRIORITY_ORDER = List.of("LOWEST", "LOW", "NORMAL", "HIGH", "HIGHEST", "MONITOR");

    private SnapshotDiffEngine() {}

    public static SnapshotDiff diff(EventSnapshot before, EventSnapshot after, boolean includeUnchanged) {
        Map<String, SnapshotValue> beforeValues = toValueMap(before);
        Map<String, SnapshotValue> afterValues = toValueMap(after);

        Set<String> propertyNames = new LinkedHashSet<>();
        propertyNames.addAll(beforeValues.keySet());
        propertyNames.addAll(afterValues.keySet());

        List<PropertyChange> changed = new ArrayList<>();
        List<PropertyChange> unchanged = new ArrayList<>();

        for (String property : propertyNames) {
            SnapshotValue beforeValue = beforeValues.getOrDefault(property, unsupported("missing before capture"));
            SnapshotValue afterValue = afterValues.getOrDefault(property, unsupported("missing after capture"));
            PropertyChange change = new PropertyChange(property, beforeValue, afterValue);
            if (valuesEqual(beforeValue, afterValue)) {
                if (includeUnchanged) {
                    unchanged.add(change);
                }
            } else {
                changed.add(change);
            }
        }

        Optional<CancellationTransition> cancellationTransition =
                detectCancellationTransition(beforeValues, afterValues);
        return new SnapshotDiff(List.copyOf(changed), List.copyOf(unchanged), cancellationTransition);
    }

    public static List<BandChange> computeBandChanges(
            List<EventSnapshot> checkpointsInOrder,
            List<TraceListenerSnapshot> listenerChain,
            boolean includeUnchanged) {
        if (checkpointsInOrder.size() < 2) {
            return List.of();
        }

        List<BandChange> bandChanges = new ArrayList<>();
        for (int index = 1; index < checkpointsInOrder.size(); index++) {
            EventSnapshot previous = checkpointsInOrder.get(index - 1);
            EventSnapshot current = checkpointsInOrder.get(index);
            String band = current.checkpoint();
            List<String> plugins = pluginsForPriority(listenerChain, band);
            SnapshotDiff diff = diff(previous, current, includeUnchanged);
            if (diff.changed().isEmpty() && !hasMeaningfulCancellationChange(diff) && !includeUnchanged) {
                continue;
            }

            boolean conflicting = plugins.size() > 1
                    && (!diff.changed().isEmpty()
                            || diff.cancellationTransition()
                                    .map(transition -> transition.kind() != CancellationTransitionKind.UNCHANGED)
                                    .orElse(false));

            bandChanges.add(new BandChange(band, List.copyOf(plugins), conflicting, diff));
        }

        return List.copyOf(bandChanges);
    }

    public static List<String> priorityCheckpoints() {
        return PRIORITY_ORDER;
    }

    private static Map<String, SnapshotValue> toValueMap(EventSnapshot snapshot) {
        Map<String, SnapshotValue> values = new LinkedHashMap<>();
        for (SnapshotField field : snapshot.fields()) {
            values.putIfAbsent(field.name(), field.value());
        }
        return values;
    }

    private static boolean valuesEqual(SnapshotValue left, SnapshotValue right) {
        if (left instanceof SnapshotValue.Unsupported || right instanceof SnapshotValue.Unsupported) {
            return left.getClass().equals(right.getClass()) && displayText(left).equals(displayText(right));
        }
        return displayText(left).equals(displayText(right));
    }

    private static String displayText(SnapshotValue value) {
        return switch (value) {
            case SnapshotValue.Present(var type, var display) -> type + ":" + display;
            case SnapshotValue.Unsupported(var reason) -> "unsupported:" + reason;
            case SnapshotValue.Truncated(var display, var reason) -> "truncated:" + display + ":" + reason;
        };
    }

    private static SnapshotValue unsupported(String reason) {
        return new SnapshotValue.Unsupported(reason);
    }

    private static Optional<CancellationTransition> detectCancellationTransition(
            Map<String, SnapshotValue> beforeValues, Map<String, SnapshotValue> afterValues) {
        if (!beforeValues.containsKey(CANCELLED_PROPERTY) || !afterValues.containsKey(CANCELLED_PROPERTY)) {
            return Optional.empty();
        }

        boolean before = parseBoolean(beforeValues.get(CANCELLED_PROPERTY));
        boolean after = parseBoolean(afterValues.get(CANCELLED_PROPERTY));
        CancellationTransitionKind kind;
        if (before == after) {
            kind = CancellationTransitionKind.UNCHANGED;
        } else if (after) {
            kind = CancellationTransitionKind.BECAME_CANCELLED;
        } else {
            kind = CancellationTransitionKind.BECAME_UNCANCELLED;
        }

        return Optional.of(new CancellationTransition(before, after, kind));
    }

    private static boolean parseBoolean(SnapshotValue value) {
        if (value instanceof SnapshotValue.Present(var type, var display) && "boolean".equals(type)) {
            return Boolean.parseBoolean(display);
        }
        return false;
    }

    private static List<String> pluginsForPriority(List<TraceListenerSnapshot> listenerChain, String priorityBand) {
        List<String> plugins = new ArrayList<>();
        for (TraceListenerSnapshot listener : listenerChain) {
            if (listener.priority().equalsIgnoreCase(priorityBand)) {
                String pluginName = listener.pluginName();
                if (!plugins.contains(pluginName)) {
                    plugins.add(pluginName);
                }
            }
        }
        return List.copyOf(plugins);
    }

    private static boolean hasMeaningfulCancellationChange(SnapshotDiff diff) {
        Optional<CancellationTransition> transition = diff.cancellationTransition();
        if (transition.isEmpty()) {
            return false;
        }
        return transition.get().kind() != CancellationTransitionKind.UNCHANGED;
    }
}
