package dev.bellaouzo.eventlens.forge;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensClientAccess;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensUiPreferences;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class ForgeKeybinds {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(EventLensForgeMod.MOD_ID, "key.categories.eventlens"));

    public static final KeyMapping OPEN = new KeyMapping(
            "key.eventlens.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping HUD = new KeyMapping(
            "key.eventlens.hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

    private ForgeKeybinds() {}

    public static void register() {
        TickEvent.ClientTickEvent.Post.BUS.addListener(ForgeKeybinds::onClientTick);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        while (OPEN.consumeClick()) {
            if (EventLensScreen.isOpen()) {
                Minecraft.getInstance().gui.setScreen(null);
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
