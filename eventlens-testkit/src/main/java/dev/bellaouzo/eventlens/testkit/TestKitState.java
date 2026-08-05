package dev.bellaouzo.eventlens.testkit;

import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestKitState {

    private static final AtomicReference<TestKitMode> MODE = new AtomicReference<>(TestKitMode.PASSIVE);

    private TestKitState() {}

    public static TestKitMode mode() {
        return MODE.get();
    }

    public static void setMode(TestKitMode mode) {
        MODE.set(mode == null ? TestKitMode.PASSIVE : mode);
    }

    public static boolean scenarioActive() {
        TestKitMode mode = MODE.get();
        return mode == TestKitMode.TRACE || mode == TestKitMode.EXCEPTION || mode == TestKitMode.SLOW;
    }

    public static boolean isTestKitPlugin(JavaPlugin plugin) {
        return plugin != null && "EventLensTestTarget".equals(plugin.getName());
    }
}
