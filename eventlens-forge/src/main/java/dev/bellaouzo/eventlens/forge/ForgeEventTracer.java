package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class ForgeEventTracer {

    private final ModDispatchRecorder recorder;
    private long tickDispatchKey = -1L;

    public ForgeEventTracer(ModDispatchRecorder recorder) {
        this.recorder = recorder;
    }

    public void register() {
        TickEvent.ClientTickEvent.Pre.BUS.addListener(this::onClientTickPre);
        TickEvent.ClientTickEvent.Post.BUS.addListener(this::onClientTickPost);
        ClientChatEvent.BUS.addListener(this::onClientChat);
        ClientChatReceivedEvent.BUS.addListener(this::onClientChatReceived);
        ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(this::onJoin);
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(this::onDisconnect);
        ScreenEvent.Opening.BUS.addListener(this::onScreenOpen);
        ScreenEvent.Closing.BUS.addListener(this::onScreenClose);
        AttackEntityEvent.BUS.addListener(this::onAttack);
        PlayerInteractEvent.RightClickItem.BUS.addListener(this::onUseItem);
        PlayerInteractEvent.EntityInteractSpecific.BUS.addListener(this::onUseEntity);
        PlayerInteractEvent.LeftClickBlock.BUS.addListener(this::onAttackBlock);
        PlayerInteractEvent.RightClickBlock.BUS.addListener(this::onUseBlock);
        PlayerInteractEvent.LeftClickEmpty.BUS.addListener(this::onLeftClickEmpty);
    }

    private void onClientTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (!recorder.isTracing()) {
            return;
        }
        tickDispatchKey =
                recorder.beginPaired(SupportedModEventTypes.CLIENT_TICK_EVENT, playerName(), worldName(), event);
        recordMove();
    }

    private void onClientTickPost(TickEvent.ClientTickEvent.Post event) {
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

    private void onClientChat(ClientChatEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHAT_EVENT,
                List.of(ModSnapshotFields.text("message", event.getMessage())),
                playerName(),
                worldName(),
                event);
    }

    private void onClientChatReceived(ClientChatReceivedEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHAT_RECEIVED_EVENT,
                List.of(ModSnapshotFields.text("message", event.getMessage().getString())),
                playerName(),
                worldName(),
                event);
    }

    private void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        recorder.recordImmediate(SupportedModEventTypes.CLIENT_JOIN_EVENT, List.of(), playerName(), worldName(), event);
    }

    private void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_DISCONNECT_EVENT, List.of(), playerName(), worldName(), event);
    }

    private void onScreenOpen(ScreenEvent.Opening event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREEN_OPEN_EVENT,
                List.of(ModSnapshotFields.text("screen", event.getScreen().getClass().getSimpleName())),
                playerName(),
                worldName(),
                event);
    }

    private void onScreenClose(ScreenEvent.Closing event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREEN_CLOSE_EVENT,
                List.of(ModSnapshotFields.text("screen", event.getScreen().getClass().getSimpleName())),
                playerName(),
                worldName(),
                event);
    }

    private void onAttack(AttackEntityEvent event) {
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

    private void onUseItem(PlayerInteractEvent.RightClickItem event) {
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

    private void onUseEntity(PlayerInteractEvent.EntityInteractSpecific event) {
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

    private void onAttackBlock(PlayerInteractEvent.LeftClickBlock event) {
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

    private void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
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

    private void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
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
                player.getName().getString(),
                level.dimension().identifier().toString(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot());
    }

    private static Optional<String> playerName() {
        return ForgeClientContext.playerName();
    }

    private static Optional<String> worldName() {
        return ForgeClientContext.worldName();
    }
}
