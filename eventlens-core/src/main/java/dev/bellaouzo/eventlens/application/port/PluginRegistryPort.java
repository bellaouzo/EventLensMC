package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.domain.plugin.PluginDescriptor;
import dev.bellaouzo.eventlens.domain.plugin.PluginSearchResult;
import java.util.List;
import java.util.Optional;

public interface PluginRegistryPort {

    PluginSearchResult searchPlugins(String query);

    Optional<PluginDescriptor> getDescriptor(String pluginName);

    List<String> listPluginNames();
}
