package dev.bellaouzo.eventlens.fabric;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;

final class FabricChatClicks {

    private FabricChatClicks() {}

    static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof ChatScreen)) {
                return;
            }
            ScreenMouseEvents.afterMouseClick(screen).register((clicked, event, consumed) -> {
                if (event.button() != 0 || client.gui == null) {
                    return false;
                }
                Style style = clickedChatStyle(client, event.x(), event.y());
                if (style == null || style.getClickEvent() == null) {
                    return false;
                }
                ClickEvent clickEvent = style.getClickEvent();
                if (clickEvent instanceof ClickEvent.CopyToClipboard copy
                        && copy.value() != null
                        && !copy.value().isBlank()) {
                    FabricToasts.copied();
                }
                return false;
            });
        });
    }

    private static Style clickedChatStyle(Minecraft minecraft, double mouseX, double mouseY) {
        ActiveTextCollector.ClickableStyleFinder finder =
                new ActiveTextCollector.ClickableStyleFinder(minecraft.font, (int) mouseX, (int) mouseY);
        minecraft.gui.hud.getChat().captureClickableText(
                finder,
                minecraft.getWindow().getGuiScaledHeight(),
                minecraft.gui.hud.getGuiTicks(),
                ChatComponent.DisplayMode.FOREGROUND);
        return finder.result();
    }
}
