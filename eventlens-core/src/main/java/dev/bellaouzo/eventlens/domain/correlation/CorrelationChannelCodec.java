package dev.bellaouzo.eventlens.domain.correlation;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class CorrelationChannelCodec {

    public static final String CHANNEL = "eventlens:correlate";

    private CorrelationChannelCodec() {}

    public static byte[] hello(String clientSessionId, long sequence, String correlationKey) {
        return ("HELLO\t" + clientSessionId + "\t" + sequence + "\t" + correlationKey).getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] reply(
            String serverSessionId, long serverSequence, String clientSessionId, long clientSequence) {
        return ("REPLY\t" + serverSessionId + "\t" + serverSequence + "\t" + clientSessionId + "\t" + clientSequence)
                .getBytes(StandardCharsets.UTF_8);
    }

    public static Optional<Hello> parseHello(byte[] payload) {
        String[] parts = split(payload);
        if (parts.length != 4 || !"HELLO".equals(parts[0])) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Hello(parts[1], Long.parseLong(parts[2]), parts[3]));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public static Optional<Reply> parseReply(byte[] payload) {
        String[] parts = split(payload);
        if (parts.length != 5 || !"REPLY".equals(parts[0])) {
            return Optional.empty();
        }
        try {
            return Optional.of(new Reply(parts[1], Long.parseLong(parts[2]), parts[3], Long.parseLong(parts[4])));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static String[] split(byte[] payload) {
        return new String(payload, StandardCharsets.UTF_8).split("\\t", -1);
    }

    public record Hello(String clientSessionId, long sequence, String correlationKey) {}

    public record Reply(String serverSessionId, long serverSequence, String clientSessionId, long clientSequence) {}
}
