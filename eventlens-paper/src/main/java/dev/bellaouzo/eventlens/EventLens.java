package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.EventLensDashboardConfig;
import dev.bellaouzo.eventlens.application.EventLensReportConfig;
import dev.bellaouzo.eventlens.application.LiveFeedConfig;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.domain.snapshot.SupportedEventTypes;
import dev.bellaouzo.eventlens.paper.EventLensConfigLoader;
import dev.bellaouzo.eventlens.paper.PaperCorrelationChannel;
import dev.bellaouzo.eventlens.paper.instrumentation.AgentInstrumentationAdapter;
import dev.bellaouzo.eventlens.paper.instrumentation.NoOpInstrumentationAdapter;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EventLens extends JavaPlugin {

    private static final String TARGET_PLATFORM = "Paper 26.2";
    private static final String COMMAND_NAME = "eventlens";
    private static final String AGENT_LOADED_PROPERTY = "dev.bellaouzo.eventlens.agent.loaded";

    private TraceSessionManager traceSessionManager;
    private EventLensServices.Context services;

    @Override
    public void onEnable() {
        EventLensConfigLoader.mergeMissingDefaults(this);
        EventLensReportConfig reportConfig = EventLensConfigLoader.loadReportConfig(getConfig());
        EventLensDashboardConfig dashboardConfig = EventLensConfigLoader.loadDashboardConfig(getConfig());
        EventLensCommandConfig commandConfig = EventLensConfigLoader.loadCommandConfig(getConfig());
        LiveFeedConfig liveFeedConfig = EventLensConfigLoader.loadLiveFeedConfig(getConfig());
        SupportedEventTypes.setAdditionalEventClassNames(EventLensConfigLoader.loadAdditionalTraceEvents(getConfig()));

        traceSessionManager = new TraceSessionManager();
        InstrumentationPort instrumentationPort = createInstrumentationPort();
        traceSessionManager.setInstrumentationPort(instrumentationPort);

        services = EventLensServices.create(new EventLensServiceFactory.BootstrapInput(
                this,
                getClassLoader(),
                traceSessionManager,
                instrumentationPort,
                TARGET_PLATFORM,
                reportConfig,
                commandConfig,
                liveFeedConfig,
                dashboardConfig));
        if (getCommand(COMMAND_NAME) != null) {
            getCommand(COMMAND_NAME).setExecutor(EventLensServices.createCommand(services));
            getCommand(COMMAND_NAME).setTabCompleter(EventLensServices.createCommand(services));
        } else {
            getLogger().severe("Command '" + COMMAND_NAME + "' is missing from plugin.yml.");
        }

        EventLensServices.registerSchedulers(this, services);
        PaperCorrelationChannel.register(this, services.traceCorrelateService());

        int deleted = services.reportRetentionService().cleanupIfEnabled();
        if (deleted > 0) {
            getLogger().info(() -> "Cleaned up " + deleted + " old trace report(s).");
        }

        EventLensStartupMessages.log(this, instrumentationPort, commandConfig);
    }

    @Override
    public void onDisable() {
        PaperCorrelationChannel.unregister(this);
        if (services != null) {
            services.dashboardHttpServer().close();
            services = null;
        }
        if (traceSessionManager != null) {
            traceSessionManager.closeAll();
            traceSessionManager = null;
        }
        getLogger().info("EventLens disabled.");
    }

    public TraceSessionManager getTraceSessionManager() {
        return traceSessionManager;
    }

    private InstrumentationPort createInstrumentationPort() {
        if (Boolean.getBoolean(AGENT_LOADED_PROPERTY)) {
            return AgentInstrumentationAdapter.createAndRegister();
        }
        return new NoOpInstrumentationAdapter();
    }
}
