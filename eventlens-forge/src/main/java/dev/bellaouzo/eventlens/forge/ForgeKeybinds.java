package dev.bellaouzo.eventlens.forge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensClientAccess;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensUiPreferences;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = EventLensForgeMod.MOD_ID, value = Dist.CLIENT)
public final class ForgeKeybinds {

    public static final KeyMapping OPEN = new KeyMapping(
            "key.eventlens.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.eventlens");
    public static final KeyMapping HUD = new KeyMapping(
            "key.eventlens.hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.categories.eventlens");

    private ForgeKeybinds() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        while (OPEN.consumeClick()) {
            if (EventLensScreen.isOpen()) {
                Minecraft.getInstance().setScreen(null);
            } else {
                EventLensScreen.open();
            }
        }
        while (HUD.consumeClick()) {
            EventLensUiPreferences preferences = EventLensClientAccess.preferences();
            if (preferences != null) {
                preferences.toggleHud();
            }
        }
    }
}
