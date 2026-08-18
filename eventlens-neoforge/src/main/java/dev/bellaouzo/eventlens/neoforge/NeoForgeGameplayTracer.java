package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class NeoForgeGameplayTracer {

    private final ModDispatchRecorder recorder;

    public NeoForgeGameplayTracer(ModDispatchRecorder recorder) {
        this.recorder = recorder;
    }

    @SubscribeEvent
    public void onToss(ItemTossEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recordItem(SupportedModEventTypes.CLIENT_ITEM_TOSS_EVENT, event.getEntity(), event);
    }

    @SubscribeEvent
    public void onPickup(ItemEntityPickupEvent.Post event) {
        if (!event.getPlayer().level().isClientSide()) {
            return;
        }
        recordItem(SupportedModEventTypes.CLIENT_ITEM_PICKUP_EVENT, event.getItemEntity(), event);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_DEATH_EVENT,
                entityFields(event.getEntity(), ModSnapshotFields.text("source", event.getSource().getMsgId())),
                NeoForgeClientContext.playerName(),
                NeoForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onHurt(LivingIncomingDamageEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_HURT_EVENT,
                entityFields(
                        event.getEntity(),
                        ModSnapshotFields.number("amount", event.getAmount()),
                        ModSnapshotFields.text("source", event.getSource().getMsgId())),
                NeoForgeClientContext.playerName(),
                NeoForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onUseEntityAt(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_USE_ENTITY_AT_EVENT,
                List.of(
                        ModSnapshotFields.text("hand", event.getHand().name()),
                        ModSnapshotFields.text("target", event.getTarget().getName().getString()),
                        ModSnapshotFields.text("targetType", event.getTarget().getType().toShortString())),
                NeoForgeClientContext.playerName(),
                NeoForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        ItemStack item = event.getItem();
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_USE_ITEM_FINISH_EVENT,
                entityFields(event.getEntity(), ModSnapshotFields.text("item", item.getHoverName().getString())),
                NeoForgeClientContext.playerName(),
                NeoForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recordContainer(SupportedModEventTypes.CLIENT_CONTAINER_OPEN_EVENT, event, event.getContainer().getClass());
    }

    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recordContainer(SupportedModEventTypes.CLIENT_CONTAINER_CLOSE_EVENT, event, event.getContainer().getClass());
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        var fields = new ArrayList<>(List.of(
                ModSnapshotFields.text("block", event.getState().getBlock().toString()),
                ModSnapshotFields.number("original", event.getOriginalSpeed()),
                ModSnapshotFields.number("speed", event.getNewSpeed())));
        event.getPosition().ifPresent(pos -> {
            fields.add(ModSnapshotFields.number("x", pos.getX()));
            fields.add(ModSnapshotFields.number("y", pos.getY()));
            fields.add(ModSnapshotFields.number("z", pos.getZ()));
        });
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_BREAK_SPEED_EVENT,
                fields,
                NeoForgeClientContext.playerName(),
                NeoForgeClientContext.worldName(),
                event);
    }

    private void recordItem(String type, ItemEntity item, Object event) {
        recorder.recordImmediate(
                type,
                List.of(
                        ModSnapshotFields.text("item", item.getItem().getHoverName().getString()),
                        ModSnapshotFields.number("count", item.getItem().getCount())),
                NeoForgeClientContext.playerName(),
                NeoForgeClientContext.worldName(),
                event);
    }

    private void recordContainer(String type, Object event, Class<?> containerClass) {
        recorder.recordImmediate(
                type,
                List.of(ModSnapshotFields.text("container", containerClass.getSimpleName())),
                NeoForgeClientContext.playerName(),
                NeoForgeClientContext.worldName(),
                event);
    }

    private static List<SnapshotField> entityFields(Entity entity, SnapshotField... extra) {
        List<dev.bellaouzo.eventlens.domain.snapshot.SnapshotField> fields = new ArrayList<>();
        fields.add(ModSnapshotFields.text("entity", entity.getName().getString()));
        fields.add(ModSnapshotFields.text("entityType", entity.getType().toShortString()));
        fields.addAll(List.of(extra));
        return fields;
    }
}
