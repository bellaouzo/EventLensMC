package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModLocalPlayerHurtDetector;
import dev.bellaouzo.eventlens.modcommon.ModLocalPlayerStateDetector;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;

final class FabricEventTracer {

    private FabricEventTracer() {}

    static void register(ModDispatchRecorder recorder) {
        AtomicLong tickDispatchKey = new AtomicLong(-1L);
        ModLocalPlayerHurtDetector hurtDetector = new ModLocalPlayerHurtDetector();
        ModLocalPlayerStateDetector stateDetector = new ModLocalPlayerStateDetector();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            recordLocalPlayer(recorder, hurtDetector, stateDetector);
            if (!recorder.isTracing()) {
                return;
            }
            tickDispatchKey.set(recorder.beginPaired(
                    SupportedModEventTypes.CLIENT_TICK_EVENT, playerName(), worldName()));
            recordMove(recorder);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long key = tickDispatchKey.getAndSet(-1L);
            if (key < 0L) {
                return;
            }
            recorder.endPaired(
                    key, SupportedModEventTypes.CLIENT_TICK_EVENT, List.of(), playerName(), worldName());
        });
        ClientSendMessageEvents.CHAT.register(message -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHAT_EVENT,
                List.of(ModSnapshotFields.text("message", message)),
                playerName(),
                worldName()));
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
                recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_CHAT_RECEIVED_EVENT,
                        List.of(ModSnapshotFields.text("message", message.getString())),
                        playerName(),
                        worldName()));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                recorder.recordImmediate(SupportedModEventTypes.CLIENT_JOIN_EVENT, List.of(), playerName(), worldName()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_DISCONNECT_EVENT, List.of(), playerName(), worldName()));
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREEN_OPEN_EVENT,
                List.of(ModSnapshotFields.text("screen", screen.getClass().getSimpleName())),
                playerName(),
                worldName()));
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
                ScreenEvents.remove(screen).register(closed -> recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_SCREEN_CLOSE_EVENT,
                        List.of(ModSnapshotFields.text("screen", closed.getClass().getSimpleName())),
                        playerName(),
                        worldName())));
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClientSide()) {
                recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_ATTACK_BLOCK_EVENT,
                        List.of(
                                ModSnapshotFields.text("hand", hand.name()),
                                ModSnapshotFields.number("x", pos.getX()),
                                ModSnapshotFields.number("y", pos.getY()),
                                ModSnapshotFields.number("z", pos.getZ()),
                                ModSnapshotFields.text("face", direction.getName())),
                        playerName(),
                        worldName());
            }
            return InteractionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) {
                recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_USE_ENTITY_EVENT,
                        List.of(
                                ModSnapshotFields.text("hand", hand.name()),
                                ModSnapshotFields.text("target", entity.getName().getString()),
                                ModSnapshotFields.text("targetType", entity.getType().toShortString())),
                        playerName(),
                        worldName());
            }
            return InteractionResult.PASS;
        });
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) {
                recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_ATTACK_EVENT,
                        List.of(
                                ModSnapshotFields.text("hand", hand.name()),
                                ModSnapshotFields.text("target", entity.getName().getString()),
                                ModSnapshotFields.text("targetType", entity.getType().toShortString())),
                        playerName(),
                        worldName());
            }
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) {
                recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_USE_ITEM_EVENT,
                        List.of(
                                ModSnapshotFields.text("hand", hand.name()),
                                ModSnapshotFields.text("item", player.getItemInHand(hand).getHoverName().getString())),
                        playerName(),
                        worldName());
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) {
                recorder.recordImmediate(
                        SupportedModEventTypes.CLIENT_USE_BLOCK_EVENT,
                        List.of(
                                ModSnapshotFields.text("hand", hand.name()),
                                ModSnapshotFields.number("x", hitResult.getBlockPos().getX()),
                                ModSnapshotFields.number("y", hitResult.getBlockPos().getY()),
                                ModSnapshotFields.number("z", hitResult.getBlockPos().getZ()),
                                ModSnapshotFields.text("face", hitResult.getDirection().getName())),
                        playerName(),
                        worldName());
            }
            return InteractionResult.PASS;
        });
        FabricWorldTracer.register(recorder);
        FabricScreenTracer.register(recorder);
    }

    private static void recordLocalPlayer(
            ModDispatchRecorder recorder,
            ModLocalPlayerHurtDetector hurtDetector,
            ModLocalPlayerStateDetector stateDetector) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            hurtDetector.reset();
            stateDetector.reset();
            return;
        }
        String source = player.getLastDamageSource() == null ? "unknown" : player.getLastDamageSource().getMsgId();
        hurtDetector.observe(recorder, player.getHealth(), player.hurtTime, source, playerName(), worldName());
        stateDetector.observe(
                recorder,
                new ModLocalPlayerStateDetector.Sample(
                        player.getHealth(),
                        player.getFoodData().getFoodLevel(),
                        player.getAirSupply(),
                        player.experienceLevel,
                        player.totalExperience,
                        player.getInventory().selected,
                        player.isSprinting(),
                        player.isShiftKeyDown(),
                        player.onGround(),
                        player.getDeltaMovement().y,
                        player.isFallFlying(),
                        player.isSwimming(),
                        player.isSleeping()),
                playerName(),
                worldName());
    }

    private static void recordMove(ModDispatchRecorder recorder) {
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
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? Optional.empty() : Optional.of(player.getGameProfile().getName());
    }

    private static Optional<String> worldName() {
        Level level = Minecraft.getInstance().level;
        return level == null ? Optional.empty() : Optional.of(level.dimension().location().toString());
    }
}
