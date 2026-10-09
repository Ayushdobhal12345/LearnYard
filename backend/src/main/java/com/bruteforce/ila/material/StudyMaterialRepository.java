package com.bruteforce.ila.material;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {

    List<StudyMaterial> findByTopicId(Long topicId);
    void deleteByTopicId(Long topicId);
    List<StudyMaterial> findByUnitId(Long unitId);

    void deleteByUnitId(Long unitId);
}