package dev.bellaouzo.eventlens.application;

public record EventLensCommandContext(
        StatusQueryService statusQueryService,
        ListenerQueryService listenerQueryService,
        PluginQueryService pluginQueryService,
        TraceCommandService traceCommandService,
        TraceLiveFeedService traceLiveFeedService,
        ExportCommandService exportCommandService,
        BaselineCommandService baselineCommandService,
        InstrumentationTestService instrumentationTestService,
        PlayerPreferencesService playerPreferencesService,
        EventLensCommandConfig commandConfig,
        LiveFeedConfig liveFeedConfig,
        EventCatalogService eventCatalogService,
        ExceptionInboxService exceptionInboxService,
        TraceCorrelateService traceCorrelateService) {}
