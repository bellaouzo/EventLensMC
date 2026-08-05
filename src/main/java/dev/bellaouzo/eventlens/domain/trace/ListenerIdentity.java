package dev.bellaouzo.eventlens.domain.trace;

import org.jspecify.annotations.NonNull;

public record ListenerIdentity(
        @NonNull String pluginName, @NonNull String listenerClassName, @NonNull String methodName) {

    public String displayName() {
        int lastDot = listenerClassName.lastIndexOf('.');
        String simpleClass = lastDot >= 0 ? listenerClassName.substring(lastDot + 1) : listenerClassName;
        return pluginName + "/" + simpleClass + "." + methodName;
    }
}
