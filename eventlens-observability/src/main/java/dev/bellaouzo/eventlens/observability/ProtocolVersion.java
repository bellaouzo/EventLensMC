package dev.bellaouzo.eventlens.observability;

public final class ProtocolVersion {

    public static final int CURRENT = 2;
    public static final int MIN_SUPPORTED = 1;

    private ProtocolVersion() {}

    public static boolean isCompatible(int reportedVersion) {
        return reportedVersion >= MIN_SUPPORTED && reportedVersion <= CURRENT;
    }
}
