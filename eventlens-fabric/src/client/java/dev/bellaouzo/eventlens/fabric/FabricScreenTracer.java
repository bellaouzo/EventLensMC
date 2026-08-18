package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

final class FabricScreenTracer {

    private FabricScreenTracer() {}

    static void register(ModDispatchRecorder recorder) {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenMouseEvents.afterMouseClick(screen).register((clicked, event, consumed) -> {
                recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_SCREEN_CLICK_EVENT,
                        List.of(
                                ModSnapshotFields.text("screen", clicked.getClass().getSimpleName()),
                                ModSnapshotFields.number("button", event.button()),
                                ModSnapshotFields.number("x", event.x()),
                                ModSnapshotFields.number("y", event.y())),
                        playerName(),
                        worldName());
                return false;
            });
            ScreenKeyboardEvents.afterKeyPress(screen).register((pressed, event) ->
                    recorder.recordImmediate(
                            SupportedModEventTypes.CLIENT_SCREEN_KEY_EVENT,
                            List.of(
                                    ModSnapshotFields.text("screen", pressed.getClass().getSimpleName()),
                                    ModSnapshotFields.number("key", event.key())),
                            playerName(),
                            worldName()));
        });
    }

    private static Optional<String> playerName() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? Optional.empty() : Optional.of(player.getName().getString());
    }

    private static Optional<String> worldName() {
        Level level = Minecraft.getInstance().level;
        return level == null ? Optional.empty() : Optional.of(level.dimension().identifier().toString());
    }
}
