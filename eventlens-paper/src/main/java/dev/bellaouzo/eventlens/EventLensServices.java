package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.application.BaselineCommandService;
import dev.bellaouzo.eventlens.application.DashboardQueryService;
import dev.bellaouzo.eventlens.application.EventCatalogService;
import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import dev.bellaouzo.eventlens.application.EventLensCommandContext;
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
import dev.bellaouzo.eventlens.command.EventLensCommand;
import dev.bellaouzo.eventlens.paper.dashboard.PaperDashboardHttpServer;
import org.bukkit.plugin.java.JavaPlugin;

final class EventLensServices {

    private EventLensServices() {}

    record Context(
            StatusQueryService statusQueryService,
            ListenerQueryService listenerQueryService,
            PluginQueryService pluginQueryService,
            TraceCommandService traceCommandService,
            TraceLiveFeedService traceLiveFeedService,
            ExportCommandService exportCommandService,
            BaselineCommandService baselineCommandService,
            InstrumentationTestService instrumentationTestService,
            ReportRetentionService reportRetentionService,
            PlayerPreferencesService playerPreferencesService,
            DashboardQueryService dashboardQueryService,
            PaperDashboardHttpServer dashboardHttpServer,
            EventLensCommandConfig commandConfig,
            LiveFeedConfig liveFeedConfig,
            EventCatalogService eventCatalogService,
            ExceptionInboxService exceptionInboxService,
            TraceCorrelateService traceCorrelateService) {}

    static Context create(EventLensServiceFactory.BootstrapInput input) {
        return EventLensServiceFactory.create(input);
    }

    static void registerSchedulers(JavaPlugin plugin, Context context) {
        plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, context.traceCommandService()::expireSessions, 20L * 60L, 20L * 60L);
        plugin.getServer()
                .getScheduler()
                .runTaskTimer(plugin, () -> context.traceLiveFeedService().tick(System.currentTimeMillis()), 20L, 20L);
    }

    static EventLensCommand createCommand(Context context) {
        return new EventLensCommand(new EventLensCommandContext(
                context.statusQueryService(),
                context.listenerQueryService(),
                context.pluginQueryService(),
                context.traceCommandService(),
                context.traceLiveFeedService(),
                context.exportCommandService(),
                context.baselineCommandService(),
                context.instrumentationTestService(),
                context.playerPreferencesService(),
                context.commandConfig(),
                context.liveFeedConfig(),
                context.eventCatalogService(),
                context.exceptionInboxService(),
                context.traceCorrelateService()));
    }
}
