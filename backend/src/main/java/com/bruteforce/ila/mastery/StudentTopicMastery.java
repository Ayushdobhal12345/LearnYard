package com.bruteforce.ila.mastery;

import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.syllabus.Topic;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_topic_mastery",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "topic_id"}))
public class StudentTopicMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // Mastery score from 0.0 (no understanding) to 100.0 (fully mastered)
    @Column(name = "mastery_score", nullable = false)
    private Double masteryScore;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts;

    @Column(name = "correct_attempts", nullable = false)
    private Integer correctAttempts;

    @Column(name = "last_practiced_at")
    private LocalDateTime lastPracticedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public StudentTopicMastery() {
    }

    public StudentTopicMastery(Student student, Topic topic) {
        this.student = student;
        this.topic = topic;
        // Every topic starts at a neutral, "unknown" mastery level of 50
        this.masteryScore = 50.0;
        this.totalAttempts = 0;
        this.correctAttempts = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(Double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public Integer getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(Integer totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Integer getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(Integer correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public LocalDateTime getLastPracticedAt() {
        return lastPracticedAt;
    }

    public void setLastPracticedAt(LocalDateTime lastPracticedAt) {
        this.lastPracticedAt = lastPracticedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}