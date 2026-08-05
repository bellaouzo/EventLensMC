package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.BaselineCommandService;
import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.EventLensReportConfig;
import dev.bellaouzo.eventlens.application.ExportCommandService;
import dev.bellaouzo.eventlens.application.InstrumentationTestService;
import dev.bellaouzo.eventlens.application.ListenerQueryService;
import dev.bellaouzo.eventlens.application.LiveFeedConfig;
import dev.bellaouzo.eventlens.application.PlayerPreferencesService;
import dev.bellaouzo.eventlens.application.PluginQueryService;
import dev.bellaouzo.eventlens.application.ReportRetentionService;
import dev.bellaouzo.eventlens.application.StatusQueryService;
import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.application.TraceLiveFeedService;
import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.application.port.InstrumentationPort;
import dev.bellaouzo.eventlens.application.port.PlayerPreferencesPort;
import dev.bellaouzo.eventlens.paper.PaperEnvironmentCollector;
import dev.bellaouzo.eventlens.paper.PaperExportAdapter;
import dev.bellaouzo.eventlens.paper.PaperListenerRegistry;
import dev.bellaouzo.eventlens.paper.PaperPluginRegistry;
import dev.bellaouzo.eventlens.paper.PaperTraceHookManager;
import dev.bellaouzo.eventlens.paper.YamlPlayerPreferencesStore;
import dev.bellaouzo.eventlens.paper.instrumentation.PaperInstrumentationTestAdapter;
import dev.bellaouzo.eventlens.paper.snapshot.PaperEventSnapshotRegistry;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("java:S6539")
final class EventLensServiceFactory {

    record BootstrapInput(
            JavaPlugin plugin,
            ClassLoader pluginClassLoader,
            TraceSessionManager traceSessionManager,
            InstrumentationPort instrumentationPort,
            String targetPlatform,
            EventLensReportConfig reportConfig,
            EventLensCommandConfig commandConfig,
            LiveFeedConfig liveFeedConfig) {}

    private EventLensServiceFactory() {}

    static EventLensServices.Context create(BootstrapInput input) {
        PaperListenerRegistry listenerRegistry = new PaperListenerRegistry(input.pluginClassLoader());
        PaperPluginRegistry pluginRegistry = new PaperPluginRegistry();
        PaperInstrumentationTestAdapter instrumentationTestAdapter =
                new PaperInstrumentationTestAdapter(input.plugin());
        PaperTraceHookManager traceHookManager = new PaperTraceHookManager(
                input.plugin(),
                input.traceSessionManager(),
                listenerRegistry,
                input.instrumentationPort(),
                input.pluginClassLoader());

        StatusQueryService statusQueryService = new StatusQueryService(
                input.traceSessionManager(),
                input.instrumentationPort(),
                instrumentationTestAdapter,
                input.plugin().getPluginMeta().getVersion(),
                input.targetPlatform());
        ListenerQueryService listenerQueryService = new ListenerQueryService(listenerRegistry);
        PluginQueryService pluginQueryService = new PluginQueryService(
                pluginRegistry, listenerRegistry, input.traceSessionManager(), input.instrumentationPort());
        TraceCommandService traceCommandService = new TraceCommandService(
                input.traceSessionManager(),
                listenerRegistry,
                traceHookManager,
                input.commandConfig(),
                new PaperEventSnapshotRegistry());

        TraceLiveFeedService traceLiveFeedService = EventLensLiveFeedBootstrap.register(
                input.plugin(), input.traceSessionManager(), input.liveFeedConfig());

        PaperExportAdapter exportAdapter = new PaperExportAdapter(input.plugin());
        ReportRetentionService reportRetentionService = new ReportRetentionService(exportAdapter, input.reportConfig());
        TraceReportBuilder traceReportBuilder = new TraceReportBuilder(
                new PaperEnvironmentCollector(input.plugin(), input.targetPlatform()),
                input.instrumentationPort(),
                input.targetPlatform());
        ExportCommandService exportCommandService = new ExportCommandService(
                input.traceSessionManager(), traceReportBuilder, exportAdapter, reportRetentionService);
        BaselineCommandService baselineCommandService = new BaselineCommandService(exportCommandService, exportAdapter);
        InstrumentationTestService instrumentationTestService =
                new InstrumentationTestService(input.instrumentationPort(), instrumentationTestAdapter);

        PlayerPreferencesPort preferencesPort = new YamlPlayerPreferencesStore(input.plugin());
        PlayerPreferencesService playerPreferencesService =
                new PlayerPreferencesService(preferencesPort, input.commandConfig());

        return new EventLensServices.Context(
                statusQueryService,
                listenerQueryService,
                pluginQueryService,
                traceCommandService,
                traceLiveFeedService,
                exportCommandService,
                baselineCommandService,
                instrumentationTestService,
                reportRetentionService,
                playerPreferencesService,
                input.commandConfig(),
                input.liveFeedConfig());
    }
}
