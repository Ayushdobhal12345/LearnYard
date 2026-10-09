package com.bruteforce.ila.pyq;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PYQPaperRepository extends JpaRepository<PYQPaper, Long> {
    List<PYQPaper> findBySubjectId(Long subjectId);
    void deleteBySubjectId(Long subjectId);
}