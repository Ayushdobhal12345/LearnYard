package com.bruteforce.ila.dependency.dto;

import jakarta.validation.constraints.NotNull;

public class DependencyRequest {

    @NotNull(message = "topicId is required")
    private Long topicId;

    @NotNull(message = "prerequisiteTopicId is required")
    private Long prerequisiteTopicId;

    public DependencyRequest() {
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getPrerequisiteTopicId() {
        return prerequisiteTopicId;
    }

    public void setPrerequisiteTopicId(Long prerequisiteTopicId) {
        this.prerequisiteTopicId = prerequisiteTopicId;
    }
}