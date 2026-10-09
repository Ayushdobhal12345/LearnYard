package com.bruteforce.ila.pyq;

import com.bruteforce.ila.syllabus.Topic;
import jakarta.persistence.*;

@Entity
@Table(name = "pyq_questions")
public class PYQQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Column(nullable = false)
    private Integer marks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pyq_paper_id", nullable = false)
    private PYQPaper pyqPaper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    public PYQQuestion() {
    }

    public PYQQuestion(String questionText, Integer marks, PYQPaper pyqPaper, Topic topic) {
        this.questionText = questionText;
        this.marks = marks;
        this.pyqPaper = pyqPaper;
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public PYQPaper getPyqPaper() {
        return pyqPaper;
    }

    public void setPyqPaper(PYQPaper pyqPaper) {
        this.pyqPaper = pyqPaper;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
}