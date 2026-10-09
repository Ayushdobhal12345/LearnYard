package com.bruteforce.ila.dependency.dto;

public class DependencyResponse {

    private Long id;
    private Long topicId;
    private String topicName;
    private Long prerequisiteTopicId;
    private String prerequisiteTopicName;

    public DependencyResponse() {
    }

    public DependencyResponse(Long id, Long topicId, String topicName,
                              Long prerequisiteTopicId, String prerequisiteTopicName) {
        this.id = id;
        this.topicId = topicId;
        this.topicName = topicName;
        this.prerequisiteTopicId = prerequisiteTopicId;
        this.prerequisiteTopicName = prerequisiteTopicName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getPrerequisiteTopicId() {
        return prerequisiteTopicId;
    }

    public void setPrerequisiteTopicId(Long prerequisiteTopicId) {
        this.prerequisiteTopicId = prerequisiteTopicId;
    }

    public String getPrerequisiteTopicName() {
        return prerequisiteTopicName;
    }

    public void setPrerequisiteTopicName(String prerequisiteTopicName) {
        this.prerequisiteTopicName = prerequisiteTopicName;
    }
}