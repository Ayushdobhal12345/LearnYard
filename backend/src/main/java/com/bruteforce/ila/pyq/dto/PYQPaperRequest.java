package com.bruteforce.ila.pyq.dto;

import jakarta.validation.constraints.NotNull;

public class PYQPaperRequest {

    @NotNull(message = "year is required")
    private Integer year;

    @NotNull(message = "subjectId is required")
    private Long subjectId;

    public PYQPaperRequest() {
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}