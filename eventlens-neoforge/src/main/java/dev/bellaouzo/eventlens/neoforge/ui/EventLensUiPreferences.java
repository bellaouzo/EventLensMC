package dev.bellaouzo.eventlens.neoforge.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class EventLensUiPreferences {

    private static final String HUD_ENABLED = "hud.enabled";
    private final Path file;
    private boolean hudEnabled;

    private EventLensUiPreferences(Path file, boolean hudEnabled) {
        this.file = file;
        this.hudEnabled = hudEnabled;
    }

    public static EventLensUiPreferences load(Path configDir) {
        Path file = configDir.resolve("ui.properties");
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
            } catch (IOException ignored) {
                return new EventLensUiPreferences(file, false);
            }
        }
        return new EventLensUiPreferences(file, Boolean.parseBoolean(properties.getProperty(HUD_ENABLED, "false")));
    }

    public boolean hudEnabled() {
        return hudEnabled;
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

    private void save() {
        Properties properties = new Properties();
        properties.setProperty(HUD_ENABLED, Boolean.toString(hudEnabled));
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                properties.store(out, "EventLens client UI");
            }
        } catch (IOException ignored) {
            // Keep the in-memory toggle even if the file cannot be written.
        }
    }
}
