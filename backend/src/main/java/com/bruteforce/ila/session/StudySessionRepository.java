package com.bruteforce.ila.session;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByStudentIdAndTopicIdOrderBySessionDateDesc(Long studentId, Long topicId);

    // The single most recent session for a student on a topic - used by the Revision Scheduler
    Optional<StudySession> findFirstByStudentIdAndTopicIdOrderBySessionDateDesc(Long studentId, Long topicId);

    List<StudySession> findByStudentIdOrderBySessionDateDesc(Long studentId);
    void deleteByTopicId(Long topicId);
}