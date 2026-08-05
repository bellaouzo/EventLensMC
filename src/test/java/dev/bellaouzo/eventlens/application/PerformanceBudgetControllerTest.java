package dev.bellaouzo.eventlens.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.bellaouzo.eventlens.domain.observability.PerformanceBudget;
import org.junit.jupiter.api.Test;

class PerformanceBudgetControllerTest {

    @Test
    void throttlesWhenWindowP95ExceedsBudget() {
        PerformanceBudgetController controller = new PerformanceBudgetController();

        PerformanceBudgetController.Decision decision = PerformanceBudgetController.Decision.CONTINUE;
        for (int index = 0; index < PerformanceBudget.WINDOW_SIZE; index++) {
            decision = controller.recordOverhead(1_500_000L).decision();
        }

        assertEquals(PerformanceBudgetController.Decision.THROTTLE, decision);
    }

    @Test
    void stopsOnEmergencyOverhead() {
        PerformanceBudgetController controller = new PerformanceBudgetController();

        PerformanceBudgetController.BudgetEvaluation evaluation =
                controller.recordOverhead(PerformanceBudget.EMERGENCY_STOP_NANOS);

        assertEquals(PerformanceBudgetController.Decision.STOP, evaluation.decision());
    }

    @Test
    void continuesUnderBudget() {
        PerformanceBudgetController controller = new PerformanceBudgetController();

        PerformanceBudgetController.BudgetEvaluation evaluation = controller.recordOverhead(100_000L);

        assertEquals(PerformanceBudgetController.Decision.CONTINUE, evaluation.decision());
        assertTrue(evaluation.latestOverheadNanos() > 0L);
    }
}
