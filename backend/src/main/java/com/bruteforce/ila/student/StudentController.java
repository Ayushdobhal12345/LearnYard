package com.bruteforce.ila.student;

import com.bruteforce.ila.student.dto.StudentRequest;
import com.bruteforce.ila.student.dto.StudentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        Student student = studentService.createStudent(request.getName(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(student));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(studentService.getStudentById(id)));
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(student.getId(), student.getName(), student.getEmail(), student.getCreatedAt());
    }
}