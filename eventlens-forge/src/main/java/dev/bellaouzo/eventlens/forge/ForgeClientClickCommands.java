package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen;
import net.minecraftforge.client.ClientCommandHandler;

public final class ForgeClientClickCommands {

    private ForgeClientClickCommands() {}

    public static boolean handle(String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            return false;
        }
        String command = rawCommand.charAt(0) == '/' ? rawCommand.substring(1) : rawCommand;
        if (isUi(command)) {
            EventLensScreen.open();
            return true;
        }
        if (!isEventLensCommand(command)) {
            return false;
        }
        ClientCommandHandler.runCommand(command);
        return true;
    }

    static boolean isEventLensCommand(String command) {
        return command.equals("eventlens")
                || command.equals("el")
                || command.startsWith("eventlens ")
                || command.startsWith("el ");
    }

    static boolean isUi(String command) {
        return command.equals("eventlens ui")
                || command.equals("el ui")
                || command.equals("eventlensui");
    }
}
