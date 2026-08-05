package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.EventLensReportConfig;
import dev.bellaouzo.eventlens.application.LiveFeedConfig;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import dev.bellaouzo.eventlens.paper.EventLensConfigLoader;
import dev.bellaouzo.eventlens.paper.instrumentation.AgentInstrumentationAdapter;
import dev.bellaouzo.eventlens.paper.instrumentation.NoOpInstrumentationAdapter;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EventLens extends JavaPlugin {

    private static final String TARGET_PLATFORM = "Paper 26.2";
    private static final String COMMAND_NAME = "eventlens";
    private static final String AGENT_LOADED_PROPERTY = "dev.bellaouzo.eventlens.agent.loaded";

    private TraceSessionManager traceSessionManager;

    @Override
    public void onEnable() {
        EventLensConfigLoader.mergeMissingDefaults(this);
        EventLensReportConfig reportConfig = EventLensConfigLoader.loadReportConfig(getConfig());
        EventLensCommandConfig commandConfig = EventLensConfigLoader.loadCommandConfig(getConfig());
        LiveFeedConfig liveFeedConfig = EventLensConfigLoader.loadLiveFeedConfig(getConfig());
        SupportedEventTypes.setAdditionalEventClassNames(EventLensConfigLoader.loadAdditionalTraceEvents(getConfig()));

        traceSessionManager = new TraceSessionManager();
        InstrumentationPort instrumentationPort = createInstrumentationPort();
        traceSessionManager.setInstrumentationPort(instrumentationPort);

        EventLensServices.Context services = EventLensServices.create(new EventLensServiceFactory.BootstrapInput(
                this,
                getClassLoader(),
                traceSessionManager,
                instrumentationPort,
                TARGET_PLATFORM,
                reportConfig,
                commandConfig,
                liveFeedConfig));
        if (getCommand(COMMAND_NAME) != null) {
            getCommand(COMMAND_NAME).setExecutor(EventLensServices.createCommand(services));
            getCommand(COMMAND_NAME).setTabCompleter(EventLensServices.createCommand(services));
        } else {
            getLogger().severe("Command '" + COMMAND_NAME + "' is missing from plugin.yml.");
        }

        EventLensServices.registerSchedulers(this, services);

        int deleted = services.reportRetentionService().cleanupIfEnabled();
        if (deleted > 0) {
            getLogger().info(() -> "Cleaned up " + deleted + " old trace report(s).");
        }

        logStartup(instrumentationPort, commandConfig);
    }

    @Override
    public void onDisable() {
        if (traceSessionManager != null) {
            traceSessionManager.closeAll();
            traceSessionManager = null;
        }
        getLogger().info("EventLens disabled.");
    }

    public TraceSessionManager getTraceSessionManager() {
        return traceSessionManager;
    }

    private void logStartup(InstrumentationPort instrumentationPort, EventLensCommandConfig commandConfig) {
        if (instrumentationPort.isAgentPresent()) {
            getLogger()
                    .info(() -> "EventLens agent attached (protocol " + instrumentationPort.protocolVersion()
                            + "). Per-listener timing enabled.");
        } else {
            getLogger().warning("EventLens agent not detected. Per-listener timing unavailable; dispatch timing only.");
        }
        getLogger()
                .info(() -> "EventLens v" + getPluginMeta().getVersion() + " enabled for " + TARGET_PLATFORM
                        + ". Commands: /eventlens status, listeners, trace (export, copy, compare, live).");
        if (!commandConfig.presets().isEmpty()) {
            getLogger()
                    .info(() -> "Loaded " + commandConfig.presets().size() + " trace preset(s): "
                            + String.join(", ", commandConfig.presets().keySet()));
        }
    }

    private InstrumentationPort createInstrumentationPort() {
        if (Boolean.getBoolean(AGENT_LOADED_PROPERTY)) {
            return AgentInstrumentationAdapter.createAndRegister();
        }
        return new NoOpInstrumentationAdapter();
    }
}
