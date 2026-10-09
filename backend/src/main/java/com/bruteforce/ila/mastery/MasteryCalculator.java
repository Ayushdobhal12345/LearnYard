package com.bruteforce.ila.mastery;

/**
 * Deterministic mastery update logic.
 * Given whether an answer was correct and how difficult the question was,
 * calculates the new mastery score using a weighted-nudge approach:
 * a correct answer on a HARD question boosts mastery more than a correct
 * answer on an EASY question. A wrong answer on an EASY question hurts
 * mastery more than a wrong answer on a HARD question.
 */
public class MasteryCalculator {

    private static final double MIN_MASTERY = 0.0;
    private static final double MAX_MASTERY = 100.0;

    // How much a single answer can move the mastery score
    private static final double BASE_ADJUSTMENT = 8.0;

    public static double calculateNewMastery(double currentMastery, boolean wasCorrect, int difficultyLevel) {
        // difficultyLevel: 1 = easy, 2 = medium, 3 = hard
        double difficultyMultiplier = difficultyLevel / 2.0; // easy=0.5, medium=1.0, hard=1.5

        double adjustment = BASE_ADJUSTMENT * difficultyMultiplier;

        double newMastery;
        if (wasCorrect) {
            newMastery = currentMastery + adjustment;
        } else {
            // Wrong answers on easy questions are penalized more heavily,
            // because getting an easy question wrong is a stronger signal of weakness
            double wrongPenaltyMultiplier = difficultyLevel == 1 ? 1.5 : 1.0;
            newMastery = currentMastery - (adjustment * wrongPenaltyMultiplier);
        }

        // Clamp between 0 and 100
        return Math.max(MIN_MASTERY, Math.min(MAX_MASTERY, newMastery));
    }
}