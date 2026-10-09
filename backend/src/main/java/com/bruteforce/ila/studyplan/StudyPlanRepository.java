package com.bruteforce.ila.studyplan;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    Optional<StudyPlan> findByStudentIdAndSubjectIdAndIsActiveTrue(
            Long studentId,
            Long subjectId
    );

    List<StudyPlan> findByStudentIdAndSubjectIdOrderByGeneratedAtDesc(
            Long studentId,
            Long subjectId
    );

    boolean existsByStudentIdAndIsActiveTrue(Long studentId);

    void deleteBySubjectId(Long subjectId);
}