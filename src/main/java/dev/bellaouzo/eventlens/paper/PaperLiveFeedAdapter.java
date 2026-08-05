package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.port.LiveFeedDeliveryPort;
import dev.bellaouzo.eventlens.domain.live.LiveFeedDisplayMode;
import dev.bellaouzo.eventlens.domain.live.LiveFeedLine;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperLiveFeedAdapter implements LiveFeedDeliveryPort {

    private final JavaPlugin plugin;
    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();

    public PaperLiveFeedAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void deliverChat(UUID viewerId, List<LiveFeedLine> lines) {
        runOnMainThread(() -> {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null) {
                return;
            }
            for (LiveFeedLine line : lines) {
                player.sendMessage(formatLine(line));
            }
        });
    }

    @Override
    public void deliverStatus(UUID viewerId, String statusText, LiveFeedDisplayMode displayMode) {
        runOnMainThread(() -> {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null) {
                return;
            }
            Component component = Component.text(statusText, NamedTextColor.AQUA);
            switch (displayMode) {
                case ACTION_BAR -> player.sendActionBar(component);
                case BOSS_BAR -> updateBossBar(player, component, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
                default -> {
                    /* chat-only status uses periodic messages elsewhere */
                }
            }
        });
    }

    @Override
    public void clearStatus(UUID viewerId) {
        runOnMainThread(() -> {
            Player player = Bukkit.getPlayer(viewerId);
            BossBar bossBar = bossBars.remove(viewerId);
            if (player != null && bossBar != null) {
                player.hideBossBar(bossBar);
            }
        });
    }

    @Override
    public void deliverAlert(UUID viewerId, LiveFeedLine line, LiveFeedDisplayMode displayMode) {
        runOnMainThread(() -> {
            Player player = Bukkit.getPlayer(viewerId);
            if (player == null) {
                return;
            }
            Component alert = formatLine(line);
            switch (displayMode) {
                case ACTION_BAR -> player.sendActionBar(alert);
                case BOSS_BAR -> updateBossBar(player, alert, BossBar.Color.RED, BossBar.Overlay.NOTCHED_6);
                default -> player.sendMessage(alert);
            }
        });
    }

    private void updateBossBar(Player player, Component title, BossBar.Color color, BossBar.Overlay overlay) {
        BossBar bossBar = bossBars.computeIfAbsent(
                player.getUniqueId(), ignored -> BossBar.bossBar(Component.empty(), 1.0f, color, overlay));
        bossBar.name(title);
        bossBar.color(color);
        bossBar.overlay(overlay);
        player.showBossBar(bossBar);
    }

    private static Component formatLine(LiveFeedLine line) {
        NamedTextColor color =
                switch (line.channel()) {
                    case FREQUENCY -> NamedTextColor.GRAY;
                    case SLOW -> NamedTextColor.YELLOW;
                    case CANCELLATION -> NamedTextColor.GOLD;
                    case EXCEPTION -> NamedTextColor.RED;
                    case ALERT -> NamedTextColor.LIGHT_PURPLE;
                };
        Component prefix =
                Component.text("[" + line.channel().name().toLowerCase(Locale.ROOT) + "] ", color, TextDecoration.BOLD);
        return prefix.append(Component.text(line.text(), line.urgent() ? NamedTextColor.RED : NamedTextColor.WHITE));
    }

    private void runOnMainThread(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, task);
    }
}
