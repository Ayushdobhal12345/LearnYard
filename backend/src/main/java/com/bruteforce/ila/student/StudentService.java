package com.bruteforce.ila.student;

import com.bruteforce.ila.common.exception.DuplicateResourceException;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(String name, String email) {
        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Student with email '" + email + "' already exists.");
        }
        Student student = new Student(name, email);
        return studentRepository.save(student);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student with id " + id + " not found."));
    }
}