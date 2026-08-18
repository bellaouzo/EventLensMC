package dev.bellaouzo.eventlens.modcommon.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.TraceReportBuilder;
import dev.bellaouzo.eventlens.application.port.ExportPort;
import dev.bellaouzo.eventlens.domain.report.ExportFormat;
import dev.bellaouzo.eventlens.domain.runtime.ModRuntimeKind;
import dev.bellaouzo.eventlens.modcommon.ModEnvironmentCollector;
import dev.bellaouzo.eventlens.modcommon.ModHandlerRegistration;
import dev.bellaouzo.eventlens.modcommon.ModNoOpInstrumentationAdapter;
import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.port.ModEnvironmentPort;
import dev.bellaouzo.eventlens.modcommon.port.ModListenerRegistryPort;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ModClientCommandsTest {

    private ModTraceCoordinator coordinator;

    @BeforeEach
    void setUp() {
        TraceSessionManager sessions = new TraceSessionManager();
        ModNoOpInstrumentationAdapter instrumentation = new ModNoOpInstrumentationAdapter();
        sessions.setInstrumentationPort(instrumentation);
        StubEnvironment environment = new StubEnvironment();
        coordinator = new ModTraceCoordinator(
                sessions,
                new TraceReportBuilder(new ModEnvironmentCollector(environment), instrumentation, environment.platformLabel()),
                new StubExport(),
                new EmptyListeners(),
                environment);
    }

    @Test
    void statusIncludesClickableActions() {
        List<ModChatLine> lines = ModClientCommands.execute(coordinator, "Dev", List.of("status"));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "[Events]")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "[Sessions]")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "[Open UI]")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "dispatch-only")));
    }

    @Test
    void hotEventRequiresConfirmThenStarts() {
        List<ModChatLine> prompt =
                ModClientCommands.execute(coordinator, "Dev", List.of("trace", "start", "ClientTickEvent"));
        assertTrue(prompt.stream().anyMatch(line -> contains(line, "[Start anyway]")));
        List<ModChatLine> started = ModClientCommands.execute(
                coordinator, "Dev", List.of("trace", "start", "ClientTickEvent", "--confirm-hot"));
        assertTrue(started.stream().anyMatch(line -> contains(line, "Session started")));
        assertTrue(started.stream().anyMatch(line -> contains(line, "[Open session]")));
    }

    @Test
    void listAndViewAreClickable() {
        ModClientCommands.execute(coordinator, "Dev", List.of("trace", "start", "ClientChatEvent"));
        List<ModChatLine> list = ModClientCommands.execute(coordinator, "Dev", List.of("trace", "list"));
        assertTrue(list.stream().anyMatch(line -> contains(line, "ClientChatEvent")));
        String sessionId = coordinator.listSessions().getFirst().sessionId();
        List<ModChatLine> view = ModClientCommands.execute(coordinator, "Dev", List.of("trace", "view", sessionId));
        assertTrue(view.stream().anyMatch(line -> contains(line, sessionId)));
        assertTrue(view.stream().anyMatch(line -> contains(line, "[Refresh]")));
        List<ModChatLine> status = ModClientCommands.execute(coordinator, "Dev", List.of("status"));
        assertTrue(status.stream().anyMatch(line -> contains(line, sessionId)));
    }

    @Test
    void listenersListsClickableStarts() {
        List<ModChatLine> lines = ModClientCommands.execute(coordinator, "Dev", List.of("listeners"));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "ClientAttackBlockEvent")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "[Start]")));
    }

    @Test
    void listenersExplainEmptyHandlerList() {
        List<ModChatLine> lines =
                ModClientCommands.execute(coordinator, "Dev", List.of("listeners", "ClientChatEvent"));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "None listed")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "addListener")));
    }

    @Test
    void listenersShowOverlapBetweenMods() {
        coordinator = coordinatorWithHandlers(List.of(
                new ModHandlerRegistration("jei", "jei.ChatHook", "onChat", 2),
                new ModHandlerRegistration("chatplus", "chatplus.Filter", "onChat", 1)));
        List<ModChatLine> lines =
                ModClientCommands.execute(coordinator, "Dev", List.of("listeners", "ClientChatEvent"));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "Overlap")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "jei")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "chatplus")));
    }

    @Test
    void uiExplainsFabricHasLighterScreen() {
        List<ModChatLine> lines = ModClientCommands.execute(coordinator, "Dev", List.of("ui"));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "/eventlens ui")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "Fabric, NeoForge, and Forge")));
    }

    @Test
    void fabricModProfileIsHonestAboutCoarseList() {
        coordinator = coordinatorForRuntime(
                ModRuntimeKind.FABRIC, List.of(new ModHandlerRegistration("jei", "fabric.callback", "unknown", 0)));
        List<ModChatLine> lines = ModClientCommands.execute(coordinator, "Dev", List.of("mod", "jei"));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "loaded-mod placeholder")));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "not a callback inventory")));
    }

    @Test
    void startPresetClickFlow() {
        List<ModChatLine> lines =
                ModClientCommands.execute(coordinator, "Dev", List.of("trace", "start", "--preset", "click-flow"));
        assertTrue(lines.stream().anyMatch(line -> contains(line, "Session started")));
        assertTrue(coordinator.listSessions().getFirst().eventClassName().contains("ClientUseItemEvent"));
    }

    @Test
    void tabCompletesPresetAndClickFlow() {
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace", "start"), "click")
                .contains("click-flow"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace", "start"), "--p")
                .contains("--preset"));
    }

    @Test
    void tabCompletesModAndExceptions() {
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of(), "m").contains("mod"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of(), "ex").contains("exceptions"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("mod"), "c").contains("compare"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("exceptions"), "").contains("1"));
    }

    @Test
    void tabCompletesEventsAndFlags() {
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of(), "u").contains("ui"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace"), "s").contains("start"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace"), "p").contains("pause"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace"), "r").contains("resume"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace"), "r").contains("restart"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace", "start"), "ClientC")
                .contains("ClientChatEvent"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace", "start"), "hurt")
                .contains("ClientHurtEvent"));
        assertTrue(ModClientTabCompleter.matchingEventNames("explode").isEmpty());
        assertTrue(ModClientTabCompleter.matchingEventNames("hurt").contains("ClientHurtEvent"));
        assertTrue(ModClientTabCompleter.complete(
                        coordinator, List.of("trace", "start", "ClientTickEvent"), "--c")
                .contains("--confirm-hot"));
        List<String> afterConfirm = ModClientTabCompleter.completeStartFlagSuggestions(
                "ClientTickEvent", "--confirm-hot");
        assertTrue(afterConfirm.contains("--confirm-hot --max-events"));
        assertTrue(afterConfirm.contains("--confirm-hot --mod"));
        assertTrue(afterConfirm.contains("--confirm-hot --player"));
        assertTrue(afterConfirm.stream().noneMatch(value -> value.equals("--confirm-hot --confirm-hot")));
        List<String> afterConfirmSpace = ModClientTabCompleter.completeStartFlagSuggestions(
                "ClientTickEvent", "--confirm-hot ");
        assertTrue(afterConfirmSpace.contains("--confirm-hot --max-events"));
    }

    @Test
    void statusHoverExplainsIdleAndDispatch() {
        var status = coordinator.status();
        assertTrue(ModStatusHover.tracingLines(status).stream().anyMatch(line -> line.contains("idle")));
        assertTrue(ModStatusHover.instrumentationLines(status).stream().anyMatch(line -> line.contains("not attached")));
        assertTrue(ModStatusHover.instrumentationLines(status).stream().anyMatch(line -> line.contains("1.0.0")));
    }

    @Test
    void pauseAndResumeSession() {
        ModClientCommands.execute(coordinator, "Dev", List.of("trace", "start", "ClientChatEvent"));
        String sessionId = coordinator.listSessions().getFirst().sessionId();
        List<ModChatLine> paused =
                ModClientCommands.execute(coordinator, "Dev", List.of("trace", "pause", sessionId));
        assertTrue(paused.stream().anyMatch(line -> contains(line, "Paused")));
        assertTrue(paused.stream().anyMatch(line -> contains(line, "[Resume]")));
        List<ModChatLine> resumed =
                ModClientCommands.execute(coordinator, "Dev", List.of("trace", "resume", sessionId));
        assertTrue(resumed.stream().anyMatch(line -> contains(line, "Resumed")));
        assertTrue(resumed.stream().anyMatch(line -> contains(line, "[Pause]")));
    }

    @Test
    void restartStoppedSessionReusesSameId() {
        ModClientCommands.execute(coordinator, "Dev", List.of("trace", "start", "ClientChatEvent"));
        String sessionId = coordinator.listSessions().getFirst().sessionId();
        ModClientCommands.execute(coordinator, "Dev", List.of("trace", "stop", sessionId));
        List<ModChatLine> restarted =
                ModClientCommands.execute(coordinator, "Dev", List.of("trace", "restart", sessionId));
        assertTrue(restarted.stream().anyMatch(line -> contains(line, "RESTARTED")));
        assertTrue(restarted.stream().anyMatch(line -> contains(line, sessionId)));
        assertTrue(restarted.stream().anyMatch(line -> contains(line, "previous run kept")));
        assertTrue(restarted.stream().anyMatch(line -> contains(line, "[Open session]")));
        assertEquals(sessionId, coordinator.listSessions().getFirst().sessionId());
        assertTrue(coordinator.listSessions().getFirst().restarted());
        assertEquals(2, coordinator.listGenerations(sessionId).size());
        List<ModChatLine> previous = ModClientCommands.execute(
                coordinator, "Dev", List.of("trace", "view", sessionId, "--run", "1"));
        assertTrue(previous.stream().anyMatch(line -> contains(line, sessionId)));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace", "view", sessionId), "--r")
                .contains("--run"));
        ModClientCommands.execute(coordinator, "Dev", List.of("trace", "stop", sessionId));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace", "restart"), sessionId.substring(0, 1))
                .contains(sessionId));
    }

    @Test
    void exportPathIsClickToCopy() {
        ModClientCommands.execute(coordinator, "Dev", List.of("trace", "start", "ClientChatEvent"));
        String sessionId = coordinator.listSessions().getFirst().sessionId();
        List<ModChatLine> exported =
                ModClientCommands.execute(coordinator, "Dev", List.of("trace", "export", sessionId));
        assertTrue(exported.stream().anyMatch(line -> contains(line, "Saved to")));
        assertTrue(exported.stream().anyMatch(line -> contains(line, "Folder")));
        assertTrue(exported.stream().anyMatch(line -> line.spans().stream()
                .anyMatch(span -> span.clickKind() == dev.bellaouzo.eventlens.modcommon.chat.ModChatClick.COPY)));
        var toast = ModCommandNotices.toastMessage(List.of("trace", "export", sessionId), exported);
        assertTrue(toast.isPresent());
        assertTrue(toast.get().startsWith("Exported"));
        assertTrue(toast.get().indexOf('\\') < 0);
        assertTrue(toast.get().indexOf('/') < 0);
        assertTrue(!toast.get().contains("Saved to"));
        assertTrue(ModClientTabCompleter.complete(coordinator, List.of("trace", "export", sessionId), "--f")
                .contains("--format"));
        assertTrue(ModClientTabCompleter.complete(
                        coordinator, List.of("trace", "export", sessionId, "--format"), "ht")
                .contains("html"));
        List<ModChatLine> html = ModClientCommands.execute(
                coordinator, "Dev", List.of("trace", "export", sessionId, "--format", "html"));
        assertTrue(html.stream().anyMatch(line -> contains(line, "Saved to")));
        List<ModChatLine> bundle = ModClientCommands.execute(
                coordinator, "Dev", List.of("trace", "export", sessionId, "--format", "bundle"));
        assertTrue(bundle.stream().anyMatch(line -> contains(line, "bundle")));
    }

    private static boolean contains(ModChatLine line, String text) {
        return line.spans().stream().anyMatch(span -> span.text().contains(text));
    }

    private ModTraceCoordinator coordinatorWithHandlers(List<ModHandlerRegistration> handlers) {
        return coordinatorForRuntime(ModRuntimeKind.NEOFORGE, handlers);
    }

    private ModTraceCoordinator coordinatorForRuntime(
            ModRuntimeKind runtimeKind, List<ModHandlerRegistration> handlers) {
        TraceSessionManager sessions = new TraceSessionManager();
        ModNoOpInstrumentationAdapter instrumentation = new ModNoOpInstrumentationAdapter();
        sessions.setInstrumentationPort(instrumentation);
        StubEnvironment environment = new StubEnvironment(runtimeKind);
        return new ModTraceCoordinator(
                sessions,
                new TraceReportBuilder(
                        new ModEnvironmentCollector(environment), instrumentation, environment.platformLabel()),
                new StubExport(),
                ignored -> handlers,
                environment);
    }

    private static final class EmptyListeners implements ModListenerRegistryPort {
        @Override
        public List<ModHandlerRegistration> listHandlers(String eventClassName) {
            return List.of();
        }
    }

    private static final class StubEnvironment implements ModEnvironmentPort {
        private final ModRuntimeKind runtimeKind;

        private StubEnvironment() {
            this(ModRuntimeKind.NEOFORGE);
        }

        private StubEnvironment(ModRuntimeKind runtimeKind) {
            this.runtimeKind = runtimeKind;
        }

        @Override
        public ModRuntimeKind runtimeKind() {
            return runtimeKind;
        }

        @Override
        public String loaderVersion() {
            return "test";
        }

        @Override
        public Map<String, String> loadedModVersions() {
            Map<String, String> mods = new LinkedHashMap<>();
            mods.put("eventlens", "test");
            mods.put("jei", "test");
            return mods;
        }

        @Override
        public String platformLabel() {
            return "test";
        }

        @Override
        public String minecraftVersion() {
            return "1.21.1";
        }

        @Override
        public String eventLensVersion() {
            return "1.0.0";
        }
    }

    private static final class StubExport implements ExportPort {
        @Override
        public Path reportsDirectory() {
            return Path.of("build", "tmp", "eventlens-test-reports");
        }

        @Override
        public Path baselinesDirectory() {
            return Path.of("build", "tmp", "eventlens-test-baselines");
        }

        @Override
        public ExportWriteResult writeReport(String safeBaseName, ExportFormat format, String content) {
            return ExportWriteResult.success(reportsDirectory().resolve(safeBaseName + ".json"));
        }

        @Override
        public ExportWriteResult writeBaseline(String safeBaseName, String content) {
            return ExportWriteResult.failure("unused");
        }

        @Override
        public int deleteReportsOlderThan(long cutoffMillis) {
            return 0;
        }

        @Override
        public Optional<String> readReport(String safeBaseName, ExportFormat format) {
            return Optional.empty();
        }

        @Override
        public Optional<String> readBaseline(String safeBaseName) {
            return Optional.empty();
        }

        @Override
        public List<String> listBaselines() {
            return List.of();
        }

        @Override
        public boolean deleteBaseline(String safeBaseName) {
            return false;
        }
    }
}
