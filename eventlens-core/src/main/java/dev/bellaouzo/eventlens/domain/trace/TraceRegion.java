package dev.bellaouzo.eventlens.domain.trace;

public record TraceRegion(int minX, int minZ, int maxX, int maxZ) {

    public TraceRegion {
        if (minX > maxX || minZ > maxZ) {
            throw new IllegalArgumentException("Region bounds are invalid.");
        }
    }

    public boolean contains(int x, int z) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
