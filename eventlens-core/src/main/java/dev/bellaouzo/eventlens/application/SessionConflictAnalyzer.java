package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.conflict.ConflictDetectionEngine;
import dev.bellaouzo.eventlens.domain.conflict.ConflictKind;
import dev.bellaouzo.eventlens.domain.conflict.ConflictSeverity;
import dev.bellaouzo.eventlens.domain.conflict.DispatchConflict;
import dev.bellaouzo.eventlens.domain.conflict.InvestigationTarget;
import dev.bellaouzo.eventlens.domain.conflict.ListenerChainAnalyzer;
import dev.bellaouzo.eventlens.domain.conflict.SessionConflictSummary;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SessionConflictAnalyzer {

    private static final int MAX_TARGETS = 5;
    private static final Pattern LISTENER_MESSAGE =
            Pattern.compile("^(?<plugin>[^/]+)/(?<listener>[^#]+)#(?<method>\\S+)");

    private SessionConflictAnalyzer() {}

    public static List<DispatchConflict> detectForDispatch(TraceDispatchRecord dispatch, long slowThresholdNanos) {
        return ConflictDetectionEngine.detect(dispatch, slowThresholdNanos);
    }

    public static List<DispatchConflict> analyzeInventory(List<ListenerRegistration> registrations) {
        return ListenerChainAnalyzer.detectInventoryIssues(registrations);
    }

    public static SessionConflictSummary analyze(List<TraceDispatchRecord> records, long slowThresholdNanos) {
        if (records.isEmpty()) {
            return SessionConflictSummary.empty();
        }

        List<DispatchConflict> allConflicts = new ArrayList<>();
        Set<Long> dispatchSequencesWithConflicts = new LinkedHashSet<>();

        for (TraceDispatchRecord dispatch : records) {
            List<DispatchConflict> conflicts = ConflictDetectionEngine.detect(dispatch, slowThresholdNanos);
            if (!conflicts.isEmpty()) {
                dispatchSequencesWithConflicts.add(dispatch.sequence());
            }
            allConflicts.addAll(conflicts);
        }

        if (allConflicts.isEmpty()) {
            return new SessionConflictSummary(
                    records.size(), 0, "No conflicts detected.", Map.of(), List.of(), List.of());
        }

        Map<ConflictKind, Integer> countsByKind = countByKind(allConflicts);
        List<InvestigationTarget> targets = rankInvestigationTargets(allConflicts);
        String summary =
                buildLikelySummary(records.size(), dispatchSequencesWithConflicts.size(), countsByKind, targets);
        List<String> suggestions = buildSuggestions(countsByKind, targets);

        return new SessionConflictSummary(
                records.size(), dispatchSequencesWithConflicts.size(), summary, countsByKind, targets, suggestions);
    }

    private static Map<ConflictKind, Integer> countByKind(List<DispatchConflict> conflicts) {
        Map<ConflictKind, Integer> counts = new EnumMap<>(ConflictKind.class);
        for (DispatchConflict conflict : conflicts) {
            counts.merge(conflict.kind(), 1, Integer::sum);
        }
        return Map.copyOf(counts);
    }

    private static List<InvestigationTarget> rankInvestigationTargets(List<DispatchConflict> conflicts) {
        Map<String, TargetAccumulator> accumulators = new LinkedHashMap<>();

        for (DispatchConflict conflict : conflicts) {
            Optional<ParsedListener> parsedListener = parseListener(conflict.message());
            if (parsedListener.isPresent()) {
                ParsedListener listener = parsedListener.get();
                String key = listener.pluginName() + "|" + listener.listenerClassName() + "|" + listener.methodName();
                accumulators
                        .computeIfAbsent(
                                key,
                                ignored -> new TargetAccumulator(
                                        listener.pluginName(),
                                        Optional.of(listener.listenerClassName()),
                                        Optional.of(listener.methodName())))
                        .recordOccurrence(conflict.severity());
                continue;
            }

            for (String plugin : conflict.involvedPlugins()) {
                accumulators
                        .computeIfAbsent(
                                plugin, ignored -> new TargetAccumulator(plugin, Optional.empty(), Optional.empty()))
                        .recordOccurrence(conflict.severity());
            }
        }

        return accumulators.values().stream()
                .sorted(Comparator.comparingInt(TargetAccumulator::score)
                        .reversed()
                        .thenComparing(TargetAccumulator::pluginName))
                .limit(MAX_TARGETS)
                .map(TargetAccumulator::toTarget)
                .toList();
    }

    private static Optional<ParsedListener> parseListener(String message) {
        Matcher matcher = LISTENER_MESSAGE.matcher(message);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(
                new ParsedListener(matcher.group("plugin"), matcher.group("listener"), matcher.group("method")));
    }

    private static String buildLikelySummary(
            int dispatchesAnalyzed,
            int dispatchesWithConflicts,
            Map<ConflictKind, Integer> countsByKind,
            List<InvestigationTarget> targets) {
        List<String> kindSummaries = countsByKind.entrySet().stream()
                .sorted(Map.Entry.<ConflictKind, Integer>comparingByValue().reversed())
                .map(entry -> formatKind(entry.getKey()) + " (" + entry.getValue() + ")")
                .toList();

        String kinds = String.join(", ", kindSummaries);
        String suspects = targets.isEmpty()
                ? "unknown plugins"
                : targets.stream()
                        .map(InvestigationTarget::displayName)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("");

        return dispatchesWithConflicts + " of " + dispatchesAnalyzed + " dispatches flagged: " + kinds
                + ". Investigate first: " + suspects + ".";
    }

    private static List<String> buildSuggestions(
            Map<ConflictKind, Integer> countsByKind, List<InvestigationTarget> targets) {
        List<String> suggestions = new ArrayList<>();
        String primary = targets.isEmpty() ? null : targets.getFirst().displayName();

        if (countsByKind.containsKey(ConflictKind.CANCELLATION_FIGHT)) {
            suggestions.add("Review cancellation toggles"
                    + (primary == null ? "" : " starting with " + primary)
                    + "; multiple plugins may be fighting over event cancellation.");
        }
        if (countsByKind.containsKey(ConflictKind.MULTI_PLUGIN_PROPERTY_CHANGE)) {
            suggestions.add(
                    "Compare property mutations within the same priority band; multiple plugins changed state.");
        }
        if (countsByKind.containsKey(ConflictKind.PROPERTY_REVERTED)) {
            suggestions.add("Look for listeners undoing earlier changes (property reverted to original value).");
        }
        if (countsByKind.containsKey(ConflictKind.POST_CANCEL_LISTENER)) {
            suggestions.add("Check listeners with ignoreCancelled=false that still run after cancellation.");
        }
        if (countsByKind.containsKey(ConflictKind.MONITOR_MUTATION)) {
            suggestions.add("MONITOR listeners should observe only; remove mutations at MONITOR priority.");
        }
        if (countsByKind.containsKey(ConflictKind.DUPLICATE_REGISTRATION)) {
            suggestions.add("Remove duplicate listener registrations for the same plugin/class/method/priority.");
        }
        if (countsByKind.containsKey(ConflictKind.EQUIVALENT_LISTENER)) {
            suggestions.add("Consolidate repeated listener registrations from the same plugin at one priority.");
        }
        if (countsByKind.containsKey(ConflictKind.LISTENER_EXCEPTION)) {
            suggestions.add("Fix listeners that throw exceptions during event handling.");
        }
        if (countsByKind.containsKey(ConflictKind.SLOW_LISTENER_CHAIN)) {
            suggestions.add("Profile unusually slow listener chains; consider async work or hot-path optimization.");
        }

        return List.copyOf(suggestions);
    }

    private static String formatKind(ConflictKind kind) {
        return kind.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private record ParsedListener(String pluginName, String listenerClassName, String methodName) {}

    private static final class TargetAccumulator {
        private final String pluginName;
        private final Optional<String> listenerClassName;
        private final Optional<String> methodName;
        private int occurrenceCount;
        private ConflictSeverity maxSeverity = ConflictSeverity.LOW;

        private TargetAccumulator(String pluginName, Optional<String> listenerClassName, Optional<String> methodName) {
            this.pluginName = pluginName;
            this.listenerClassName = listenerClassName;
            this.methodName = methodName;
        }

        private void recordOccurrence(ConflictSeverity severity) {
            occurrenceCount++;
            if (severity == ConflictSeverity.HIGH) {
                maxSeverity = ConflictSeverity.HIGH;
            } else if (severity == ConflictSeverity.MEDIUM && maxSeverity != ConflictSeverity.HIGH) {
                maxSeverity = ConflictSeverity.MEDIUM;
            }
        }

        private int score() {
            int severityWeight =
                    switch (maxSeverity) {
                        case HIGH -> 3;
                        case MEDIUM -> 2;
                        case LOW -> 1;
                    };
            return occurrenceCount * severityWeight;
        }

        private String pluginName() {
            return pluginName;
        }

        private InvestigationTarget toTarget() {
            return new InvestigationTarget(pluginName, listenerClassName, methodName, occurrenceCount, maxSeverity);
        }
    }
}
