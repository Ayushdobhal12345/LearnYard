package com.bruteforce.ila.mastery;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MasteryHistoryRepository extends JpaRepository<MasteryHistory, Long> {
    List<MasteryHistory> findByStudentIdAndTopicIdOrderByRecordedAtAsc(Long studentId, Long topicId);
    void deleteByTopicId(Long topicId);
}