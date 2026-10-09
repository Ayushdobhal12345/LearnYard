package com.bruteforce.ila.studyplan;

import com.bruteforce.ila.syllabus.Topic;
import jakarta.persistence.*;

@Entity
@Table(name = "study_plan_items")
public class StudyPlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id", nullable = false)
    private StudyPlan studyPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // The priority score this topic had AT THE TIME this plan was generated - a snapshot
    @Column(name = "priority_score_snapshot", nullable = false)
    private Double priorityScoreSnapshot;

    @Column(name = "allocated_hours", nullable = false)
    private Double allocatedHours;

    // The order topics should be studied in within this plan (1 = study first)
    @Column(name = "item_order")
    private Integer itemOrder;

    public StudyPlanItem() {
    }

    public StudyPlanItem(StudyPlan studyPlan, Topic topic, Double priorityScoreSnapshot,
                         Double allocatedHours, Integer itemOrder) {
        this.studyPlan = studyPlan;
        this.topic = topic;
        this.priorityScoreSnapshot = priorityScoreSnapshot;
        this.allocatedHours = allocatedHours;
        this.itemOrder = itemOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudyPlan getStudyPlan() {
        return studyPlan;
    }

    public void setStudyPlan(StudyPlan studyPlan) {
        this.studyPlan = studyPlan;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
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