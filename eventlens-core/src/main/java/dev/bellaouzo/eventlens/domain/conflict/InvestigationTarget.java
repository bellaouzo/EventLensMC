package dev.bellaouzo.eventlens.domain.conflict;

import java.util.Optional;
import org.jspecify.annotations.NonNull;

public record InvestigationTarget(
        @NonNull String pluginName,
        Optional<String> listenerClassName,
        Optional<String> methodName,
        int occurrenceCount,
        @NonNull ConflictSeverity maxSeverity) {

    public String displayName() {
        if (listenerClassName.isEmpty() || methodName.isEmpty()) {
            return pluginName;
        }
        String simpleClass = listenerClassName.get();
        int lastDot = simpleClass.lastIndexOf('.');
        if (lastDot >= 0) {
            simpleClass = simpleClass.substring(lastDot + 1);
        }
        return pluginName + "/" + simpleClass + "#" + methodName.get();
    }
}
