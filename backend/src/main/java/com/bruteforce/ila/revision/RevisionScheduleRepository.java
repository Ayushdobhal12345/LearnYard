package com.bruteforce.ila.revision;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RevisionScheduleRepository extends JpaRepository<RevisionSchedule, Long> {

    Optional<RevisionSchedule> findByStudentIdAndTopicId(Long studentId, Long topicId);

    List<RevisionSchedule> findByStudentId(Long studentId);
    void deleteByTopicId(Long topicId);
}