package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.neoforge.ui.EventLensHudOverlay;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public final class ForgeClientEvents {

    private ForgeClientEvents() {}

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ForgeKeybinds.OPEN);
        event.register(ForgeKeybinds.HUD);
    }

    public static void registerHud(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw()
                .add(ResourceLocation.fromNamespaceAndPath(EventLensForgeMod.MOD_ID, "hud"), EventLensHudOverlay::render);
    }
}
