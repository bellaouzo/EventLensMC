package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;

public final class EventLensClientAccess {

    private static ModTraceCoordinator coordinator;
    private static EventLensUiPreferences preferences;

    private EventLensClientAccess() {}

    public static void bind(ModTraceCoordinator nextCoordinator, EventLensUiPreferences nextPreferences) {
        coordinator = nextCoordinator;
        preferences = nextPreferences;
    }

    public static ModTraceCoordinator coordinator() {
        return coordinator;
    }

    public static EventLensUiPreferences preferences() {
        return preferences;
    }
}
