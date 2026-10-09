package com.bruteforce.ila.studyplan;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyPlanItemRepository extends JpaRepository<StudyPlanItem, Long> {

    List<StudyPlanItem> findByStudyPlanIdOrderByItemOrderAsc(Long studyPlanId);
    void deleteByTopicId(Long topicId);

    void deleteByStudyPlan_Subject_Id(Long subjectId);
}