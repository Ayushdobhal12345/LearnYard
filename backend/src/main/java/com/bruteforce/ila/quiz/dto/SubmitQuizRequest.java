package com.bruteforce.ila.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SubmitQuizRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<SubmitAnswerRequest> answers;

    public SubmitQuizRequest() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public List<SubmitAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SubmitAnswerRequest> answers) {
        this.answers = answers;
    }
}