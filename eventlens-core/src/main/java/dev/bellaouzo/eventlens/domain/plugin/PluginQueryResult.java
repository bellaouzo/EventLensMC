package dev.bellaouzo.eventlens.domain.plugin;

import java.util.List;

public sealed interface PluginQueryResult {

    record NotFound(String query) implements PluginQueryResult {}

    record Ambiguous(List<String> candidateNames) implements PluginQueryResult {}

    record Success(PluginProfile profile) implements PluginQueryResult {}
}
