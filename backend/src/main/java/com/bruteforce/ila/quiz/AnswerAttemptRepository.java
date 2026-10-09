package com.bruteforce.ila.quiz;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnswerAttemptRepository extends JpaRepository<AnswerAttempt, Long> {

    void deleteByQuestion_Topic_Id(Long topicId);

    List<AnswerAttempt> findByQuizAttempt_Student_Id(Long studentId);

}