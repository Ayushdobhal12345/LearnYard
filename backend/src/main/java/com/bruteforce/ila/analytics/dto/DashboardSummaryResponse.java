package com.bruteforce.ila.analytics.dto;

import java.util.List;

public class DashboardSummaryResponse {

    private Double overallMasteryPercentage;
    private Integer totalTopicsTracked;
    private Integer weakTopicsCount;
    private List<String> weakTopicNames;
    private Integer topicsDueForRevisionCount;
    private Boolean hasActiveStudyPlan;
    private Integer totalQuizAttempts;
    private Integer streakDays;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(Double overallMasteryPercentage, Integer totalTopicsTracked,
                                    Integer weakTopicsCount, List<String> weakTopicNames,
                                    Integer topicsDueForRevisionCount, Boolean hasActiveStudyPlan,
                                    Integer totalQuizAttempts, Integer streakDays) {
        this.overallMasteryPercentage = overallMasteryPercentage;
        this.totalTopicsTracked = totalTopicsTracked;
        this.weakTopicsCount = weakTopicsCount;
        this.weakTopicNames = weakTopicNames;
        this.topicsDueForRevisionCount = topicsDueForRevisionCount;
        this.hasActiveStudyPlan = hasActiveStudyPlan;
        this.totalQuizAttempts = totalQuizAttempts;
        this.streakDays = streakDays;
    }

    public Double getOverallMasteryPercentage() {
        return overallMasteryPercentage;
    }

    public void setOverallMasteryPercentage(Double overallMasteryPercentage) {
        this.overallMasteryPercentage = overallMasteryPercentage;
    }

    public Integer getTotalTopicsTracked() {
        return totalTopicsTracked;
    }

    public void setTotalTopicsTracked(Integer totalTopicsTracked) {
        this.totalTopicsTracked = totalTopicsTracked;
    }

    public Integer getWeakTopicsCount() {
        return weakTopicsCount;
    }

    public void setWeakTopicsCount(Integer weakTopicsCount) {
        this.weakTopicsCount = weakTopicsCount;
    }

    public List<String> getWeakTopicNames() {
        return weakTopicNames;
    }

    public void setWeakTopicNames(List<String> weakTopicNames) {
        this.weakTopicNames = weakTopicNames;
    }

    public Integer getTopicsDueForRevisionCount() {
        return topicsDueForRevisionCount;
    }

    public void setTopicsDueForRevisionCount(Integer topicsDueForRevisionCount) {
        this.topicsDueForRevisionCount = topicsDueForRevisionCount;
    }

    public Boolean getHasActiveStudyPlan() {
        return hasActiveStudyPlan;
    }

    public void setHasActiveStudyPlan(Boolean hasActiveStudyPlan) {
        this.hasActiveStudyPlan = hasActiveStudyPlan;
    }

    public Integer getTotalQuizAttempts() {
        return totalQuizAttempts;
    }

    public void setTotalQuizAttempts(Integer totalQuizAttempts) {
        this.totalQuizAttempts = totalQuizAttempts;
    }

    public Integer getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(Integer streakDays) {
        this.streakDays = streakDays;
    }
}