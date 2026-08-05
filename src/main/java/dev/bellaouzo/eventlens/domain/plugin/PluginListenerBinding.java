package dev.bellaouzo.eventlens.domain.plugin;

import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import org.jspecify.annotations.NonNull;

public record PluginListenerBinding(@NonNull String eventClassName, @NonNull ListenerRegistration registration) {}
