package dev.bellaouzo.eventlens.paper.snapshot;

import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotField;
import dev.bellaouzo.eventlens.domain.snapshot.SnapshotValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.event.Event;

public final class PaperEventSnapshotCapture {

    private final List<EventSnapshotAdapter> adapters = List.of(
            new CommonEventSnapshotAdapter(),
            new PlayerEventSnapshotAdapter(),
            new BlockEventSnapshotAdapter(),
            new EntityDamageEventSnapshotAdapter(),
            new EntityDeathEventSnapshotAdapter(),
            new EntitySpawnEventSnapshotAdapter(),
            new InventoryClickEventSnapshotAdapter(),
            new PlayerInteractEventSnapshotAdapter(),
            new PlayerJoinEventSnapshotAdapter(),
            new PlayerMoveEventSnapshotAdapter(),
            new PlayerQuitEventSnapshotAdapter(),
            new PlayerTeleportEventSnapshotAdapter(),
            new PlayerCommandPreprocessEventSnapshotAdapter(),
            new AsyncChatEventSnapshotAdapter(),
            new InventoryOpenEventSnapshotAdapter(),
            new InventoryCloseEventSnapshotAdapter(),
            new InventoryDragEventSnapshotAdapter(),
            new PlayerDropItemEventSnapshotAdapter(),
            new EntityPickupItemEventSnapshotAdapter(),
            new ProjectileLaunchEventSnapshotAdapter(),
            new ProjectileHitEventSnapshotAdapter(),
            new CreatureSpawnEventSnapshotAdapter(),
            new ServerCommandEventSnapshotAdapter(),
            new CombatAndExplosionSnapshotAdapter(),
            new PlayerActionSnapshotAdapter(),
            new PlayerWorldSnapshotAdapter(),
            new ProtectionAndMobSnapshotAdapter());

    public EventSnapshot capture(Event event, String checkpoint) {
        SnapshotFieldCollector collector = new SnapshotFieldCollector();
        for (EventSnapshotAdapter adapter : adapters) {
            adapter.contribute(event, collector);
        }

        List<SnapshotField> fields = new ArrayList<>();
        for (Map.Entry<String, SnapshotValue> entry : collector.fields().entrySet()) {
            fields.add(new SnapshotField(entry.getKey(), entry.getValue()));
        }

        return new EventSnapshot(
                event.getClass().getName(),
                checkpoint,
                System.currentTimeMillis(),
                System.nanoTime(),
                List.copyOf(fields));
    }
}
