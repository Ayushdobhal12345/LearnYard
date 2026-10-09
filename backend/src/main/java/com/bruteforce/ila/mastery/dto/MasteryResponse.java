package com.bruteforce.ila.mastery.dto;

import java.time.LocalDateTime;

public class MasteryResponse {

    private Long id;
    private Long studentId;
    private Long topicId;
    private String topicName;
    private Double masteryScore;
    private Integer totalAttempts;
    private Integer correctAttempts;
    private LocalDateTime lastPracticedAt;

    public MasteryResponse() {
    }

    public MasteryResponse(Long id, Long studentId, Long topicId, String topicName,
                           Double masteryScore, Integer totalAttempts, Integer correctAttempts,
                           LocalDateTime lastPracticedAt) {
        this.id = id;
        this.studentId = studentId;
        this.topicId = topicId;
        this.topicName = topicName;
        this.masteryScore = masteryScore;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.lastPracticedAt = lastPracticedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(Integer totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Integer getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(Integer correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public LocalDateTime getLastPracticedAt() {
        return lastPracticedAt;
    }

    public void setLastPracticedAt(LocalDateTime lastPracticedAt) {
        this.lastPracticedAt = lastPracticedAt;
    }
}