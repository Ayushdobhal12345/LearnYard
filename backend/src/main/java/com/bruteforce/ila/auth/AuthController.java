package com.bruteforce.ila.auth;

import com.bruteforce.ila.auth.dto.AuthResponse;
import com.bruteforce.ila.auth.dto.LoginRequest;
import com.bruteforce.ila.auth.dto.RegisterRequest;
import com.bruteforce.ila.common.exception.DuplicateResourceException;
import com.bruteforce.ila.security.JwtService;
import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.student.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }

        Student student = new Student(request.getName(), request.getEmail());
        student.setPassword(passwordEncoder.encode(request.getPassword()));
        student = studentRepository.save(student);

        String token = jwtService.generateToken(student.getId(), student.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, student.getId(), student.getName(), student.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Student student = studentRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (student.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String token = jwtService.generateToken(student.getId(), student.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, student.getId(), student.getName(), student.getEmail()));
    }

    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody com.bruteforce.ila.auth.dto.UpdateProfileRequest request,
            org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new java.util.NoSuchElementException("Student not found."));
        student.setName(request.getName());
        studentRepository.save(student);
        String token = jwtService.generateToken(student.getId(), student.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, student.getId(), student.getName(), student.getEmail()));
    }
}