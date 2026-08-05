package dev.bellaouzo.eventlens.paper;

public final class EventLensCheckpointMarker {

    private static final ThreadLocal<Long> CHECKPOINT_START_NANOS = new ThreadLocal<>();

    private EventLensCheckpointMarker() {}

    public static void beginCheckpoint() {
        CHECKPOINT_START_NANOS.set(System.nanoTime());
    }

    public static long endCheckpoint() {
        Long start = CHECKPOINT_START_NANOS.get();
        CHECKPOINT_START_NANOS.remove();
        if (start == null) {
            return 0L;
        }
        return Math.max(0L, System.nanoTime() - start);
    }
}
