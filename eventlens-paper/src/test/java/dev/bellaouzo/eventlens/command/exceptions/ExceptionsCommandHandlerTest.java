package dev.bellaouzo.eventlens.command.exceptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.ExceptionInboxService;
import dev.bellaouzo.eventlens.command.RecordingCommandSender;
import dev.bellaouzo.eventlens.domain.snapshot.EventSnapshot;
import dev.bellaouzo.eventlens.domain.trace.ListenerTimingRecord;
import dev.bellaouzo.eventlens.domain.trace.TraceDispatchRecord;
import dev.bellaouzo.eventlens.domain.trace.TracePartialReason;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExceptionsCommandHandlerTest {

    private final ExceptionInboxService inbox = new ExceptionInboxService();
    private final ExceptionsCommandHandler handler = new ExceptionsCommandHandler(inbox);

    @Test
    void emptyInboxExplainsNoExceptions() {
        RecordingCommandSender sender = new RecordingCommandSender(true);
        handler.handle(sender.sender(), new String[] {"exceptions"});
        assertTrue(sender.joined().contains("No attributed exceptions yet."));
    }

    @Test
    void listsInboxEntry() {
        inbox.onDispatchCaptured("abcd1234", throwingDispatch());
        RecordingCommandSender sender = new RecordingCommandSender(true);
        handler.handle(sender.sender(), new String[] {"exceptions"});
        assertTrue(sender.joined().contains("WorldGuard"));
        assertTrue(sender.joined().contains("onInteract"));
        assertTrue(sender.joined().contains("PlayerInteractEvent"));
    }

    @Test
    void deniesWithoutPermission() {
        RecordingCommandSender sender = new RecordingCommandSender(false);
        handler.handle(sender.sender(), new String[] {"exceptions"});
        assertTrue(sender.joined().contains("You do not have permission."));
    }

    @Test
    void tabCompletesPage() {
        assertTrue(handler.tabComplete("").contains("1"));
    }

    private static TraceDispatchRecord throwingDispatch() {
        EventSnapshot snapshot =
                new EventSnapshot("org.bukkit.event.player.PlayerInteractEvent", "LOWEST", 1_000L, List.of());
        return new TraceDispatchRecord(
                1L,
                1_000L,
                1_000_000_000L,
                1_000_000L,
                0L,
                "org.bukkit.event.player.PlayerInteractEvent",
                true,
                true,
                false,
                false,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                snapshot,
                snapshot,
                List.of(snapshot),
                List.of(),
                List.of(ListenerTimingRecord.timingOnly(
                        1,
                        "WorldGuard",
                        "com.example.Guard",
                        "onInteract",
                        "NORMAL",
                        1_000_000L,
                        true,
                        false,
                        false,
                        Optional.empty(),
                        true,
                        Optional.of("IllegalStateException"))),
                EnumSet.noneOf(TracePartialReason.class));
    }
}
