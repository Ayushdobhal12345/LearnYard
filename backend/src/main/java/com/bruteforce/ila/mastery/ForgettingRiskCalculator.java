package com.bruteforce.ila.mastery;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Deterministic forgetting risk estimation.
 * Risk increases the longer it's been since a topic was last practiced.
 * Higher mastery slows down how quickly risk accumulates, modeling the
 * idea that well-learned material is retained longer than shakily-learned
 * material - a simplified approximation of the forgetting curve concept.
 */
public class ForgettingRiskCalculator {

    private static final double MIN_RISK = 0.0;
    private static final double MAX_RISK = 100.0;

    // Risk grows by this many points per day, before mastery adjustment
    private static final double BASE_DAILY_RISK_INCREASE = 4.0;

    public static double calculateForgettingRisk(Double masteryScore, LocalDateTime lastPracticedAt) {
        // Never practiced at all - treat as maximum risk, since there's nothing to "forget from"
        if (lastPracticedAt == null) {
            return MAX_RISK;
        }

        long daysSincePractice = ChronoUnit.DAYS.between(lastPracticedAt, LocalDateTime.now());
        if (daysSincePractice <= 0) {
            return MIN_RISK; // Practiced today - essentially no forgetting risk yet
        }

        // Higher mastery = slower decay. A mastery of 100 halves the daily risk increase;
        // a mastery of 0 applies the full base rate.
        double masteryProtectionFactor = 1.0 - (masteryScore / 200.0); // ranges 0.5 to 1.0

        double risk = daysSincePractice * BASE_DAILY_RISK_INCREASE * masteryProtectionFactor;

        return Math.max(MIN_RISK, Math.min(MAX_RISK, risk));
    }
}