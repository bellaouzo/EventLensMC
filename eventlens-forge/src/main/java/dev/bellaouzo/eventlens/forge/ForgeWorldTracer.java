package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.client.event.ClientPauseChangeEvent;
import net.minecraftforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;

public final class ForgeWorldTracer {

    private final ModDispatchRecorder recorder;

    public ForgeWorldTracer(ModDispatchRecorder recorder) {
        this.recorder = recorder;
    }

    public void register() {
        ClientPlayerNetworkEvent.Clone.BUS.addListener(this::onRespawn);
        ClientPlayerChangeGameTypeEvent.BUS.addListener(this::onGameType);
        ClientPauseChangeEvent.Post.BUS.addListener(this::onPause);
        ItemTooltipEvent.BUS.addListener(this::onTooltip);
        ScreenshotEvent.BUS.addListener(this::onScreenshot);
        ToastAddEvent.BUS.addListener(this::onToast);
        PlaySoundEvent.BUS.addListener(this::onSound);
        EntityJoinLevelEvent.BUS.addListener(this::onEntityJoin);
        EntityLeaveLevelEvent.BUS.addListener(this::onEntityLeave);
        ChunkEvent.Load.BUS.addListener(this::onChunkLoad);
        ChunkEvent.Unload.BUS.addListener(this::onChunkUnload);
        TickEvent.LevelTickEvent.Post.BUS.addListener(this::onWorldTick);
        TickEvent.PlayerTickEvent.Post.BUS.addListener(this::onPlayerTick);
    }

    private void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_RESPAWN_EVENT,
                List.of(),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onGameType(ClientPlayerChangeGameTypeEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_GAME_TYPE_CHANGE_EVENT,
                List.of(
                        ModSnapshotFields.text("from", event.getCurrentGameType().getName()),
                        ModSnapshotFields.text("to", event.getNewGameType().getName())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onPause(ClientPauseChangeEvent.Post event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_PAUSE_EVENT,
                List.of(ModSnapshotFields.bool("paused", event.isPaused())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onTooltip(ItemTooltipEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_TOOLTIP_EVENT,
                List.of(
                        ModSnapshotFields.text("item", event.getItemStack().getHoverName().getString()),
                        ModSnapshotFields.number("lines", event.getToolTip().size())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onScreenshot(ScreenshotEvent event) {
        String file = event.getScreenshotFile() == null ? "" : event.getScreenshotFile().getName();
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREENSHOT_EVENT,
                List.of(ModSnapshotFields.text("file", file)),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onToast(ToastAddEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_TOAST_EVENT,
                List.of(ModSnapshotFields.text("toast", event.getToast().getClass().getSimpleName())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onSound(PlaySoundEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SOUND_EVENT,
                List.of(ModSnapshotFields.text("sound", event.getName())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recordEntity(SupportedModEventTypes.CLIENT_ENTITY_JOIN_EVENT, event.getEntity(), event);
    }

    private void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recordEntity(SupportedModEventTypes.CLIENT_ENTITY_LEAVE_EVENT, event.getEntity(), event);
    }

    private void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() == null || !event.getLevel().isClientSide()) {
            return;
        }
        recordChunk(SupportedModEventTypes.CLIENT_CHUNK_LOAD_EVENT, event.getChunk(), event);
    }

    private void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() == null || !event.getLevel().isClientSide()) {
            return;
        }
        recordChunk(SupportedModEventTypes.CLIENT_CHUNK_UNLOAD_EVENT, event.getChunk(), event);
    }

    private void onWorldTick(TickEvent.LevelTickEvent.Post event) {
        if (!event.level().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_WORLD_TICK_EVENT,
                List.of(ModSnapshotFields.text("dimension", event.level().dimension().identifier().toString())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (!(event.player() instanceof LocalPlayer) || !event.player().level().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_PLAYER_TICK_EVENT,
                List.of(),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void recordEntity(String type, Entity entity, Object event) {
        recorder.recordImmediate(
                type,
                List.of(
                        ModSnapshotFields.text("entity", entity.getName().getString()),
                        ModSnapshotFields.text("entityType", entity.getType().toShortString())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void recordChunk(String type, ChunkAccess chunk, Object event) {
        recorder.recordImmediate(
                type,
                List.of(
                        ModSnapshotFields.number("chunkX", chunk.getPos().x()),
                        ModSnapshotFields.number("chunkZ", chunk.getPos().z())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }
}
