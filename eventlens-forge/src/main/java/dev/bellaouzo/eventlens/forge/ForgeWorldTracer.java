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
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ForgeWorldTracer {

    private final ModDispatchRecorder recorder;

    public ForgeWorldTracer(ModDispatchRecorder recorder) {
        this.recorder = recorder;
    }

    @SubscribeEvent
    public void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_RESPAWN_EVENT,
                List.of(),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onGameType(ClientPlayerChangeGameTypeEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_GAME_TYPE_CHANGE_EVENT,
                List.of(
                        ModSnapshotFields.text("from", event.getCurrentGameType().getName()),
                        ModSnapshotFields.text("to", event.getNewGameType().getName())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onPause(ClientPauseChangeEvent.Post event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_PAUSE_EVENT,
                List.of(ModSnapshotFields.bool("paused", event.isPaused())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_TOOLTIP_EVENT,
                List.of(
                        ModSnapshotFields.text("item", event.getItemStack().getHoverName().getString()),
                        ModSnapshotFields.number("lines", event.getToolTip().size())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onScreenshot(ScreenshotEvent event) {
        String file = event.getScreenshotFile() == null ? "" : event.getScreenshotFile().getName();
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SCREENSHOT_EVENT,
                List.of(ModSnapshotFields.text("file", file)),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onToast(ToastAddEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_TOAST_EVENT,
                List.of(ModSnapshotFields.text("toast", event.getToast().getClass().getSimpleName())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onSound(PlaySoundEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_SOUND_EVENT,
                List.of(ModSnapshotFields.text("sound", event.getName())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onRecipes(RecipesUpdatedEvent event) {
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_RECIPES_UPDATED_EVENT,
                List.of(),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recordEntity(SupportedModEventTypes.CLIENT_ENTITY_JOIN_EVENT, event.getEntity(), event);
    }

    @SubscribeEvent
    public void onEntityLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recordEntity(SupportedModEventTypes.CLIENT_ENTITY_LEAVE_EVENT, event.getEntity(), event);
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() == null || !event.getLevel().isClientSide()) {
            return;
        }
        recordChunk(SupportedModEventTypes.CLIENT_CHUNK_LOAD_EVENT, event.getChunk(), event);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() == null || !event.getLevel().isClientSide()) {
            return;
        }
        recordChunk(SupportedModEventTypes.CLIENT_CHUNK_UNLOAD_EVENT, event.getChunk(), event);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent.Post event) {
        if (!event.level.isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_WORLD_TICK_EVENT,
                List.of(ModSnapshotFields.text("dimension", event.level.dimension().location().toString())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (!(event.player instanceof LocalPlayer) || !event.player.level().isClientSide()) {
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
                        ModSnapshotFields.number("chunkX", chunk.getPos().x),
                        ModSnapshotFields.number("chunkZ", chunk.getPos().z)),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }
}
