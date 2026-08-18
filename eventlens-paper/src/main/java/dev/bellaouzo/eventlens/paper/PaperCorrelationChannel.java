package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.application.TraceCorrelateService;
import dev.bellaouzo.eventlens.domain.correlation.CorrelationChannelCodec;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class PaperCorrelationChannel implements PluginMessageListener {

    private final JavaPlugin plugin;
    private final TraceCorrelateService correlateService;

    private PaperCorrelationChannel(JavaPlugin plugin, TraceCorrelateService correlateService) {
        this.plugin = plugin;
        this.correlateService = correlateService;
    }

    public static void register(JavaPlugin plugin, TraceCorrelateService correlateService) {
        Messenger messenger = plugin.getServer().getMessenger();
        PaperCorrelationChannel listener = new PaperCorrelationChannel(plugin, correlateService);
        messenger.registerOutgoingPluginChannel(plugin, CorrelationChannelCodec.CHANNEL);
        messenger.registerIncomingPluginChannel(plugin, CorrelationChannelCodec.CHANNEL, listener);
    }

    public static void unregister(JavaPlugin plugin) {
        Messenger messenger = plugin.getServer().getMessenger();
        messenger.unregisterIncomingPluginChannel(plugin, CorrelationChannelCodec.CHANNEL);
        messenger.unregisterOutgoingPluginChannel(plugin, CorrelationChannelCodec.CHANNEL);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!CorrelationChannelCodec.CHANNEL.equals(channel)) {
            return;
        }
        CorrelationChannelCodec.parseHello(message)
                .flatMap(hello ->
                        correlateService.acceptHello(hello.clientSessionId(), hello.sequence(), hello.correlationKey()))
                .ifPresent(reply -> sendReply(player, reply));
    }

    private void sendReply(Player player, CorrelationChannelCodec.Reply reply) {
        try {
            player.sendPluginMessage(
                    plugin,
                    CorrelationChannelCodec.CHANNEL,
                    CorrelationChannelCodec.reply(
                            reply.serverSessionId(),
                            reply.serverSequence(),
                            reply.clientSessionId(),
                            reply.clientSequence()));
        } catch (IllegalArgumentException | IllegalStateException _) {
            // Client did not register the channel; fail closed.
        }
    }
}
