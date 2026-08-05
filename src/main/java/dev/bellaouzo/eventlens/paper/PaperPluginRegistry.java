package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.PluginRegistryPort;
import dev.bellaouzo.eventlens.domain.plugin.PluginDescriptor;
import dev.bellaouzo.eventlens.domain.plugin.PluginSearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public final class PaperPluginRegistry implements PluginRegistryPort {

    @Override
    public PluginSearchResult searchPlugins(String query) {
        String normalized = query.trim();
        if (normalized.isEmpty()) {
            return PluginSearchResult.notFound();
        }

        Plugin exact = Bukkit.getPluginManager().getPlugin(normalized);
        if (exact != null) {
            return PluginSearchResult.found(exact.getName());
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String name = plugin.getName();
            if (name.toLowerCase(Locale.ROOT).contains(lower)) {
                matches.add(name);
            }
        }

        matches.sort(String.CASE_INSENSITIVE_ORDER);
        if (matches.isEmpty()) {
            return PluginSearchResult.notFound();
        }
        if (matches.size() == 1) {
            return PluginSearchResult.found(matches.getFirst());
        }
        return PluginSearchResult.ambiguous(matches);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Optional<PluginDescriptor> getDescriptor(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            return Optional.empty();
        }

        PluginDescriptionFile description = plugin.getDescription();
        return Optional.of(new PluginDescriptor(
                plugin.getName(),
                plugin.getPluginMeta().getVersion(),
                plugin.isEnabled(),
                List.copyOf(description.getDepend()),
                List.copyOf(description.getSoftDepend()),
                List.copyOf(description.getLoadBefore()),
                List.copyOf(description.getProvides())));
    }

    @Override
    public List<String> listPluginNames() {
        return java.util.Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
