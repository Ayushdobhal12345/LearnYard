package com.bruteforce.ila.priority.dto;

public class PriorityBreakdownResponse {

    private Long topicId;
    private String topicName;
    private double weaknessScore;
    private double examImportance;
    private double prerequisiteImpact;
    private double forgettingRisk;
    private double difficultyScore;
    private double finalPriorityScore;

    public PriorityBreakdownResponse() {
    }

    public PriorityBreakdownResponse(Long topicId, String topicName, double weaknessScore,
                                     double examImportance, double prerequisiteImpact,
                                     double forgettingRisk, double difficultyScore,
                                     double finalPriorityScore) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.weaknessScore = weaknessScore;
        this.examImportance = examImportance;
        this.prerequisiteImpact = prerequisiteImpact;
        this.forgettingRisk = forgettingRisk;
        this.difficultyScore = difficultyScore;
        this.finalPriorityScore = finalPriorityScore;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public double getWeaknessScore() {
        return weaknessScore;
    }

    public void setWeaknessScore(double weaknessScore) {
        this.weaknessScore = weaknessScore;
    }

    public double getExamImportance() {
        return examImportance;
    }

    public void setExamImportance(double examImportance) {
        this.examImportance = examImportance;
    }

    public double getPrerequisiteImpact() {
        return prerequisiteImpact;
    }

    public void setPrerequisiteImpact(double prerequisiteImpact) {
        this.prerequisiteImpact = prerequisiteImpact;
    }

    public double getForgettingRisk() {
        return forgettingRisk;
    }

    public void setForgettingRisk(double forgettingRisk) {
        this.forgettingRisk = forgettingRisk;
    }

    public double getDifficultyScore() {
        return difficultyScore;
    }

    public void setDifficultyScore(double difficultyScore) {
        this.difficultyScore = difficultyScore;
    }

    public double getFinalPriorityScore() {
        return finalPriorityScore;
    }

    public void setFinalPriorityScore(double finalPriorityScore) {
        this.finalPriorityScore = finalPriorityScore;
    }
}