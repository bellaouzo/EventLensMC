package dev.bellaouzo.eventlens.paper.dashboard;

import dev.bellaouzo.eventlens.application.port.DashboardServerContextPort;
import dev.bellaouzo.eventlens.domain.dashboard.DashboardServerContext;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperDashboardServerContextAdapter implements DashboardServerContextPort {

    private final JavaPlugin plugin;

    public PaperDashboardServerContextAdapter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public DashboardServerContext capture() {
        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().getFirst();
        String worldName = world != null ? world.getName() : "—";
        String gameModeLabel = Bukkit.getDefaultGameMode().name().toLowerCase(Locale.ROOT);
        return new DashboardServerContext(
                Bukkit.getVersion(),
                plugin.getPluginMeta().getVersion(),
                worldName,
                gameModeLabel,
                Bukkit.getOnlinePlayers().size(),
                readTps(),
                System.currentTimeMillis());
    }

    private static double readTps() {
        try {
            double[] tps = Bukkit.getTPS();
            if (tps.length > 0) {
                return Math.clamp(tps[0], 0.0, 20.0);
            }
        } catch (RuntimeException | LinkageError _) {
            // fall through
        }
        return 20.0;
    }
}
