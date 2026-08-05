package dev.bellaouzo.eventlens.paper.snapshot;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.Event;

final class AsyncChatEventSnapshotAdapter implements EventSnapshotAdapter {

    @Override
    public void contribute(Event event, SnapshotFieldCollector collector) {
        if (!(event instanceof AsyncChatEvent chatEvent)) {
            return;
        }

        collector.putString(
                "chat.message", PlainTextComponentSerializer.plainText().serialize(chatEvent.message()));
    }
}
