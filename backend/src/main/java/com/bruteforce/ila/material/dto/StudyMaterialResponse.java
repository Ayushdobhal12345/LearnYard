package com.bruteforce.ila.material.dto;

import java.time.LocalDateTime;

public class StudyMaterialResponse {

    private Long id;
    private String originalFileName;

    private Long topicId;
    private String topicName;

    private Long unitId;
    private String unitName;

    private Boolean isProcessed;
    private LocalDateTime uploadedAt;

    public StudyMaterialResponse() {
    }

    public StudyMaterialResponse(
            Long id,
            String originalFileName,
            Long topicId,
            String topicName,
            Long unitId,
            String unitName,
            Boolean isProcessed,
            LocalDateTime uploadedAt) {

        this.id = id;
        this.originalFileName = originalFileName;
        this.topicId = topicId;
        this.topicName = topicName;
        this.unitId = unitId;
        this.unitName = unitName;
        this.isProcessed = isProcessed;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
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

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}