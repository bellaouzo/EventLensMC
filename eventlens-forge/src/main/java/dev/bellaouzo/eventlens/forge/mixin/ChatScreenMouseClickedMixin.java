package dev.bellaouzo.eventlens.forge.mixin;

import dev.bellaouzo.eventlens.forge.ForgeClientClickCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMouseClickedMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void eventlens$openUiClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return;
        }
        Style style = minecraft.gui.getChat().getClickedComponentStyleAt(mouseX, mouseY);
        if (style == null || style.getClickEvent() == null) {
            return;
        }
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
            return;
        }
        if (ForgeClientClickCommands.handle(clickEvent.getValue())) {
            cir.setReturnValue(true);
        }
    }
}
