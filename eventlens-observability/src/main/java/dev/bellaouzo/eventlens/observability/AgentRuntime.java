package dev.bellaouzo.eventlens.observability;

public final class AgentRuntime {

    private static volatile long slowThresholdNanos = 1_000_000L;
    private static volatile boolean captureStacks;
    private static volatile ListenerSnapshotBridge listenerSnapshotBridge;

    private AgentRuntime() {}

    public static long slowThresholdNanos() {
        return slowThresholdNanos;
    }

    public static void setSlowThresholdNanos(long thresholdNanos) {
        slowThresholdNanos = Math.max(0L, thresholdNanos);
    }

    public static boolean captureStacks() {
        return captureStacks;
    }

    public static void setCaptureStacks(boolean enabled) {
        captureStacks = enabled;
    }

    public static boolean isAgentLoaded() {
        return Boolean.getBoolean("dev.bellaouzo.eventlens.agent.loaded");
    }

    public static void markAgentLoaded() {
        System.setProperty("dev.bellaouzo.eventlens.agent.loaded", "true");
    }

    public static void setListenerSnapshotBridge(ListenerSnapshotBridge bridge) {
        listenerSnapshotBridge = bridge;
    }

    public static ListenerSnapshotBridge listenerSnapshotBridge() {
        return listenerSnapshotBridge;
    }

    public static boolean listenerSnapshotsEnabled() {
        return listenerSnapshotBridge != null;
    }
}
