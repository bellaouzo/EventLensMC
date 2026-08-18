package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.neoforge.ui.EventLensToasts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.ChatComponent;
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
        Style style = clickedChatStyle(minecraft, event.getMouseX(), event.getMouseY());
        EventLensToasts.copiedFrom(style);
    }

    static Style clickedChatStyle(Minecraft minecraft, double mouseX, double mouseY) {
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
