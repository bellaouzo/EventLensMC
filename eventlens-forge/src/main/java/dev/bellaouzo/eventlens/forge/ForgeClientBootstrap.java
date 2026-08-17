package dev.bellaouzo.eventlens.forge;

import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.neoforge.ui.EventLensScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("eventlensui").executes(context -> queueOpenUi()));
    }

    private static int queueOpenUi() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.execute(EventLensScreen::open);
        }
        return 1;
    }
}
