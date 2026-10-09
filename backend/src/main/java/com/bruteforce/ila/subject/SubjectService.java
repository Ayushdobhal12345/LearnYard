package com.bruteforce.ila.subject;

import com.bruteforce.ila.common.exception.DuplicateResourceException;
import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.student.StudentRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;

    public SubjectService(SubjectRepository subjectRepository, StudentRepository studentRepository) {
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
    }

    public Subject createSubject(String name, String description, Long ownerId) {
        if (subjectRepository.existsByNameAndOwnerId(name, ownerId)) {
            throw new DuplicateResourceException("You already have a subject named '" + name + "'.");
        }
        Student owner = studentRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Student with id " + ownerId + " not found."));
        Subject subject = new Subject(name, description);
        subject.setOwner(owner);
        return subjectRepository.save(subject);
    }

    public List<Subject> getSubjectsForOwner(Long ownerId) {
        return subjectRepository.findByOwnerId(ownerId);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subject with id " + id + " not found."));
    }
}