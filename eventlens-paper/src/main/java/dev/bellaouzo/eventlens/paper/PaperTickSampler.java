package dev.bellaouzo.eventlens.paper;

import dev.bellaouzo.eventlens.domain.trace.DispatchTickContext;
import org.bukkit.Bukkit;

final class PaperTickSampler {

    private PaperTickSampler() {}

    static DispatchTickContext capture() {
        long tick = Bukkit.getCurrentTick();
        double tps = Bukkit.getTPS()[0];
        double mspt = averageTickMillis();
        return DispatchTickContext.paper(tick, tps, mspt);
    }

    private static double averageTickMillis() {
        try {
            Object value = Bukkit.getServer()
                    .getClass()
                    .getMethod("getAverageTickTime")
                    .invoke(Bukkit.getServer());
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        } catch (ReflectiveOperationException ignored) {
            return 50.0d;
        }
        return 50.0d;
    }
}
