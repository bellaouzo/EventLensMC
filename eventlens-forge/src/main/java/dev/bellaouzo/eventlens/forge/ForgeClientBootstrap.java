package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EventLensForgeMod.MOD_ID, value = Dist.CLIENT)
public final class ForgeClientBootstrap {

    private ForgeClientBootstrap() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        ModTraceCoordinator coordinator = EventLensForgeMod.coordinator();
        if (coordinator != null) {
            new ForgeClientCommands(coordinator).register(event.getDispatcher());
        }
    }
}
