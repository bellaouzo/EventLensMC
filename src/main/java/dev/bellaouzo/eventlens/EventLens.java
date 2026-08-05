package dev.bellaouzo.eventlens;

import dev.bellaouzo.eventlens.command.StatusCommand;
import dev.bellaouzo.eventlens.trace.TraceSessionManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EventLens extends JavaPlugin {

    private TraceSessionManager traceSessionManager;

    @Override
    public void onEnable() {
        traceSessionManager = new TraceSessionManager();

        StatusCommand statusCommand = new StatusCommand(this, traceSessionManager);
        if (getCommand("eventlens") != null) {
            getCommand("eventlens").setExecutor(statusCommand);
            getCommand("eventlens").setTabCompleter(statusCommand);
        } else {
            getLogger().severe("Command 'eventlens' is missing from plugin.yml.");
        }

        getLogger().info("EventLens v" + getDescription().getVersion()
                + " enabled for Paper 26.2. Tracing engine not yet active.");
    }

    @Override
    public void onDisable() {
        if (traceSessionManager != null) {
            traceSessionManager.closeAll();
            traceSessionManager = null;
        }

        getLogger().info("EventLens disabled.");
    }

    public TraceSessionManager getTraceSessionManager() {
        return traceSessionManager;
    }
}
