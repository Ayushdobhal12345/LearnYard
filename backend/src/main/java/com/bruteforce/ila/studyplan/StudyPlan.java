package com.bruteforce.ila.studyplan;

import com.bruteforce.ila.student.Student;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_plans")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Total hours the student said they have available when this plan was generated
    @Column(name = "available_hours", nullable = false)
    private Double availableHours;

    // Optional - hours until the exam, if the student specified one
    @Column(name = "hours_until_exam")
    private Double hoursUntilExam;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private com.bruteforce.ila.subject.Subject subject;

    public com.bruteforce.ila.subject.Subject getSubject() { return subject; }
    public void setSubject(com.bruteforce.ila.subject.Subject subject) { this.subject = subject; }

    // True only for the most recent plan for a student - lets us easily find "the current plan"
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "studyPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudyPlanItem> items = new ArrayList<>();

    public StudyPlan() {
    }

    public StudyPlan(Student student, Double availableHours, Double hoursUntilExam) {
        this.student = student;
        this.availableHours = availableHours;
        this.hoursUntilExam = hoursUntilExam;
        this.isActive = true;
    }

    @PrePersist
    protected void onCreate() {
        this.generatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
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

    public List<StudyPlanItem> getItems() {
        return items;
    }

    public void setItems(List<StudyPlanItem> items) {
        this.items = items;
    }
}