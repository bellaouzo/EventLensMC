package dev.bellaouzo.eventlens.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.EventLens;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

@Disabled("MockBukkit bootstrap does not provide all Paper 26.2 event classes used by EventLens startup.")
class EventLensCommandPermissionTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        MockBukkit.load(EventLens.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "eventlens status",
                "eventlens listeners BlockBreakEvent",
                "eventlens plugin EventLens",
                "eventlens trace list"
            })
    void deniesTopLevelCommandsWithoutPermission(String commandLine) {
        PlayerMock player = server.addPlayer();
        player.setOp(false);

        boolean handled = server.dispatchCommand(player, commandLine);

        assertTrue(handled);
        String message = player.nextMessage();
        assertNotNull(message);
        assertEquals(CommandMessages.PERMISSION_DENIED, message);
    }
}
