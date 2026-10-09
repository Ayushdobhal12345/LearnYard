package com.bruteforce.ila.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class VivaGradeRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "topicId is required")
    private Long topicId;

    @Valid
    @NotEmpty(message = "answers are required")
    @Size(max = 10, message = "A viva can have at most 10 questions")
    private List<VivaAnswer> answers;

    public VivaGradeRequest() {
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

    public List<VivaAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<VivaAnswer> answers) {
        this.answers = answers;
    }

    public static class VivaAnswer {

        @NotBlank(message = "question is required")
        @Size(max = 1000, message = "question is too long")
        private String question;

        @Size(max = 4000, message = "answer is too long")
        private String answer;

        public VivaAnswer() {
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
