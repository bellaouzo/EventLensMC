package dev.bellaouzo.eventlens.testkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class EventLensTestTarget extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new TestKitListeners(), this);
        TestKitCommand command = new TestKitCommand();
        var pluginCommand = getCommand("eltest");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
        getLogger().info("EventLensTestTarget enabled in passive mode. Use /eltest mode trace before trace scenarios.");
    }
}
