package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class NeoForgeEventTracer {

    private final ModDispatchRecorder recorder;
    private long tickDispatchKey = -1L;

    public NeoForgeEventTracer(ModDispatchRecorder recorder) {
        this.recorder = recorder;
    }

    @SubscribeEvent
    public void onClientTickPre(ClientTickEvent.Pre event) {
        if (!recorder.isTracing()) {
            return;
        }
        tickDispatchKey =
                recorder.beginPaired(SupportedModEventTypes.CLIENT_TICK_EVENT, playerName(), worldName(), event);
        recordMove();
    }

    @SubscribeEvent
    public void onClientTickPost(ClientTickEvent.Post event) {
        if (tickDispatchKey < 0L) {
            return;
        }
        recorder.endPaired(
                tickDispatchKey,
                SupportedModEventTypes.CLIENT_TICK_EVENT,
                List.of(),
                playerName(),
                worldName(),
                event);
        tickDispatchKey = -1L;
    }

    @SubscribeEvent
    public void onClientChat(ClientChatEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHAT_EVENT,
                List.of(ModSnapshotFields.text("message", event.getMessage())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHAT_RECEIVED_EVENT,
                List.of(ModSnapshotFields.text("message", event.getMessage().getString())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        recorder.recordImmediate(SupportedModEventTypes.CLIENT_JOIN_EVENT, List.of(), playerName(), worldName(), event);
    }

    @SubscribeEvent
    public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_DISCONNECT_EVENT, List.of(), playerName(), worldName(), event);
    }

    @SubscribeEvent
    public void onScreenOpen(ScreenEvent.Opening event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREEN_OPEN_EVENT,
                List.of(ModSnapshotFields.text("screen", event.getScreen().getClass().getSimpleName())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onScreenClose(ScreenEvent.Closing event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREEN_CLOSE_EVENT,
                List.of(ModSnapshotFields.text("screen", event.getScreen().getClass().getSimpleName())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_ATTACK_EVENT,
                List.of(
                        ModSnapshotFields.text("target", event.getTarget().getName().getString()),
                        ModSnapshotFields.text("targetType", event.getTarget().getType().toShortString())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_USE_ITEM_EVENT,
                List.of(
                        ModSnapshotFields.text("hand", event.getHand().name()),
                        ModSnapshotFields.text("item", event.getItemStack().getHoverName().getString())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onUseEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_USE_ENTITY_EVENT,
                List.of(
                        ModSnapshotFields.text("hand", event.getHand().name()),
                        ModSnapshotFields.text("target", event.getTarget().getName().getString()),
                        ModSnapshotFields.text("targetType", event.getTarget().getType().toShortString())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onAttackBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_ATTACK_BLOCK_EVENT,
                List.of(
                        ModSnapshotFields.text("hand", event.getHand().name()),
                        ModSnapshotFields.number("x", event.getPos().getX()),
                        ModSnapshotFields.number("y", event.getPos().getY()),
                        ModSnapshotFields.number("z", event.getPos().getZ()),
                        ModSnapshotFields.text("face", event.getFace() == null ? "none" : event.getFace().getName())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_USE_BLOCK_EVENT,
                List.of(
                        ModSnapshotFields.text("hand", event.getHand().name()),
                        ModSnapshotFields.number("x", event.getPos().getX()),
                        ModSnapshotFields.number("y", event.getPos().getY()),
                        ModSnapshotFields.number("z", event.getPos().getZ()),
                        ModSnapshotFields.text("face", event.getFace() == null ? "none" : event.getFace().getName())),
                playerName(),
                worldName(),
                event);
    }

    @SubscribeEvent
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_ATTACK_EVENT,
                List.of(ModSnapshotFields.text("target", "empty")),
                playerName(),
                worldName(),
                event);
    }

    private void recordMove() {
        LocalPlayer player = Minecraft.getInstance().player;
        Level level = Minecraft.getInstance().level;
        if (player == null || level == null) {
            return;
        }
        recorder.recordMoveIfChanged(
                player.getGameProfile().getName(),
                level.dimension().location().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot());
    }

    private static Optional<String> playerName() {
        return NeoForgeClientContext.playerName();
    }

    private static Optional<String> worldName() {
        return NeoForgeClientContext.worldName();
    }
}
