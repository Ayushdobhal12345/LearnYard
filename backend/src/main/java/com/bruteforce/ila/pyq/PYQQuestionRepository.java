package com.bruteforce.ila.pyq;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PYQQuestionRepository extends JpaRepository<PYQQuestion, Long> {

    List<PYQQuestion> findByTopicId(Long topicId);
    void deleteByTopicId(Long topicId);

    // How many times has this topic appeared across all PYQ papers?
    long countByTopicId(Long topicId);

    // Total marks this topic has been worth across all PYQ papers
    @Query("SELECT COALESCE(SUM(pq.marks), 0) FROM PYQQuestion pq WHERE pq.topic.id = :topicId")
    Integer sumMarksByTopicId(@Param("topicId") Long topicId);

    // The highest frequency count among ALL topics for a subject - used to normalize scores to 0-100
    @Query("SELECT COUNT(pq) FROM PYQQuestion pq " +
            "WHERE pq.topic.unit.subject.id = :subjectId " +
            "GROUP BY pq.topic.id " +
            "ORDER BY COUNT(pq) DESC")
    List<Long> getFrequencyCountsForSubject(@Param("subjectId") Long subjectId);
}