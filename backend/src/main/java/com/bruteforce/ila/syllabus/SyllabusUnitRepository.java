package com.bruteforce.ila.syllabus;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SyllabusUnitRepository extends JpaRepository<SyllabusUnit, Long> {

    List<SyllabusUnit> findBySubjectIdOrderByUnitOrderAsc(Long subjectId);
}