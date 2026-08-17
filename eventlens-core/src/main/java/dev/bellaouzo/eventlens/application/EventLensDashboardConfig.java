package dev.bellaouzo.eventlens.application;

public record EventLensDashboardConfig(boolean enabled, int port, String bindAddress) {

    public static final int DEFAULT_PORT = 8765;
    public static final String DEFAULT_BIND_ADDRESS = "127.0.0.1";

    public EventLensDashboardConfig {
        if (port < 1 || port > 65535) {
            port = DEFAULT_PORT;
        }
        if (bindAddress == null || bindAddress.isBlank()) {
            bindAddress = DEFAULT_BIND_ADDRESS;
        }
    }

    public static EventLensDashboardConfig defaults() {
        return new EventLensDashboardConfig(false, DEFAULT_PORT, DEFAULT_BIND_ADDRESS);
    }

    public boolean isLoopbackOnly() {
        String normalized = bindAddress.trim().toLowerCase(java.util.Locale.ROOT);
        return normalized.equals(DEFAULT_BIND_ADDRESS)
                || normalized.equals("localhost")
                || normalized.equals("::1")
                || normalized.equals("[::1]");
    }
}
