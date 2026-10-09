package com.bruteforce.ila.quiz.dto;

import jakarta.validation.constraints.NotNull;

public class SubmitAnswerRequest {

    @NotNull(message = "questionId is required")
    private Long questionId;

    @NotNull(message = "selectedOption is required")
    private String selectedOption;

    public SubmitAnswerRequest() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
}