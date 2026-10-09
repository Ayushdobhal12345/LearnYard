package com.bruteforce.ila.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTopicId(Long topicId);
    void deleteByTopicId(Long topicId);
}