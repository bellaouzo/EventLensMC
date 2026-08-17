package dev.bellaouzo.eventlens.fabric;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
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
            ScreenMouseEvents.afterMouseClick(screen).register((clicked, mouseX, mouseY, button) -> {
                if (button != 0 || client.gui == null) {
                    return;
                }
                Style style = client.gui.getChat().getClickedComponentStyleAt(mouseX, mouseY);
                if (style == null || style.getClickEvent() == null) {
                    return;
                }
                ClickEvent clickEvent = style.getClickEvent();
                if (clickEvent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD
                        && clickEvent.getValue() != null
                        && !clickEvent.getValue().isBlank()) {
                    FabricToasts.copied();
                }
            });
        });
    }
}
