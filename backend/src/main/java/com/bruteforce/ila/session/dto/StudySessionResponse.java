package com.bruteforce.ila.session.dto;

import java.time.LocalDateTime;

public class StudySessionResponse {

    private Long id;
    private Long topicId;
    private String topicName;
    private Integer durationMinutes;
    private LocalDateTime sessionDate;

    public StudySessionResponse() {
    }

    public StudySessionResponse(Long id, Long topicId, String topicName,
                                Integer durationMinutes, LocalDateTime sessionDate) {
        this.id = id;
        this.topicId = topicId;
        this.topicName = topicName;
        this.durationMinutes = durationMinutes;
        this.sessionDate = sessionDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }
}