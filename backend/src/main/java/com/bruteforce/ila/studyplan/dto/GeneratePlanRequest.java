package com.bruteforce.ila.studyplan.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class GeneratePlanRequest {

    @NotNull(message = "studentId is required")
    private Long studentId;

    @NotNull(message = "subjectId is required")
    private Long subjectId;

    @NotNull(message = "availableHours is required")
    @Positive(message = "availableHours must be positive")
    private Double availableHours;

    private Double hoursUntilExam;

    public GeneratePlanRequest() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Double getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(Double availableHours) {
        this.availableHours = availableHours;
    }

    public Double getHoursUntilExam() {
        return hoursUntilExam;
    }

    public void setHoursUntilExam(Double hoursUntilExam) {
        this.hoursUntilExam = hoursUntilExam;
    }
}