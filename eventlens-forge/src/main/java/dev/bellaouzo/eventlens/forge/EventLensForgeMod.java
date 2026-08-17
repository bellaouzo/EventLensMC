package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.modcommon.FileModExportAdapter;
import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModEnvironmentCollector;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensClientAccess;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensUiPreferences;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.nio.file.Path;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(EventLensForgeMod.MOD_ID)
public final class EventLensForgeMod {

    public static final String MOD_ID = "eventlens";

    private static ModTraceCoordinator coordinator;
    private static ForgeListenerRegistry listenerRegistry;
    private static EventLensUiPreferences uiPreferences;

    public EventLensForgeMod(FMLJavaModLoadingContext context) {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
        uiPreferences = EventLensUiPreferences.load(configDir);
        TraceSessionManager sessionManager = new TraceSessionManager();
        ForgeClientInstrumentation.Result instrumentation = ForgeClientInstrumentation.create(sessionManager);
        sessionManager.setInstrumentationPort(instrumentation.port());
        ForgeEnvironmentAdapter environmentAdapter = new ForgeEnvironmentAdapter(context);
        ModEnvironmentCollector environmentCollector = new ModEnvironmentCollector(environmentAdapter);
        TraceReportBuilder reportBuilder =
                new TraceReportBuilder(environmentCollector, instrumentation.port(), environmentAdapter.platformLabel());
        FileModExportAdapter exportAdapter = new FileModExportAdapter(configDir);
        listenerRegistry = new ForgeListenerRegistry();
        coordinator = new ModTraceCoordinator(
                sessionManager, reportBuilder, exportAdapter, listenerRegistry, environmentAdapter);
        EventLensClientAccess.bind(coordinator, uiPreferences);
        ModDispatchRecorder recorder = instrumentation.recorder();
        MinecraftForge.EVENT_BUS.register(new ForgeEventTracer(recorder));
        MinecraftForge.EVENT_BUS.register(new ForgeInputTracer(recorder));
        MinecraftForge.EVENT_BUS.register(new ForgeWorldTracer(recorder));
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(ForgeClientEvents::registerKeys);
        modBus.addListener(ForgeClientEvents::registerHud);
    }

    public static ModTraceCoordinator coordinator() {
        return coordinator;
    }

    public static ForgeListenerRegistry listenerRegistry() {
        return listenerRegistry;
    }

    public static EventLensUiPreferences uiPreferences() {
        return uiPreferences;
    }
}
