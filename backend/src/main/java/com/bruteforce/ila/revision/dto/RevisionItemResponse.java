package com.bruteforce.ila.revision.dto;

public class RevisionItemResponse {

    private Long topicId;
    private String topicName;
    private Double masteryScore;
    private Double forgettingRisk;
    private Boolean dueForRevision;

    public RevisionItemResponse() {
    }

    public RevisionItemResponse(Long topicId, String topicName, Double masteryScore,
                                Double forgettingRisk, Boolean dueForRevision) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.masteryScore = masteryScore;
        this.forgettingRisk = forgettingRisk;
        this.dueForRevision = dueForRevision;
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

    public Double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(Double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public Double getForgettingRisk() {
        return forgettingRisk;
    }

    public void setForgettingRisk(Double forgettingRisk) {
        this.forgettingRisk = forgettingRisk;
    }

    public Boolean getDueForRevision() {
        return dueForRevision;
    }

    public void setDueForRevision(Boolean dueForRevision) {
        this.dueForRevision = dueForRevision;
    }
}