package dev.bellaouzo.eventlens.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
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
        if (event.getButton() != 0 || !(event.getScreen() instanceof ChatScreen)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return;
        }
        Style style = minecraft.gui.getChat().getClickedComponentStyleAt(event.getMouseX(), event.getMouseY());
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
}
