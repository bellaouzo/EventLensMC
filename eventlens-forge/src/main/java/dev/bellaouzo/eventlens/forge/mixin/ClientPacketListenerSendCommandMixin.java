package dev.bellaouzo.eventlens.forge.mixin;

import dev.bellaouzo.eventlens.forge.ForgeClientClickCommands;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerSendCommandMixin {

    @Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
    private void eventlens$runClientCommand(String command, CallbackInfo ci) {
        if (ForgeClientClickCommands.handle(command)) {
            ci.cancel();
        }
    }

    @Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
    private void eventlens$runUnsignedClientCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        if (ForgeClientClickCommands.handle(command)) {
            cir.setReturnValue(true);
        }
    }
}
