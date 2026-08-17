package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import dev.bellaouzo.eventlens.modcommon.FileModExportAdapter;
import dev.bellaouzo.eventlens.modcommon.ModEnvironmentCollector;
import dev.bellaouzo.eventlens.modcommon.ModNoOpInstrumentationAdapter;
import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.port.ModEnvironmentPort;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class EventLensFabricMod implements ClientModInitializer {

    private static ModTraceCoordinator coordinator;

    @Override
    public void onInitializeClient() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("eventlens");
        TraceSessionManager sessionManager = new TraceSessionManager();
        ModNoOpInstrumentationAdapter instrumentation = new ModNoOpInstrumentationAdapter();
        sessionManager.setInstrumentationPort(instrumentation);
        FabricEnvironmentAdapter environmentAdapter = new FabricEnvironmentAdapter();
        ModEnvironmentCollector environmentCollector = new ModEnvironmentCollector(environmentAdapter);
        TraceReportBuilder reportBuilder =
                new TraceReportBuilder(environmentCollector, instrumentation, environmentAdapter.platformLabel());
        FileModExportAdapter exportAdapter = new FileModExportAdapter(configDir);
        FabricListenerRegistry listenerRegistry = new FabricListenerRegistry();
        coordinator = new ModTraceCoordinator(
                sessionManager, reportBuilder, exportAdapter, listenerRegistry, environmentAdapter);
        FabricEventTracer.register(new ModDispatchRecorder(sessionManager));
        FabricClientCommands.register(coordinator);
    }

    public static ModTraceCoordinator coordinator() {
        return coordinator;
    }

    static final class FabricEnvironmentAdapter implements ModEnvironmentPort {
        @Override
        public ModRuntimeKind runtimeKind() {
            return ModRuntimeKind.FABRIC;
        }

        @Override
        public String loaderVersion() {
            return FabricLoader.getInstance().getModContainer("fabricloader")
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("unknown");
        }

        @Override
        public Map<String, String> loadedModVersions() {
            Map<String, String> versions = new LinkedHashMap<>();
            FabricLoader.getInstance().getAllMods().forEach(mod -> {
                if (versions.size() >= 64) {
                    return;
                }
                versions.put(mod.getMetadata().getId(), mod.getMetadata().getVersion().getFriendlyString());
            });
            return versions;
        }

        @Override
        public String platformLabel() {
            return "Fabric " + loaderVersion();
        }

        @Override
        public String minecraftVersion() {
            return FabricLoader.getInstance().getModContainer("minecraft")
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("1.21.1");
        }

        @Override
        public String eventLensVersion() {
            return FabricLoader.getInstance().getModContainer("eventlens")
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("1.0.0");
        }
    }
}
