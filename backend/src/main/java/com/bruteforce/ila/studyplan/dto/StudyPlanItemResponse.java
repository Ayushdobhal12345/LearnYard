package com.bruteforce.ila.studyplan.dto;

public class StudyPlanItemResponse {

    private Long topicId;
    private String topicName;
    private Double priorityScoreSnapshot;
    private Double allocatedHours;
    private Integer itemOrder;

    public StudyPlanItemResponse() {
    }

    public StudyPlanItemResponse(Long topicId, String topicName, Double priorityScoreSnapshot,
                                 Double allocatedHours, Integer itemOrder) {
        this.topicId = topicId;
        this.topicName = topicName;
        this.priorityScoreSnapshot = priorityScoreSnapshot;
        this.allocatedHours = allocatedHours;
        this.itemOrder = itemOrder;
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

    public Double getPriorityScoreSnapshot() {
        return priorityScoreSnapshot;
    }

    public void setPriorityScoreSnapshot(Double priorityScoreSnapshot) {
        this.priorityScoreSnapshot = priorityScoreSnapshot;
    }

    public Double getAllocatedHours() {
        return allocatedHours;
    }

    public void setAllocatedHours(Double allocatedHours) {
        this.allocatedHours = allocatedHours;
    }

    public Integer getItemOrder() {
        return itemOrder;
    }

    public void setItemOrder(Integer itemOrder) {
        this.itemOrder = itemOrder;
    }
}