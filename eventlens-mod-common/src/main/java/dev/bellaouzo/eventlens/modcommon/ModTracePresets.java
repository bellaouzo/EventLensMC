package dev.bellaouzo.eventlens.modcommon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ModTracePresets {

    public static final String CLICK_FLOW = "click-flow";
    private static final Map<String, List<String>> PRESETS = Map.of(
            CLICK_FLOW,
            List.of(
                    "ClientUseItemEvent",
                    "ClientUseBlockEvent",
                    "ClientUseEntityEvent",
                    "ClientAttackEvent",
                    "ClientAttackBlockEvent"));

    private ModTracePresets() {}

    public static List<String> names() {
        return List.copyOf(PRESETS.keySet());
    }

    public static List<String> events(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        return PRESETS.getOrDefault(name.trim().toLowerCase(Locale.ROOT), List.of());
    }

    public static List<String> resolveEvents(String query) {
        List<String> names = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return names;
        }
        List<String> presetEvents = events(query);
        if (!presetEvents.isEmpty()) {
            return presetEvents;
        }
        for (String part : query.split("[,\\s]+")) {
            if (!part.isBlank()) {
                names.add(part.trim());
            }
        }
        return names;
    }

    public record ResolvedStart(List<String> classNames, String label, boolean anyHot, String error) {
        public boolean failed() {
            return error != null && !error.isBlank();
        }
    }

    public static ResolvedStart resolveStart(String query) {
        List<String> requested = resolveEvents(query);
        List<String> classNames = new ArrayList<>();
        boolean anyHot = false;
        String label = "";
        for (String name : requested) {
            var type = SupportedModEventTypes.resolve(name);
            if (type.isEmpty()) {
                return new ResolvedStart(
                        List.of(),
                        "",
                        false,
                        "Unsupported event. Supported: " + String.join(", ", SupportedModEventTypes.simpleNames()));
            }
            var eventType = type.orElseThrow();
            classNames.add(eventType.className());
            anyHot = anyHot || eventType.hot();
            label = label.isEmpty() ? eventType.simpleName() : label + "," + eventType.simpleName();
        }
        if (classNames.isEmpty()) {
            return new ResolvedStart(List.of(), "", false, "Specify event names or --preset click-flow.");
        }
        return new ResolvedStart(classNames, label, anyHot, "");
    }
}
