package com.bruteforce.ila.syllabus;

import com.bruteforce.ila.subject.Subject;
import com.bruteforce.ila.subject.SubjectRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class SyllabusUnitService {

    private final SyllabusUnitRepository unitRepository;
    private final SubjectRepository subjectRepository;

    public SyllabusUnitService(SyllabusUnitRepository unitRepository,
                               SubjectRepository subjectRepository) {
        this.unitRepository = unitRepository;
        this.subjectRepository = subjectRepository;
    }

    public void deleteUnit(Long id) {
        unitRepository.deleteById(id);
    }

    public SyllabusUnit createUnit(String name, String description, Integer unitOrder, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NoSuchElementException("Subject with id " + subjectId + " not found."));
        SyllabusUnit unit = new SyllabusUnit(name, description, unitOrder, subject);
        return unitRepository.save(unit);
    }

    public List<SyllabusUnit> getUnitsBySubject(Long subjectId) {
        return unitRepository.findBySubjectIdOrderByUnitOrderAsc(subjectId);
    }

    public SyllabusUnit getUnitById(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Unit with id " + id + " not found."));
    }
}