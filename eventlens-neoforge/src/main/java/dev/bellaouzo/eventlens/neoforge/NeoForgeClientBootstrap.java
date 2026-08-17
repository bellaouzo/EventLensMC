package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = EventLensNeoForgeMod.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientBootstrap {

    private NeoForgeClientBootstrap() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ModTraceCoordinator coordinator = EventLensNeoForgeMod.coordinator();
        if (coordinator != null) {
            new NeoForgeClientCommands(coordinator).register(event.getDispatcher());
        }
    }
}
