package com.bruteforce.ila.mastery.dto;

import java.time.LocalDateTime;

public class MasteryHistoryResponse {
    private Double masteryScore;
    private LocalDateTime recordedAt;

    public MasteryHistoryResponse() {}
    public MasteryHistoryResponse(Double masteryScore, LocalDateTime recordedAt) {
        this.masteryScore = masteryScore;
        this.recordedAt = recordedAt;
    }
    public Double getMasteryScore() { return masteryScore; }
    public void setMasteryScore(Double masteryScore) { this.masteryScore = masteryScore; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}