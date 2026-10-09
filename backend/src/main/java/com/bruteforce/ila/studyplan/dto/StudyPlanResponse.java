package com.bruteforce.ila.studyplan.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StudyPlanResponse {

    private Long id;
    private Long studentId;
    private Double availableHours;
    private Double hoursUntilExam;
    private LocalDateTime generatedAt;
    private Boolean isActive;
    private List<StudyPlanItemResponse> items;

    public StudyPlanResponse() {
    }

    public StudyPlanResponse(Long id, Long studentId, Double availableHours, Double hoursUntilExam,
                             LocalDateTime generatedAt, Boolean isActive, List<StudyPlanItemResponse> items) {
        this.id = id;
        this.studentId = studentId;
        this.availableHours = availableHours;
        this.hoursUntilExam = hoursUntilExam;
        this.generatedAt = generatedAt;
        this.isActive = isActive;
        this.items = items;
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

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public List<StudyPlanItemResponse> getItems() {
        return items;
    }

    public void setItems(List<StudyPlanItemResponse> items) {
        this.items = items;
    }
}