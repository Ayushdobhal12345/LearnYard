package com.bruteforce.ila.mastery;

import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.syllabus.Topic;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mastery_history")
public class MasteryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "mastery_score", nullable = false)
    private Double masteryScore;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    public MasteryHistory() {
    }

    public MasteryHistory(Student student, Topic topic, Double masteryScore) {
        this.student = student;
        this.topic = topic;
        this.masteryScore = masteryScore;
    }

    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Topic getTopic() { return topic; }
    public void setTopic(Topic topic) { this.topic = topic; }
    public Double getMasteryScore() { return masteryScore; }
    public void setMasteryScore(Double masteryScore) { this.masteryScore = masteryScore; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}