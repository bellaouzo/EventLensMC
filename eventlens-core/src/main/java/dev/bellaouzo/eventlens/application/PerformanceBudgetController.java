package dev.bellaouzo.eventlens.application;

import dev.bellaouzo.eventlens.domain.observability.DurationStatsCalculator;
import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import java.util.ArrayList;
import java.util.List;

public final class PerformanceBudgetController {

    public enum Decision {
        CONTINUE,
        THROTTLE,
        STOP
    }

    public record BudgetEvaluation(Decision decision, long latestOverheadNanos, long windowP95Nanos) {}

    private final List<Long> overheadWindow = new ArrayList<>();
    private int consecutiveOverBudgetWindows;
    private int completedDispatches;

    public BudgetEvaluation recordOverhead(long overheadNanos) {
        completedDispatches++;
        boolean warmup = completedDispatches <= PerformanceBudget.WARMUP_DISPATCHES;

        if (overheadNanos >= PerformanceBudget.EMERGENCY_STOP_NANOS) {
            Decision decision = warmup ? Decision.THROTTLE : Decision.STOP;
            return new BudgetEvaluation(decision, overheadNanos, overheadNanos);
        }

        overheadWindow.add(overheadNanos);
        if (overheadWindow.size() > PerformanceBudget.WINDOW_SIZE) {
            overheadWindow.removeFirst();
        }

        if (overheadWindow.size() < PerformanceBudget.WINDOW_SIZE) {
            return new BudgetEvaluation(Decision.CONTINUE, overheadNanos, 0L);
        }

        long windowP95 =
                DurationStatsCalculator.compute(List.copyOf(overheadWindow)).p95Nanos();
        if (windowP95 > PerformanceBudget.AUTO_STOP_P95_NANOS) {
            consecutiveOverBudgetWindows++;
            if (!warmup && consecutiveOverBudgetWindows >= PerformanceBudget.AUTO_STOP_CONSECUTIVE_WINDOWS) {
                return new BudgetEvaluation(Decision.STOP, overheadNanos, windowP95);
            }
            return new BudgetEvaluation(Decision.THROTTLE, overheadNanos, windowP95);
        }

        if (windowP95 > PerformanceBudget.THROTTLE_P95_NANOS) {
            consecutiveOverBudgetWindows = 0;
            return new BudgetEvaluation(Decision.THROTTLE, overheadNanos, windowP95);
        }

        consecutiveOverBudgetWindows = 0;
        return new BudgetEvaluation(Decision.CONTINUE, overheadNanos, windowP95);
    }
}
