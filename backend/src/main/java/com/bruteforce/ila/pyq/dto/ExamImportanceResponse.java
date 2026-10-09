package com.bruteforce.ila.pyq.dto;

public class ExamImportanceResponse {

    private Long topicId;
    private long frequency;
    private Integer totalMarks;
    private double examImportanceScore;

    public ExamImportanceResponse() {
    }

    public ExamImportanceResponse(Long topicId, long frequency, Integer totalMarks, double examImportanceScore) {
        this.topicId = topicId;
        this.frequency = frequency;
        this.totalMarks = totalMarks;
        this.examImportanceScore = examImportanceScore;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public double getExamImportanceScore() {
        return examImportanceScore;
    }

    public void setExamImportanceScore(double examImportanceScore) {
        this.examImportanceScore = examImportanceScore;
    }
}