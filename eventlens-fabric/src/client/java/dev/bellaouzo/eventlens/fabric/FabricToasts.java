package dev.bellaouzo.eventlens.fabric;

import dev.bellaouzo.eventlens.modcommon.chat.ModChatLine;
import dev.bellaouzo.eventlens.modcommon.command.ModCommandNotices;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

final class FabricToasts {

    private FabricToasts() {}

    static void command(List<String> args, List<ModChatLine> lines) {
        ModCommandNotices.toastMessage(args, lines).ifPresent(FabricToasts::show);
    }

    private static void show(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || message == null || message.isBlank()) {
            return;
        }
        SystemToast.add(
                minecraft.getToasts(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal("EventLens"),
                Component.literal(message));
    }
}
