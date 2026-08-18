package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModSnapshotFields;
import dev.bellaouzo.eventlens.modcommon.SupportedModEventTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public final class ForgeGameplayTracer {

    private final ModDispatchRecorder recorder;

    public ForgeGameplayTracer(ModDispatchRecorder recorder) {
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
    public void onPickup(EntityItemPickupEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recordItem(SupportedModEventTypes.CLIENT_ITEM_PICKUP_EVENT, event.getItem(), event);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_DEATH_EVENT,
                entityFields(event.getEntity(), ModSnapshotFields.text("source", event.getSource().getMsgId())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            return;
        }
        recorder.recordImmediate(
                SupportedModEventTypes.CLIENT_HURT_EVENT,
                entityFields(
                        event.getEntity(),
                        ModSnapshotFields.number("amount", event.getAmount()),
                        ModSnapshotFields.text("source", event.getSource().getMsgId())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
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
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
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
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void recordItem(String type, ItemEntity item, Object event) {
        recorder.recordImmediate(
                type,
                List.of(
                        ModSnapshotFields.text("item", item.getItem().getHoverName().getString()),
                        ModSnapshotFields.number("count", item.getItem().getCount())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
                event);
    }

    private void recordContainer(String type, Object event, Class<?> containerClass) {
        recorder.recordImmediate(
                type,
                List.of(ModSnapshotFields.text("container", containerClass.getSimpleName())),
                ForgeClientContext.playerName(),
                ForgeClientContext.worldName(),
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
