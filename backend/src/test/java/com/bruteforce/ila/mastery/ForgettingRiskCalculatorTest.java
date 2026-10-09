package com.bruteforce.ila.mastery;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ForgettingRiskCalculatorTest {

    @Test
    void neverPracticedReturnsMaxRisk() {
        double risk = ForgettingRiskCalculator.calculateForgettingRisk(50.0, null);
        assertEquals(100.0, risk, 0.01);
    }

    @Test
    void practicedTodayHasZeroRisk() {
        double risk = ForgettingRiskCalculator.calculateForgettingRisk(50.0, LocalDateTime.now());
        assertEquals(0.0, risk, 0.01);
    }

    @Test
    void higherMasterySlowsRiskAccumulation() {
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        double lowMasteryRisk = ForgettingRiskCalculator.calculateForgettingRisk(0.0, tenDaysAgo);
        double highMasteryRisk = ForgettingRiskCalculator.calculateForgettingRisk(100.0, tenDaysAgo);
        assertEquals(40.0, lowMasteryRisk, 0.01);
        assertEquals(20.0, highMasteryRisk, 0.01);
    }
}