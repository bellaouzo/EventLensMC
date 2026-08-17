package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.neoforge.ui.EventLensToasts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = EventLensNeoForgeMod.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeChatClicks {

    private NeoForgeChatClicks() {}

    @SubscribeEvent
    public static void onChatCopy(ScreenEvent.MouseButtonPressed.Post event) {
        if (event.getButton() != 0 || !(event.getScreen() instanceof ChatScreen)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return;
        }
        Style style = minecraft.gui.getChat().getClickedComponentStyleAt(event.getMouseX(), event.getMouseY());
        EventLensToasts.copiedFrom(style);
    }
}
