package dev.bellaouzo.eventlens.neoforge;

import dev.bellaouzo.eventlens.modcommon.ModDispatchRecorder;
import dev.bellaouzo.eventlens.modcommon.ModLocalPlayerHurtDetector;
import dev.bellaouzo.eventlens.modcommon.ModLocalPlayerStateDetector;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class NeoForgePlayerStateTracer {

    private final ModDispatchRecorder recorder;
    private final ModLocalPlayerHurtDetector hurtDetector = new ModLocalPlayerHurtDetector();
    private final ModLocalPlayerStateDetector stateDetector = new ModLocalPlayerStateDetector();

    public NeoForgePlayerStateTracer(ModDispatchRecorder recorder) {
        this.recorder = recorder;
    }

    @SubscribeEvent
    public void onClientTickPre(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            hurtDetector.reset();
            stateDetector.reset();
            return;
        }
        String source = player.getLastDamageSource() == null ? "unknown" : player.getLastDamageSource().getMsgId();
        hurtDetector.observe(
                recorder, player.getHealth(), player.hurtTime, source, playerName(), worldName());
        stateDetector.observe(recorder, sample(player), playerName(), worldName());
    }

    private static ModLocalPlayerStateDetector.Sample sample(LocalPlayer player) {
        return new ModLocalPlayerStateDetector.Sample(
                player.getHealth(),
                player.getFoodData().getFoodLevel(),
                player.getAirSupply(),
                player.experienceLevel,
                player.totalExperience,
                player.getInventory().getSelectedSlot(),
                player.isSprinting(),
                player.isShiftKeyDown(),
                player.onGround(),
                player.getDeltaMovement().y,
                player.isFallFlying(),
                player.isSwimming(),
                player.isSleeping());
    }

    private static Optional<String> playerName() {
        return NeoForgeClientContext.playerName();
    }

    private static Optional<String> worldName() {
        return NeoForgeClientContext.worldName();
    }
}
