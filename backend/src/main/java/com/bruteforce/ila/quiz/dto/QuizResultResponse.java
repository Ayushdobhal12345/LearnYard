package com.bruteforce.ila.quiz.dto;

import java.time.LocalDateTime;

public class QuizResultResponse {

    private Long quizAttemptId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double scorePercentage;
    private LocalDateTime submittedAt;

    public QuizResultResponse() {
    }

    public QuizResultResponse(Long quizAttemptId, Integer totalQuestions, Integer correctAnswers,
                              Double scorePercentage, LocalDateTime submittedAt) {
        this.quizAttemptId = quizAttemptId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.scorePercentage = scorePercentage;
        this.submittedAt = submittedAt;
    }

    public Long getQuizAttemptId() {
        return quizAttemptId;
    }

    public void setQuizAttemptId(Long quizAttemptId) {
        this.quizAttemptId = quizAttemptId;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Double getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(Double scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}