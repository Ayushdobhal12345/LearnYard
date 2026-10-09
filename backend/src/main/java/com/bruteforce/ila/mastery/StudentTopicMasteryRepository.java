package com.bruteforce.ila.mastery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentTopicMasteryRepository extends JpaRepository<StudentTopicMastery, Long> {

    Optional<StudentTopicMastery> findByStudentIdAndTopicId(Long studentId, Long topicId);

    List<StudentTopicMastery> findByStudentId(Long studentId);
    void deleteByTopicId(Long topicId);

    // Topics where mastery is below a threshold - this powers "Weakness Detection"
    List<StudentTopicMastery> findByStudentIdAndMasteryScoreLessThan(Long studentId, Double threshold);
}