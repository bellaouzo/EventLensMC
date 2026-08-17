package dev.bellaouzo.eventlens.paper.dashboard;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.plugin.java.JavaPlugin;

final class DashboardMainThreadBridge {

    private static final long MAIN_THREAD_TIMEOUT_SECONDS = 5L;

    private final JavaPlugin plugin;

    DashboardMainThreadBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    <T> T run(Callable<T> task) throws ExecutionException, InterruptedException, TimeoutException {
        if (plugin.getServer().isPrimaryThread()) {
            try {
                return task.call();
            } catch (Exception ex) {
                throw new ExecutionException(ex);
            }
        }
        var future = plugin.getServer().getScheduler().callSyncMethod(plugin, task);
        return future.get(MAIN_THREAD_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
