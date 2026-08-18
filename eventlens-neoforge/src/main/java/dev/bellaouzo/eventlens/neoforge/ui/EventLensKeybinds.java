package dev.bellaouzo.eventlens.neoforge.ui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.bellaouzo.eventlens.neoforge.EventLensNeoForgeMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EventLensNeoForgeMod.MOD_ID, value = Dist.CLIENT)
public final class EventLensKeybinds {

    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(EventLensNeoForgeMod.MOD_ID, "key.categories.eventlens"));

    public static final KeyMapping OPEN = new KeyMapping(
            "key.eventlens.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping HUD = new KeyMapping(
            "key.eventlens.hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

    private EventLensKeybinds() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN.consumeClick()) {
            if (EventLensScreen.isOpen()) {
                MinecraftHolder.close();
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

    private static final class MinecraftHolder {
        private static void close() {
            net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);
        }
    }
}
