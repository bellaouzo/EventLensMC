package dev.bellaouzo.eventlens.observability;

public final class ObservationGate {

    private static volatile boolean enabled;

    private ObservationGate() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean observationEnabled) {
        enabled = observationEnabled;
    }
}
