package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class ForgeClientBootstrap {

    private ForgeClientBootstrap() {}

    public static void register() {
        RegisterClientCommandsEvent.BUS.addListener(ForgeClientBootstrap::onRegisterClientCommands);
        RegisterCommandsEvent.BUS.addListener(ForgeClientBootstrap::onRegisterCommands);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ModTraceCoordinator coordinator = EventLensForgeMod.coordinator();
        if (coordinator != null) {
            new ForgeClientCommands(coordinator).register(event.getDispatcher());
        }
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("eventlensui").executes(context -> queueOpenUi()));
    }

    private static int queueOpenUi() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.execute(EventLensScreen::open);
        }
        return 1;
    }
}
