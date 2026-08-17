package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import org.bukkit.plugin.java.JavaPlugin;

final class EventLensStartupMessages {

    private EventLensStartupMessages() {}

    static void log(JavaPlugin plugin, InstrumentationPort instrumentationPort, EventLensCommandConfig commandConfig) {
        if (instrumentationPort.isAgentPresent()) {
            plugin.getLogger()
                    .info(() -> "EventLens agent attached (protocol " + instrumentationPort.protocolVersion()
                            + "). Per-listener timing enabled.");
        } else {
            plugin.getLogger()
                    .warning("EventLens agent not detected. Per-listener timing unavailable; dispatch timing only.");
        }
        plugin.getLogger()
                .info(() -> "EventLens v" + plugin.getPluginMeta().getVersion()
                        + " enabled. Commands: /eventlens status, listeners, trace, plugin.");
        if (!commandConfig.presets().isEmpty()) {
            plugin.getLogger()
                    .info(() -> "Loaded " + commandConfig.presets().size() + " trace preset(s): "
                            + String.join(", ", commandConfig.presets().keySet()));
        }
    }
}
