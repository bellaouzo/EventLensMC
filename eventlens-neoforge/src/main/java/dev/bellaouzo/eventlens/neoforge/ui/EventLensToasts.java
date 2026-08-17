package dev.bellaouzo.eventlens.neoforge.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public final class EventLensToasts {

    private EventLensToasts() {}

    public static void show(String message) {
        show("EventLens", message);
    }

    public static void show(String title, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        SystemToast.add(
                minecraft.getToasts(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal(title),
                Component.literal(message));
    }
}
