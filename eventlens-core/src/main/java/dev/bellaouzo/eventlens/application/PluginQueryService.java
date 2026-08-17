package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.ListenerRegistryPort;
import dev.bellaouzo.eventlens.application.port.PluginRegistryPort;
import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginCoInteraction;
import dev.bellaouzo.eventlens.domain.plugin.PluginCompareResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginDescriptor;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerBinding;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerInventory;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerPage;
import dev.bellaouzo.eventlens.domain.plugin.PluginListenerQueryResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginProfile;
import dev.bellaouzo.eventlens.domain.plugin.PluginQueryResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginSearchOutcome;
import dev.bellaouzo.eventlens.domain.plugin.PluginSearchResult;
import dev.bellaouzo.eventlens.domain.plugin.PluginTraceStatistics;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class PluginQueryService {

    public static final int DEFAULT_PAGE_SIZE = 6;

    private final PluginRegistryPort pluginRegistryPort;
    private final ListenerRegistryPort listenerRegistryPort;
    private final TraceSessionManager traceSessionManager;
    private final InstrumentationPort instrumentationPort;

    public PluginQueryService(
            PluginRegistryPort pluginRegistryPort,
            ListenerRegistryPort listenerRegistryPort,
            TraceSessionManager traceSessionManager,
            InstrumentationPort instrumentationPort) {
        this.pluginRegistryPort = pluginRegistryPort;
        this.listenerRegistryPort = listenerRegistryPort;
        this.traceSessionManager = traceSessionManager;
        this.instrumentationPort = instrumentationPort;
    }

    public List<String> listPluginNames() {
        return pluginRegistryPort.listPluginNames();
    }

    public List<String> listKnownEventSimpleNames() {
        return listenerRegistryPort.listKnownEventSimpleNames();
    }

    public Optional<String> resolvePluginName(String pluginQuery) {
        PluginSearchResult search = pluginRegistryPort.searchPlugins(pluginQuery.trim());
        if (search.outcome() == PluginSearchOutcome.FOUND) {
            return Optional.of(search.resolvedPluginName());
        }
        return Optional.empty();
    }

    public PluginQueryResult queryProfile(String pluginQuery) {
        PluginSearchResult search = pluginRegistryPort.searchPlugins(pluginQuery.trim());
        return switch (search.outcome()) {
            case NOT_FOUND -> new PluginQueryResult.NotFound(pluginQuery.trim());
            case AMBIGUOUS -> new PluginQueryResult.Ambiguous(search.candidateNames());
            case FOUND -> new PluginQueryResult.Success(buildProfile(search.resolvedPluginName()));
        };
    }

    public Optional<PluginCompareResult> comparePlugins(String leftQuery, String rightQuery) {
        Optional<PluginProfile> left = resolveProfile(leftQuery);
        Optional<PluginProfile> right = resolveProfile(rightQuery);
        if (left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }

        PluginProfile leftProfile = left.get();
        PluginProfile rightProfile = right.get();
        List<String> leftEvents = leftProfile.inventory().eventClassNames();
        List<String> rightEvents = rightProfile.inventory().eventClassNames();

        return Optional.of(new PluginCompareResult(
                leftProfile,
                rightProfile,
                PluginInventoryScanner.sharedEvents(leftEvents, rightEvents),
                PluginInventoryScanner.eventsOnlyIn(leftEvents, rightEvents),
                PluginInventoryScanner.eventsOnlyIn(rightEvents, leftEvents),
                PluginInventoryScanner.sharedCoPlugins(List.of(leftProfile.inventory(), rightProfile.inventory()))));
    }

    public PluginListenerQueryResult queryListeners(String pluginQuery, String eventQuery, int page, int pageSize) {
        PluginSearchResult search = pluginRegistryPort.searchPlugins(pluginQuery.trim());
        return switch (search.outcome()) {
            case NOT_FOUND -> new PluginListenerQueryResult.PluginNotFound(pluginQuery.trim());
            case AMBIGUOUS -> new PluginListenerQueryResult.PluginAmbiguous(search.candidateNames());
            case FOUND -> paginateListeners(search.resolvedPluginName(), eventQuery, page, pageSize);
        };
    }

    public List<String> listEventSimpleNamesForPlugin(String pluginName) {
        PluginListenerInventory inventory = PluginInventoryScanner.scan(pluginName, listenerRegistryPort);
        return inventory.eventClassNames().stream()
                .map(this::simpleEventName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private Optional<PluginProfile> resolveProfile(String pluginQuery) {
        PluginSearchResult search = pluginRegistryPort.searchPlugins(pluginQuery.trim());
        if (search.outcome() != PluginSearchOutcome.FOUND) {
            return Optional.empty();
        }
        return Optional.of(buildProfile(search.resolvedPluginName()));
    }

    private PluginProfile buildProfile(String pluginName) {
        PluginDescriptor descriptor = pluginRegistryPort
                .getDescriptor(pluginName)
                .orElseThrow(() -> new IllegalStateException("Plugin descriptor missing: " + pluginName));
        PluginListenerInventory inventory = PluginInventoryScanner.scan(pluginName, listenerRegistryPort);
        PluginTraceStatistics traceStatistics =
                PluginTraceAggregator.aggregate(pluginName, traceSessionManager, isAgentPresent());
        List<PluginCoInteraction> coInteractions = mergeCoInteractions(inventory, traceStatistics);
        return new PluginProfile(descriptor, inventory, coInteractions, traceStatistics);
    }

    private PluginListenerQueryResult paginateListeners(String pluginName, String eventQuery, int page, int pageSize) {
        PluginListenerInventory inventory = PluginInventoryScanner.scan(pluginName, listenerRegistryPort);
        List<PluginListenerBinding> bindings = inventory.bindings();
        String filteredEventClassName = null;

        if (eventQuery != null && !eventQuery.isBlank()) {
            EventSearchResult eventSearch = listenerRegistryPort.searchEvents(eventQuery.trim());
            switch (eventSearch.outcome()) {
                case NOT_FOUND -> {
                    return new PluginListenerQueryResult.EventNotFound(eventQuery.trim());
                }
                case AMBIGUOUS -> {
                    return new PluginListenerQueryResult.EventAmbiguous(eventSearch.candidateClassNames());
                }
                case FOUND -> {
                    filteredEventClassName = eventSearch.resolvedEventClassName();
                    String resolved = filteredEventClassName;
                    bindings = bindings.stream()
                            .filter(binding -> binding.eventClassName().equalsIgnoreCase(resolved))
                            .toList();
                }
            }
        }

        int safePageSize = Math.max(1, pageSize);
        int totalListeners = bindings.size();
        int totalPages = totalListeners == 0 ? 1 : (int) Math.ceil((double) totalListeners / safePageSize);
        if (page < 1 || page > totalPages) {
            return new PluginListenerQueryResult.InvalidPage(page, totalPages);
        }

        int fromIndex = (page - 1) * safePageSize;
        int toIndex = Math.min(fromIndex + safePageSize, totalListeners);
        List<PluginListenerBinding> pageItems = bindings.subList(fromIndex, toIndex);

        return new PluginListenerQueryResult.Success(new PluginListenerPage(
                pluginName, filteredEventClassName, List.copyOf(pageItems), page, totalPages, totalListeners));
    }

    private List<PluginCoInteraction> mergeCoInteractions(
            PluginListenerInventory inventory, PluginTraceStatistics traceStatistics) {
        Set<String> pluginNames = new HashSet<>();
        pluginNames.addAll(inventory.registryCoInteractions().keySet());
        pluginNames.addAll(traceStatistics.traceCoInteractions().keySet());

        List<PluginCoInteraction> interactions = new ArrayList<>();
        for (String otherPlugin : pluginNames) {
            interactions.add(new PluginCoInteraction(
                    otherPlugin,
                    inventory.registryCoInteractions().getOrDefault(otherPlugin, 0),
                    traceStatistics.traceCoInteractions().getOrDefault(otherPlugin, 0)));
        }

        interactions.sort(Comparator.comparingInt(PluginCoInteraction::totalScore)
                .thenComparing(PluginCoInteraction::pluginName, String.CASE_INSENSITIVE_ORDER)
                .reversed());
        return List.copyOf(interactions);
    }

    private boolean isAgentPresent() {
        return instrumentationPort != null && instrumentationPort.isAgentPresent();
    }

    private String simpleEventName(String eventClassName) {
        int lastDot = eventClassName.lastIndexOf('.');
        return lastDot >= 0 ? eventClassName.substring(lastDot + 1) : eventClassName;
    }
}
