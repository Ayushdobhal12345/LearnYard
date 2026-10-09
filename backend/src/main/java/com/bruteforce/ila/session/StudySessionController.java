package com.bruteforce.ila.session;

import com.bruteforce.ila.session.dto.StudySessionRequest;
import com.bruteforce.ila.session.dto.StudySessionResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {

    private final StudySessionService sessionService;

    public StudySessionController(StudySessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<StudySessionResponse> logSession(@Valid @RequestBody StudySessionRequest request) {
        StudySession session = sessionService.logSession(
                request.getStudentId(), request.getTopicId(), request.getDurationMinutes());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(session));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudySessionResponse>> getSessionsForStudent(@PathVariable Long studentId) {
        List<StudySessionResponse> sessions = sessionService.getSessionsForStudent(studentId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(sessions);
    }

    private StudySessionResponse toResponse(StudySession session) {
        return new StudySessionResponse(
                session.getId(), session.getTopic().getId(), session.getTopic().getName(),
                session.getDurationMinutes(), session.getSessionDate());
    }
}