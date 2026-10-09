package com.bruteforce.ila.priority;

/**
 * The core Priority Formula, combining five 0-100 scaled inputs into one
 * final Priority Score using fixed weights. This is a project design
 * decision, not a scientifically derived formula - weights are chosen to
 * reflect what matters most for exam preparation (weakness and exam
 * importance weighted highest).
 *
 * Priority Score =
 *     0.30 x Weakness Score
 *   + 0.25 x Exam Importance
 *   + 0.20 x Prerequisite Impact
 *   + 0.15 x Forgetting Risk
 *   + 0.10 x Difficulty
 */
public class PriorityCalculator {

    public static final double WEAKNESS_WEIGHT = 0.30;
    public static final double EXAM_IMPORTANCE_WEIGHT = 0.25;
    public static final double PREREQUISITE_IMPACT_WEIGHT = 0.20;
    public static final double FORGETTING_RISK_WEIGHT = 0.15;
    public static final double DIFFICULTY_WEIGHT = 0.10;

    public static double calculatePriority(double weaknessScore, double examImportance,
                                           double prerequisiteImpact, double forgettingRisk,
                                           double difficultyScore) {
        return (WEAKNESS_WEIGHT * weaknessScore)
                + (EXAM_IMPORTANCE_WEIGHT * examImportance)
                + (PREREQUISITE_IMPACT_WEIGHT * prerequisiteImpact)
                + (FORGETTING_RISK_WEIGHT * forgettingRisk)
                + (DIFFICULTY_WEIGHT * difficultyScore);
    }
}