package com.bruteforce.ila.mastery.dto;

import jakarta.validation.constraints.NotNull;

public class RecordAnswerRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "topicId is required")
    private Long topicId;

    @NotNull(message = "wasCorrect is required")
    private Boolean wasCorrect;

    @NotNull(message = "difficultyLevel is required")
    private Integer difficultyLevel;

    public RecordAnswerRequest() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Boolean getWasCorrect() {
        return wasCorrect;
    }

    public void setWasCorrect(Boolean wasCorrect) {
        this.wasCorrect = wasCorrect;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}