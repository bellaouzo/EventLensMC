package dev.bellaouzo.eventlens.domain.correlation;

import java.util.Locale;
import java.util.Optional;

public enum CorrelationActionKind {
    USE_ITEM,
    USE_BLOCK,
    USE_ENTITY,
    ATTACK,
    CHAT,
    COMMAND,
    BREAK,
    PLACE;

    public static Optional<CorrelationActionKind> fromEventClassName(String eventClassName) {
        if (eventClassName == null || eventClassName.isBlank()) {
            return Optional.empty();
        }
        String simple = simpleName(eventClassName).toLowerCase(Locale.ROOT);
        return switch (simple) {
            case "playerinteractevent", "clientuseitemevent" -> Optional.of(USE_ITEM);
            case "clientuseblockevent", "clientattackblockevent" -> Optional.of(USE_BLOCK);
            case "clientuseentityevent" -> Optional.of(USE_ENTITY);
            case "clientattackevent" -> Optional.of(ATTACK);
            case "asyncchatevent", "clientchatevent" -> Optional.of(CHAT);
            case "playercommandpreprocessevent", "servercommandevent" -> Optional.of(COMMAND);
            case "blockbreakevent" -> Optional.of(BREAK);
            case "blockplaceevent" -> Optional.of(PLACE);
            default -> Optional.empty();
        };
    }

    public boolean pairsWith(CorrelationActionKind other) {
        if (this == other) {
            return true;
        }
        return switch (this) {
            case USE_ITEM, USE_BLOCK, USE_ENTITY, ATTACK ->
                other == USE_ITEM || other == USE_BLOCK || other == USE_ENTITY || other == ATTACK;
            case BREAK, PLACE -> other == USE_BLOCK || other == BREAK || other == PLACE;
            case CHAT, COMMAND -> this == other;
        };
    }

    private static String simpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot < 0 ? className : className.substring(lastDot + 1);
    }
}
