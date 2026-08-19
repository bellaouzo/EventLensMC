package dev.bellaouzo.eventlens.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public record EventLensDashboardConfig(boolean enabled, int port, String bindAddress, String token) {

    public static final int DEFAULT_PORT = 8765;
    public static final String DEFAULT_BIND_ADDRESS = "127.0.0.1";
    public static final int MIN_REMOTE_TOKEN_LENGTH = 12;

    public EventLensDashboardConfig {
        if (port < 1 || port > 65535) {
            port = DEFAULT_PORT;
        }
        if (bindAddress == null || bindAddress.isBlank()) {
            bindAddress = DEFAULT_BIND_ADDRESS;
        }
        token = token == null ? "" : token.trim();
    }

    public static EventLensDashboardConfig defaults() {
        return new EventLensDashboardConfig(false, DEFAULT_PORT, DEFAULT_BIND_ADDRESS, "");
    }

    public boolean isLoopbackOnly() {
        String normalized = bindAddress.trim().toLowerCase(Locale.ROOT);
        return normalized.equals(DEFAULT_BIND_ADDRESS)
                || normalized.equals("localhost")
                || normalized.equals("::1")
                || normalized.equals("[::1]");
    }

    public boolean hasToken() {
        return !token.isEmpty();
    }

    public boolean hasRemoteToken() {
        return token.length() >= MIN_REMOTE_TOKEN_LENGTH;
    }

    public boolean canBind() {
        return isLoopbackOnly() || hasRemoteToken();
    }

    public boolean tokenMatches(String presented) {
        if (!hasToken()) {
            return true;
        }
        if (presented == null) {
            return false;
        }
        return MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8), presented.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        return "EventLensDashboardConfig[enabled="
                + enabled
                + ", port="
                + port
                + ", bindAddress="
                + bindAddress
                + ", token="
                + (hasToken() ? "<set>" : "<empty>")
                + "]";
    }
}
