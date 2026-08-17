package dev.bellaouzo.eventlens.paper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.application.EventLensCommandConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class EventLensConfigLoaderTest {

    @Test
    void loadCommandConfigReadsTracePresets() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                new java.io.StringReader(
                        """
                reports:
                  retention-days: 30
                  auto-cleanup: true
                trace:
                  require-hot-event-confirmation: true
                  presets:
                    quick-interact:
                      max-duration-ms: 30000
                      max-events: 128
                """));

        EventLensCommandConfig commandConfig = EventLensConfigLoader.loadCommandConfig(config);

        assertEquals(1, commandConfig.presets().size());
        assertTrue(commandConfig.presets().containsKey("quick-interact"));
        assertTrue(commandConfig.requireHotEventConfirmation());
    }

    @Test
    void loadCommandConfigUsesDefaultsWhenTraceSectionMissing() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                new java.io.StringReader(
                        """
                reports:
                  retention-days: 14
                  auto-cleanup: false
                """));

        EventLensCommandConfig commandConfig = EventLensConfigLoader.loadCommandConfig(config);

        assertTrue(commandConfig.presets().isEmpty());
        assertTrue(commandConfig.requireHotEventConfirmation());
    }

    @Test
    void mergeDefaultsAddsMissingTraceKeys() {
        YamlConfiguration existing = YamlConfiguration.loadConfiguration(
                new java.io.StringReader(
                        """
                reports:
                  retention-days: 30
                  auto-cleanup: true
                """));
        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                new java.io.StringReader(
                        """
                reports:
                  retention-days: 30
                  auto-cleanup: true
                trace:
                  require-hot-event-confirmation: true
                  presets:
                    quick-interact:
                      max-duration-ms: 30000
                """));

        existing.setDefaults(defaults);
        existing.options().copyDefaults(true);

        EventLensCommandConfig commandConfig = EventLensConfigLoader.loadCommandConfig(existing);

        assertFalse(commandConfig.presets().isEmpty());
        assertTrue(commandConfig.presets().containsKey("quick-interact"));
    }
}
