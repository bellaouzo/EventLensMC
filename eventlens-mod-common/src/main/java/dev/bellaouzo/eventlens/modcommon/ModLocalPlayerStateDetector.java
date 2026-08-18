package dev.bellaouzo.eventlens.modcommon;

import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import java.util.List;
import java.util.Optional;

public final class ModLocalPlayerStateDetector {

    private Sample last;
    private boolean seeded;

    public void reset() {
        last = null;
        seeded = false;
    }

    public void observe(ModDispatchRecorder recorder, Sample sample, Optional<String> playerName, Optional<String> worldName) {
        if (!seeded) {
            last = sample;
            seeded = true;
            return;
        }
        recordHeal(recorder, sample, playerName, worldName);
        recordNumber(
                recorder,
                SupportedModEventTypes.CLIENT_FOOD_EVENT,
                sample.food() != last.food(),
                List.of(ModSnapshotFields.number("food", sample.food())),
                playerName,
                worldName);
        recordNumber(
                recorder,
                SupportedModEventTypes.CLIENT_AIR_EVENT,
                sample.air() != last.air(),
                List.of(ModSnapshotFields.number("air", sample.air())),
                playerName,
                worldName);
        recordXp(recorder, sample, playerName, worldName);
        recordNumber(
                recorder,
                SupportedModEventTypes.CLIENT_SELECTED_SLOT_EVENT,
                sample.selectedSlot() != last.selectedSlot(),
                List.of(ModSnapshotFields.number("slot", sample.selectedSlot())),
                playerName,
                worldName);
        recordToggle(
                recorder,
                SupportedModEventTypes.CLIENT_SPRINT_EVENT,
                sample.sprinting() != last.sprinting(),
                "sprinting",
                sample.sprinting(),
                playerName,
                worldName);
        recordToggle(
                recorder,
                SupportedModEventTypes.CLIENT_SNEAK_EVENT,
                sample.sneaking() != last.sneaking(),
                "sneaking",
                sample.sneaking(),
                playerName,
                worldName);
        if (last.onGround() && !sample.onGround() && sample.deltaY() > 0.08d) {
            recorder.recordImmediate(
                    SupportedModEventTypes.CLIENT_JUMP_EVENT,
                    List.of(ModSnapshotFields.number("deltaY", sample.deltaY())),
                    playerName,
                    worldName);
        }
        recordToggle(
                recorder,
                SupportedModEventTypes.CLIENT_GLIDE_EVENT,
                sample.gliding() != last.gliding(),
                "gliding",
                sample.gliding(),
                playerName,
                worldName);
        recordToggle(
                recorder,
                SupportedModEventTypes.CLIENT_SWIM_EVENT,
                sample.swimming() != last.swimming(),
                "swimming",
                sample.swimming(),
                playerName,
                worldName);
        recordToggle(
                recorder,
                SupportedModEventTypes.CLIENT_SLEEP_EVENT,
                sample.sleeping() != last.sleeping(),
                "sleeping",
                sample.sleeping(),
                playerName,
                worldName);
        last = sample;
    }

    private void recordHeal(
            ModDispatchRecorder recorder, Sample sample, Optional<String> playerName, Optional<String> worldName) {
        if (sample.health() <= last.health() + 0.001f) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_HEAL_EVENT,
                List.of(
                        ModSnapshotFields.number("amount", sample.health() - last.health()),
                        ModSnapshotFields.number("health", sample.health())),
                playerName,
                worldName);
    }

    private void recordXp(
            ModDispatchRecorder recorder, Sample sample, Optional<String> playerName, Optional<String> worldName) {
        if (sample.xpLevel() == last.xpLevel() && sample.totalXp() == last.totalXp()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_XP_EVENT,
                List.of(
                        ModSnapshotFields.number("level", sample.xpLevel()),
                        ModSnapshotFields.number("total", sample.totalXp())),
                playerName,
                worldName);
    }

    private static void recordNumber(
            ModDispatchRecorder recorder,
            String type,
            boolean changed,
            List<SnapshotField> fields,
            Optional<String> playerName,
            Optional<String> worldName) {
        if (changed) {
            recorder.recordImmediate(type, fields, playerName, worldName);
        }
    }

    private static void recordToggle(
            ModDispatchRecorder recorder,
            String type,
            boolean changed,
            String field,
            boolean value,
            Optional<String> playerName,
            Optional<String> worldName) {
        if (changed) {
            recorder.recordImmediate(type, List.of(ModSnapshotFields.bool(field, value)), playerName, worldName);
        }
    }

    public record Sample(
            float health,
            int food,
            int air,
            int xpLevel,
            int totalXp,
            int selectedSlot,
            boolean sprinting,
            boolean sneaking,
            boolean onGround,
            double deltaY,
            boolean gliding,
            boolean swimming,
            boolean sleeping) {}
}
