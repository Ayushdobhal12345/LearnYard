package com.bruteforce.ila.pyq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PYQQuestionRequest {

    @NotBlank(message = "questionText is required")
    private String questionText;

    @NotNull(message = "marks is required")
    private Integer marks;

    @NotNull(message = "paperId is required")
    private Long paperId;

    @NotNull(message = "topicId is required")
    private Long topicId;

    public PYQQuestionRequest() {
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }
}