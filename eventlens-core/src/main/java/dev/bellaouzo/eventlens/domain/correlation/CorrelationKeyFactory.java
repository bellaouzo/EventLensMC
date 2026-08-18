package dev.bellaouzo.eventlens.domain.correlation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

public final class CorrelationKeyFactory {

    public static final long TIME_BUCKET_MILLIS = 100L;

    private CorrelationKeyFactory() {}

    public static Optional<String> create(
            Optional<CorrelationActionKind> actionKind,
            Optional<String> playerId,
            Optional<String> playerName,
            Optional<String> worldName,
            Optional<Integer> blockX,
            Optional<Integer> blockZ,
            long startedAtMillis) {
        if (actionKind.isEmpty()) {
            return Optional.empty();
        }
        String identity =
                playerId.filter(value -> !value.isBlank()).or(() -> playerName).orElse("-");
        String world = worldName.filter(value -> !value.isBlank()).orElse("-");
        String target = blockX.map(String::valueOf).orElse("-") + ","
                + blockZ.map(String::valueOf).orElse("-");
        long bucket = startedAtMillis / TIME_BUCKET_MILLIS;
        return Optional.of(actionKind.get().name()
                + "|"
                + identity.toLowerCase(Locale.ROOT)
                + "|"
                + world.toLowerCase(Locale.ROOT)
                + "|"
                + target
                + "|"
                + bucket);
    }

    public static String shareSafe(String correlationKey) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256").digest(correlationKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (NoSuchAlgorithmException ex) {
            return "redacted";
        }
    }
}
