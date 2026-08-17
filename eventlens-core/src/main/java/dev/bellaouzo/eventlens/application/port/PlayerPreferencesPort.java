package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.domain.preferences.PlayerPreferences;
import java.util.UUID;

public interface PlayerPreferencesPort {

    PlayerPreferences load(UUID playerId);

    void save(UUID playerId, PlayerPreferences preferences);
}
