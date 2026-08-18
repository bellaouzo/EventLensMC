package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.BaselineCommandService;
import dev.bellaouzo.eventlens.application.DashboardQueryService;
import dev.bellaouzo.eventlens.application.DashboardStreamHub;
import dev.bellaouzo.eventlens.application.DashboardStreamNotifier;
import dev.bellaouzo.eventlens.application.EventCatalogService;
import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.EventLensDashboardConfig;
import dev.bellaouzo.eventlens.application.EventLensReportConfig;
import dev.bellaouzo.eventlens.application.ExceptionInboxService;
import dev.bellaouzo.eventlens.application.ExportCommandService;
import dev.bellaouzo.eventlens.application.InstrumentationTestService;
import dev.bellaouzo.eventlens.application.ListenerQueryService;
import dev.bellaouzo.eventlens.application.LiveFeedConfig;
import dev.bellaouzo.eventlens.application.PlayerPreferencesService;
import dev.bellaouzo.eventlens.application.PluginQueryService;
import dev.bellaouzo.eventlens.application.ReportRetentionService;
import dev.bellaouzo.eventlens.application.StatusQueryService;
import dev.bellaouzo.eventlens.application.TraceCommandService;
import dev.bellaouzo.eventlens.application.TraceCorrelateService;
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
import dev.bellaouzo.eventlens.paper.dashboard.PaperDashboardHttpServer;
import dev.bellaouzo.eventlens.paper.dashboard.PaperDashboardServerContextAdapter;
import dev.bellaouzo.eventlens.paper.instrumentation.PaperInstrumentationTestAdapter;
import dev.bellaouzo.eventlens.paper.snapshot.PaperEventSnapshotRegistry;
import dev.bellaouzo.eventlens.trace.CompositeDispatchCaptureListener;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.util.List;
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
            LiveFeedConfig liveFeedConfig,
            EventLensDashboardConfig dashboardConfig) {}

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
        ExceptionInboxService exceptionInboxService = new ExceptionInboxService();

        DashboardStreamHub dashboardStreamHub = new DashboardStreamHub();
        DashboardStreamNotifier dashboardStreamNotifier =
                new DashboardStreamNotifier(dashboardStreamHub, input.traceSessionManager());
        input.traceSessionManager()
                .setDispatchCaptureListener(new CompositeDispatchCaptureListener(
                        List.of(traceLiveFeedService, dashboardStreamNotifier, exceptionInboxService),
                        List.of(traceLiveFeedService, dashboardStreamNotifier)));

        PaperExportAdapter exportAdapter = new PaperExportAdapter(input.plugin());
        ReportRetentionService reportRetentionService = new ReportRetentionService(exportAdapter, input.reportConfig());
        TraceReportBuilder traceReportBuilder = new TraceReportBuilder(
                new PaperEnvironmentCollector(input.plugin(), input.targetPlatform()),
                input.instrumentationPort(),
                input.targetPlatform());
        ExportCommandService exportCommandService = new ExportCommandService(
                input.traceSessionManager(), traceReportBuilder, exportAdapter, reportRetentionService);
        DashboardQueryService dashboardQueryService = new DashboardQueryService(
                input.traceSessionManager(),
                exportCommandService,
                exportAdapter,
                listenerRegistry,
                input.instrumentationPort(),
                new PaperDashboardServerContextAdapter(input.plugin()),
                input.dashboardConfig());
        PaperDashboardHttpServer dashboardHttpServer = EventLensDashboardBootstrap.register(
                input.plugin(), input.dashboardConfig(), dashboardQueryService, dashboardStreamHub);
        BaselineCommandService baselineCommandService = new BaselineCommandService(exportCommandService, exportAdapter);
        InstrumentationTestService instrumentationTestService =
                new InstrumentationTestService(input.instrumentationPort(), instrumentationTestAdapter);

        PlayerPreferencesPort preferencesPort = new YamlPlayerPreferencesStore(input.plugin());
        PlayerPreferencesService playerPreferencesService =
                new PlayerPreferencesService(preferencesPort, input.commandConfig());
        EventCatalogService eventCatalogService =
                new EventCatalogService(listenerRegistry, new PaperEventSnapshotRegistry());
        TraceCorrelateService traceCorrelateService =
                new TraceCorrelateService(input.traceSessionManager(), exportCommandService);

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
                dashboardQueryService,
                dashboardHttpServer,
                input.commandConfig(),
                input.liveFeedConfig(),
                eventCatalogService,
                exceptionInboxService,
                traceCorrelateService);
    }
}
