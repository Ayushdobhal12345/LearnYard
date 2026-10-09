package com.bruteforce.ila.dependency;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicDependencyRepository extends JpaRepository<TopicDependency, Long> {

    // All prerequisites required BY this topic
    List<TopicDependency> findByTopicId(Long topicId);
    void deleteByTopicId(Long topicId);
    void deleteByPrerequisiteTopicId(Long topicId);

    // All topics that require THIS topic as a prerequisite
    // (this is what tells us "Prerequisite Impact" - how many topics depend on this one)
    List<TopicDependency> findByPrerequisiteTopicId(Long prerequisiteTopicId);

    boolean existsByTopicIdAndPrerequisiteTopicId(Long topicId, Long prerequisiteTopicId);
    @org.springframework.data.jpa.repository.Query(
            "SELECT d FROM TopicDependency d WHERE d.topic.unit.subject.id = :subjectId")
    java.util.List<TopicDependency> findBySubjectId(@org.springframework.data.repository.query.Param("subjectId") Long subjectId);
}