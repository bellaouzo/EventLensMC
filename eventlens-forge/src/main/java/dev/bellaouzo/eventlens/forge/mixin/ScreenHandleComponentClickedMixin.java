package dev.bellaouzo.eventlens.forge.mixin;

import dev.bellaouzo.eventlens.forge.ForgeClientClickCommands;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ScreenHandleComponentClickedMixin {

    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void eventlens$runClientCommand(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style == null) {
            return;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (!(clickEvent instanceof ClickEvent.RunCommand run)) {
            return;
        }
        if (ForgeClientClickCommands.handle(run.command())) {
            cir.setReturnValue(true);
        }
    }
}
