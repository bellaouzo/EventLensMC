package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.neoforge.ui.EventLensToasts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EventLensForgeMod.MOD_ID, value = Dist.CLIENT)
public final class ForgeChatClicks {

    private ForgeChatClicks() {}

    @SubscribeEvent
    public static boolean onChatClick(ScreenEvent.MouseButtonPressed.Pre event) {
        Style style = clickedStyle(event.getButton(), event.getScreen(), event.getMouseX(), event.getMouseY());
        if (style == null || style.getClickEvent() == null) {
            return false;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (!(clickEvent instanceof ClickEvent.RunCommand run)) {
            return false;
        }
        return ForgeClientClickCommands.handle(run.command());
    }

    @SubscribeEvent
    public static void onChatCopy(ScreenEvent.MouseButtonPressed.Post event) {
        Style style = clickedStyle(event.getButton(), event.getScreen(), event.getMouseX(), event.getMouseY());
        EventLensToasts.copiedFrom(style);
    }

    public static Style clickedChatStyle(Minecraft minecraft, double mouseX, double mouseY) {
        ActiveTextCollector.ClickableStyleFinder finder =
                new ActiveTextCollector.ClickableStyleFinder(minecraft.font, (int) mouseX, (int) mouseY);
        minecraft.gui.hud.getChat().captureClickableText(
                finder,
                minecraft.getWindow().getGuiScaledHeight(),
                minecraft.gui.hud.getGuiTicks(),
                ChatComponent.DisplayMode.FOREGROUND);
        return finder.result();
    }

    private static Style clickedStyle(int button, Screen screen, double mouseX, double mouseY) {
        if (button != 0 || !(screen instanceof ChatScreen)) {
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return null;
        }
        return clickedChatStyle(minecraft, mouseX, mouseY);
    }
}
