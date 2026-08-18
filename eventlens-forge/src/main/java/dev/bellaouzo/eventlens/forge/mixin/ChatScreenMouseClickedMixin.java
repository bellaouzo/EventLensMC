package dev.bellaouzo.eventlens.forge.mixin;

import dev.bellaouzo.eventlens.forge.ForgeChatClicks;
import dev.bellaouzo.eventlens.forge.ForgeClientClickCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMouseClickedMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void eventlens$openUiClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() != 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.gui == null) {
            return;
        }
        Style style = ForgeChatClicks.clickedChatStyle(minecraft, event.x(), event.y());
        if (style == null || style.getClickEvent() == null) {
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
