package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.modcommon.FileModExportAdapter;
import dev.bellaouzo.eventlens.modcommon.ModAgentDiagnostics;
import dev.bellaouzo.eventlens.modcommon.ModCorrelationBridge;
import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModEnvironmentCollector;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensClientAccess;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensClientEvents;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensUiPreferences;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.nio.file.Path;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;

@Mod(EventLensNeoForgeMod.MOD_ID)
public final class EventLensNeoForgeMod {

    public static final String MOD_ID = "eventlens";

    private static ModTraceCoordinator coordinator;
    private static NeoForgeListenerRegistry listenerRegistry;
    private static EventLensUiPreferences uiPreferences;

    public EventLensNeoForgeMod(ModContainer modContainer) {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
        uiPreferences = EventLensUiPreferences.load(configDir);
        TraceSessionManager sessionManager = new TraceSessionManager();
        NeoForgeClientInstrumentation.Result instrumentation = NeoForgeClientInstrumentation.create(sessionManager);
        sessionManager.setInstrumentationPort(instrumentation.port());
        ModAgentDiagnostics.logStartupWarnings(instrumentation.port().isAgentPresent());
        NeoForgeEnvironmentAdapter environmentAdapter = new NeoForgeEnvironmentAdapter(modContainer);
        ModEnvironmentCollector environmentCollector = new ModEnvironmentCollector(environmentAdapter);
        TraceReportBuilder reportBuilder =
                new TraceReportBuilder(environmentCollector, instrumentation.port(), environmentAdapter.platformLabel());
        FileModExportAdapter exportAdapter = new FileModExportAdapter(configDir);
        listenerRegistry = new NeoForgeListenerRegistry();
        coordinator = new ModTraceCoordinator(
                sessionManager, reportBuilder, exportAdapter, listenerRegistry, environmentAdapter);
        NeoForgeCorrelationChannel correlationChannel = new NeoForgeCorrelationChannel();
        ModCorrelationBridge correlationBridge = new ModCorrelationBridge(sessionManager, correlationChannel);
        correlationChannel.bind(correlationBridge);
        sessionManager.setDispatchCaptureListener(correlationBridge);
        NeoForgeCorrelationChannel.register(modContainer.getEventBus(), correlationChannel);
        EventLensClientAccess.bind(coordinator, uiPreferences);
        ModDispatchRecorder recorder = instrumentation.recorder();
        NeoForge.EVENT_BUS.register(new NeoForgeEventTracer(recorder));
        NeoForge.EVENT_BUS.register(new NeoForgePlayerStateTracer(recorder));
        NeoForge.EVENT_BUS.register(new NeoForgeInputTracer(recorder));
        NeoForge.EVENT_BUS.register(new NeoForgeWorldTracer(recorder));
        NeoForge.EVENT_BUS.register(new NeoForgeGameplayTracer(recorder));
        modContainer.getEventBus().addListener(EventLensClientEvents::registerKeys);
        modContainer.getEventBus().addListener(EventLensClientEvents::registerHud);
    }

    public static ModTraceCoordinator coordinator() {
        return coordinator;
    }

    public static NeoForgeListenerRegistry listenerRegistry() {
        return listenerRegistry;
    }

    public static EventLensUiPreferences uiPreferences() {
        return uiPreferences;
    }
}
