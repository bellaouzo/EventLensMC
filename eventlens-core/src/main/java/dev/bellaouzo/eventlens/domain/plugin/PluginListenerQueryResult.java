package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;

public sealed interface PluginListenerQueryResult {

    record PluginNotFound(String query) implements PluginListenerQueryResult {}

    record PluginAmbiguous(List<String> candidateNames) implements PluginListenerQueryResult {}

    record EventNotFound(String eventQuery) implements PluginListenerQueryResult {}

    record EventAmbiguous(List<String> candidateClassNames) implements PluginListenerQueryResult {}

    record InvalidPage(int requestedPage, int totalPages) implements PluginListenerQueryResult {}

    record Success(PluginListenerPage page) implements PluginListenerQueryResult {}
}
