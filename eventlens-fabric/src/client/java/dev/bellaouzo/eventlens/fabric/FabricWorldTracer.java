package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

final class FabricWorldTracer {

    private FabricWorldTracer() {}

    static void register(ModDispatchRecorder recorder) {
        ClientTickEvents.END_WORLD_TICK.register(world -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_WORLD_TICK_EVENT,
                List.of(ModSnapshotFields.text("dimension", world.dimension().location().toString())),
                FabricClientContext.playerName(),
                FabricClientContext.worldName()));
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_ENTITY_JOIN_EVENT,
                List.of(
                        ModSnapshotFields.text("entity", entity.getName().getString()),
                        ModSnapshotFields.text("entityType", entity.getType().toShortString())),
                FabricClientContext.playerName(),
                FabricClientContext.worldName()));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_ENTITY_LEAVE_EVENT,
                List.of(
                        ModSnapshotFields.text("entity", entity.getName().getString()),
                        ModSnapshotFields.text("entityType", entity.getType().toShortString())),
                FabricClientContext.playerName(),
                FabricClientContext.worldName()));
        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHUNK_LOAD_EVENT,
                List.of(
                        ModSnapshotFields.number("chunkX", chunk.getPos().x),
                        ModSnapshotFields.number("chunkZ", chunk.getPos().z)),
                FabricClientContext.playerName(),
                FabricClientContext.worldName()));
        ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_CHUNK_UNLOAD_EVENT,
                List.of(
                        ModSnapshotFields.number("chunkX", chunk.getPos().x),
                        ModSnapshotFields.number("chunkZ", chunk.getPos().z)),
                FabricClientContext.playerName(),
                FabricClientContext.worldName()));
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_TOOLTIP_EVENT,
                List.of(
                        ModSnapshotFields.text("item", stack.getHoverName().getString()),
                        ModSnapshotFields.number("lines", lines.size())),
                FabricClientContext.playerName(),
                FabricClientContext.worldName()));
    }
}
