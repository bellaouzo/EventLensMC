package dev.bellaouzo.eventlens.domain.runtime;

public enum ModRuntimeKind {
    PAPER("paper"),
    NEOFORGE("neoforge"),
    FORGE("forge"),
    FABRIC("fabric");

    private final String wireName;

    ModRuntimeKind(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static ModRuntimeKind fromWireName(String value) {
        if (value == null || value.isBlank()) {
            return PAPER;
        }
        for (ModRuntimeKind kind : values()) {
            if (kind.wireName.equalsIgnoreCase(value.trim())) {
                return kind;
            }
        }
        return PAPER;
    }
}
