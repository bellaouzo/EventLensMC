package dev.bellaouzo.eventlens.modcommon;

import java.util.List;
import java.util.Optional;

public final class ModLocalPlayerHurtDetector {

    private float lastHealth = Float.NaN;
    private int lastHurtTime;
    private boolean seeded;

    public void reset() {
        lastHealth = Float.NaN;
        lastHurtTime = 0;
        seeded = false;
    }

    public void observe(
            ModDispatchRecorder recorder,
            float health,
            int hurtTime,
            String source,
            Optional<String> playerName,
            Optional<String> worldName) {
        if (!seeded) {
            seed(health, hurtTime);
            return;
        }
        boolean pulse = hurtTime > lastHurtTime;
        boolean drop = health < lastHealth - 0.001f;
        if (pulse || drop) {
            float amount = Float.isNaN(lastHealth) ? 0.0f : Math.max(0.0f, lastHealth - health);
            recorder.recordImmediate(
                    SupportedModEventTypes.CLIENT_HURT_EVENT,
                    List.of(
                            ModSnapshotFields.number("amount", amount),
                            ModSnapshotFields.text("source", source == null || source.isBlank() ? "unknown" : source),
                            ModSnapshotFields.number("health", health),
                            ModSnapshotFields.number("hurtTime", hurtTime)),
                    playerName,
                    worldName);
        }
        seed(health, hurtTime);
    }

    private void seed(float health, int hurtTime) {
        lastHealth = health;
        lastHurtTime = hurtTime;
        seeded = true;
    }
}
