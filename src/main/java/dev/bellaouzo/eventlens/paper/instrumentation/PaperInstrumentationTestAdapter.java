package dev.bellaouzo.eventlens.paper.instrumentation;

import dev.bellaouzo.eventlens.application.port.InstrumentationTestPort;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

public final class PaperInstrumentationTestAdapter implements InstrumentationTestPort {

    private final Plugin plugin;

    public PaperInstrumentationTestAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<String> resolveAgentArgument() {
        return AgentJarLocator.resolveAgentArgument(plugin);
    }

    @Override
    public boolean canResolveAgentJar() {
        return AgentJarLocator.locate(plugin).isPresent();
    }
}
