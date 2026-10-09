package com.bruteforce.ila.revision;

import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.syllabus.Topic;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "revision_schedules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "topic_id"}))
public class RevisionSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // The last time this topic was marked as "revised" (resets forgetting risk conceptually)
    @Column(name = "last_revised_at")
    private LocalDateTime lastRevisedAt;

    public RevisionSchedule() {
    }

    public RevisionSchedule(Student student, Topic topic) {
        this.student = student;
        this.topic = topic;
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

    public LocalDateTime getLastRevisedAt() {
        return lastRevisedAt;
    }

    public void setLastRevisedAt(LocalDateTime lastRevisedAt) {
        this.lastRevisedAt = lastRevisedAt;
    }
}