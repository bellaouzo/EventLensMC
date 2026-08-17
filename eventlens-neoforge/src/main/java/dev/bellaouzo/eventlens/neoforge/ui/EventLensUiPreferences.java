package dev.bellaouzo.eventlens.neoforge.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public final class EventLensUiPreferences {

    private static final String HUD_ENABLED = "hud.enabled";
    private static final String LAST_TAB = "ui.lastTab";
    private static final String LAST_SESSION = "ui.lastSession";
    private static final String LAST_GENERATION = "ui.lastGeneration";
    private static final String LAST_DISPATCH = "ui.lastDispatch";

    private final Path file;
    private boolean hudEnabled;
    private String lastTab;
    private String lastSessionId;
    private int lastGeneration;
    private int lastDispatch;

    private EventLensUiPreferences(
            Path file,
            boolean hudEnabled,
            String lastTab,
            String lastSessionId,
            int lastGeneration,
            int lastDispatch) {
        this.file = file;
        this.hudEnabled = hudEnabled;
        this.lastTab = lastTab;
        this.lastSessionId = lastSessionId;
        this.lastGeneration = lastGeneration;
        this.lastDispatch = lastDispatch;
    }

    public static EventLensUiPreferences load(Path configDir) {
        Path file = configDir.resolve("ui.properties");
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
            } catch (IOException ignored) {
                return defaults(file);
            }
        }
        return new EventLensUiPreferences(
                file,
                Boolean.parseBoolean(properties.getProperty(HUD_ENABLED, "false")),
                properties.getProperty(LAST_TAB, "HOME"),
                properties.getProperty(LAST_SESSION, ""),
                parseInt(properties.getProperty(LAST_GENERATION), -1),
                parseInt(properties.getProperty(LAST_DISPATCH), -1));
    }

    private static EventLensUiPreferences defaults(Path file) {
        return new EventLensUiPreferences(file, false, "HOME", "", -1, -1);
    }

    public boolean hudEnabled() {
        return hudEnabled;
    }

    public String lastTab() {
        return lastTab;
    }

    public String lastSessionId() {
        return lastSessionId;
    }

    public int lastGeneration() {
        return lastGeneration;
    }

    public int lastDispatch() {
        return lastDispatch;
    }

    public void setHudEnabled(boolean enabled) {
        if (this.hudEnabled == enabled) {
            return;
        }
        this.hudEnabled = enabled;
        save();
    }

    public void toggleHud() {
        setHudEnabled(!hudEnabled);
    }

    public void setLastLocation(String tab, String sessionId, int generation, int dispatch) {
        String nextTab = tab == null || tab.isBlank() ? "HOME" : tab;
        String nextSession = sessionId == null ? "" : sessionId;
        if (Objects.equals(lastTab, nextTab)
                && Objects.equals(lastSessionId, nextSession)
                && lastGeneration == generation
                && lastDispatch == dispatch) {
            return;
        }
        this.lastTab = nextTab;
        this.lastSessionId = nextSession;
        this.lastGeneration = generation;
        this.lastDispatch = dispatch;
        save();
    }

    private void save() {
        Properties properties = new Properties();
        properties.setProperty(HUD_ENABLED, Boolean.toString(hudEnabled));
        properties.setProperty(LAST_TAB, lastTab);
        properties.setProperty(LAST_SESSION, lastSessionId);
        properties.setProperty(LAST_GENERATION, Integer.toString(lastGeneration));
        properties.setProperty(LAST_DISPATCH, Integer.toString(lastDispatch));
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                properties.store(out, "EventLens client UI");
            }
        } catch (IOException ignored) {
            // Keep the in-memory values even if the file cannot be written.
        }
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
