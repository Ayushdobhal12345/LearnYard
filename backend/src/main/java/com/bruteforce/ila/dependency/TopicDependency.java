package com.bruteforce.ila.dependency;

import com.bruteforce.ila.syllabus.Topic;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "topic_dependencies")
public class TopicDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The topic that HAS the prerequisite (the one that depends on another)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    // The topic that MUST be learned first
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_topic_id", nullable = false)
    private Topic prerequisiteTopic;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public TopicDependency() {
    }

    public TopicDependency(Topic topic, Topic prerequisiteTopic) {
        this.topic = topic;
        this.prerequisiteTopic = prerequisiteTopic;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Topic getPrerequisiteTopic() {
        return prerequisiteTopic;
    }

    public void setPrerequisiteTopic(Topic prerequisiteTopic) {
        this.prerequisiteTopic = prerequisiteTopic;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}