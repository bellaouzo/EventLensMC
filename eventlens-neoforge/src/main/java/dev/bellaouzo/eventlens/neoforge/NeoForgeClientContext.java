package dev.bellaouzo.eventlens.neoforge;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

final class NeoForgeClientContext {

    private NeoForgeClientContext() {}

    static Optional<String> playerName() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? Optional.empty() : Optional.of(player.getGameProfile().getName());
    }

    static Optional<String> worldName() {
        Level level = Minecraft.getInstance().level;
        return level == null ? Optional.empty() : Optional.of(level.dimension().location().toString());
    }
}
