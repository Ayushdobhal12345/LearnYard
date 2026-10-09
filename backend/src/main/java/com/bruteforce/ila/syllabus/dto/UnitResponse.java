package com.bruteforce.ila.syllabus.dto;

public class UnitResponse {

    private Long id;
    private String name;
    private String description;
    private Integer unitOrder;
    private Long subjectId;
    private String subjectName;

    public UnitResponse() {
    }

    public UnitResponse(Long id, String name, String description, Integer unitOrder,
                        Long subjectId, String subjectName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unitOrder = unitOrder;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getUnitOrder() {
        return unitOrder;
    }

    public void setUnitOrder(Integer unitOrder) {
        this.unitOrder = unitOrder;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}