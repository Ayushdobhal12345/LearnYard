package com.bruteforce.ila.material;

import com.bruteforce.ila.syllabus.SyllabusUnit;
import com.bruteforce.ila.syllabus.Topic;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_materials")
public class StudyMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 500)
    private String storedFilePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private SyllabusUnit unit;

    @Column(name = "is_processed", nullable = false)
    private Boolean isProcessed;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    public StudyMaterial() {
    }

    public StudyMaterial(String originalFileName, String storedFilePath, Topic topic) {
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
        this.topic = topic;
        this.isProcessed = false;
    }

    public StudyMaterial(String originalFileName, String storedFilePath, SyllabusUnit unit) {
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
        this.unit = unit;
        this.isProcessed = false;
    }

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFilePath() {
        return storedFilePath;
    }

    public void setStoredFilePath(String storedFilePath) {
        this.storedFilePath = storedFilePath;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public SyllabusUnit getUnit() {
        return unit;
    }

    public void setUnit(SyllabusUnit unit) {
        this.unit = unit;
    }

    public Boolean getIsProcessed() {
        return isProcessed;
    }

    public void setIsProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}