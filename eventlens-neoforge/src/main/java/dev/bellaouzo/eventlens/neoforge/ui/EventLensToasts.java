package dev.bellaouzo.eventlens.neoforge.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class EventLensToasts {

    private static final int MAX_BODY = 48;

    private EventLensToasts() {}

    public static void copied() {
        show("Copied");
    }

    public static boolean copiedFrom(Style style) {
        if (style == null || style.getClickEvent() == null) {
            return false;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (!(clickEvent instanceof ClickEvent.CopyToClipboard copy)
                || copy.value() == null
                || copy.value().isBlank()) {
            return false;
        }
        copied();
        return true;
    }

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
                minecraft.gui.toastManager(),
                SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                Component.literal(title),
                Component.literal(fit(message)));
    }

    private static String fit(String message) {
        String body = dropFilePath(message);
        if (body.length() <= MAX_BODY) {
            return body;
        }
        return body.substring(0, MAX_BODY - 3) + "...";
    }

    private static String dropFilePath(String message) {
        int to = message.indexOf(" to ");
        if (to <= 0) {
            return message;
        }
        String rest = message.substring(to + 4);
        if (rest.indexOf('\\') >= 0 || rest.indexOf('/') >= 0) {
            return message.substring(0, to).trim();
        }
        return message;
    }
}
