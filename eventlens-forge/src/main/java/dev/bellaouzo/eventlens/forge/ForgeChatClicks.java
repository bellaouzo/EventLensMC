package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.neoforge.ui.EventLensToasts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EventLensForgeMod.MOD_ID, value = Dist.CLIENT)
public final class ForgeChatClicks {

    private ForgeChatClicks() {}

    @SubscribeEvent
    public static void onChatClick(ScreenEvent.MouseButtonPressed.Pre event) {
        Style style = clickedStyle(event.getButton(), event.getScreen(), event.getMouseX(), event.getMouseY());
        if (style == null || style.getClickEvent() == null) {
            return;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
            return;
        }
        if (ForgeClientClickCommands.handle(clickEvent.getValue())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChatCopy(ScreenEvent.MouseButtonPressed.Post event) {
        Style style = clickedStyle(event.getButton(), event.getScreen(), event.getMouseX(), event.getMouseY());
        EventLensToasts.copiedFrom(style);
    }

    private static Style clickedStyle(int button, Screen screen, double mouseX, double mouseY) {
        if (button != 0 || !(screen instanceof ChatScreen)) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return null;
        }
        return minecraft.gui.getChat().getClickedComponentStyleAt(mouseX, mouseY);
    }
}
