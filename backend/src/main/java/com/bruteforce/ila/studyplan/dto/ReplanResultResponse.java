package com.bruteforce.ila.studyplan.dto;

public class ReplanResultResponse {

    private boolean planRegenerated;
    private Long oldPlanId;
    private Long newPlanId;
    private String message;

    public ReplanResultResponse() {
    }

    public ReplanResultResponse(boolean planRegenerated, Long oldPlanId, Long newPlanId, String message) {
        this.planRegenerated = planRegenerated;
        this.oldPlanId = oldPlanId;
        this.newPlanId = newPlanId;
        this.message = message;
    }

    public boolean isPlanRegenerated() {
        return planRegenerated;
    }

    public void setPlanRegenerated(boolean planRegenerated) {
        this.planRegenerated = planRegenerated;
    }

    public Long getOldPlanId() {
        return oldPlanId;
    }

    public void setOldPlanId(Long oldPlanId) {
        this.oldPlanId = oldPlanId;
    }

    public Long getNewPlanId() {
        return newPlanId;
    }

    public void setNewPlanId(Long newPlanId) {
        this.newPlanId = newPlanId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}