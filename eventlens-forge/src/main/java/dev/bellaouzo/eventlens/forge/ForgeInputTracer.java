package dev.bellaouzo.eventlens.forge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class ForgeInputTracer {

    private final ModDispatchRecorder recorder;

    public ForgeInputTracer(ModDispatchRecorder recorder) {
        this.recorder = recorder;
    }

    public void register() {
        InputEvent.Key.BUS.addListener(this::onKey);
        InputEvent.MouseButton.Post.BUS.addListener(this::onMouseButton);
        InputEvent.MouseScrollingEvent.BUS.addListener(this::onMouseScroll);
        InputEvent.InteractionKeyMappingTriggered.BUS.addListener(this::onInteractionKey);
        MovementInputUpdateEvent.BUS.addListener(this::onMovementInput);
        ScreenEvent.MouseButtonPressed.Pre.BUS.addListener(this::onScreenClick);
        ScreenEvent.KeyPressed.Pre.BUS.addListener(this::onScreenKey);
        PlayerInteractEvent.RightClickEmpty.BUS.addListener(this::onUseEmpty);
    }

    private void onKey(InputEvent.Key event) {
        if (event.getAction() == InputConstants.REPEAT) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_KEY_EVENT,
                List.of(
                        ModSnapshotFields.number("key", event.getKey()),
                        ModSnapshotFields.text("action", actionName(event.getAction()))),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onMouseButton(InputEvent.MouseButton.Post event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_MOUSE_BUTTON_EVENT,
                List.of(
                        ModSnapshotFields.number("button", event.getButton()),
                        ModSnapshotFields.text("action", actionName(event.getAction()))),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_MOUSE_SCROLL_EVENT,
                List.of(
                        ModSnapshotFields.number("deltaX", event.getDeltaX()),
                        ModSnapshotFields.number("deltaY", event.getDeltaY())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_INTERACTION_KEY_EVENT,
                List.of(
                        ModSnapshotFields.bool("attack", event.isAttack()),
                        ModSnapshotFields.bool("use", event.isUseItem()),
                        ModSnapshotFields.bool("pick", event.isPickBlock()),
                        ModSnapshotFields.text("hand", event.getHand().name())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onMovementInput(MovementInputUpdateEvent event) {
        ClientInput input = event.getInput();
        Input keys = input.keyPresses;
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_MOVEMENT_INPUT_EVENT,
                List.of(
                        ModSnapshotFields.number("forward", input.getMoveVector().y),
                        ModSnapshotFields.number("strafe", input.getMoveVector().x),
                        ModSnapshotFields.bool("jump", keys.jump()),
                        ModSnapshotFields.bool("sneak", keys.shift())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onScreenClick(ScreenEvent.MouseButtonPressed.Pre event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREEN_CLICK_EVENT,
                List.of(
                        ModSnapshotFields.text("screen", event.getScreen().getClass().getSimpleName()),
                        ModSnapshotFields.number("button", event.getButton()),
                        ModSnapshotFields.number("x", event.getMouseX()),
                        ModSnapshotFields.number("y", event.getMouseY())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onScreenKey(ScreenEvent.KeyPressed.Pre event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREEN_KEY_EVENT,
                List.of(
                        ModSnapshotFields.text("screen", event.getScreen().getClass().getSimpleName()),
                        ModSnapshotFields.number("key", event.getKeyCode())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onUseEmpty(PlayerInteractEvent.RightClickEmpty event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_USE_EMPTY_EVENT,
                List.of(
                        ModSnapshotFields.text("hand", event.getHand().name()),
                        ModSnapshotFields.text("item", event.getItemStack().getHoverName().getString())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private static String actionName(int action) {
        if (action == InputConstants.PRESS) {
            return "press";
        }
        if (action == InputConstants.RELEASE) {
            return "release";
        }
        return "repeat";
    }
}
