package com.bruteforce.ila.subject;

import com.bruteforce.ila.curriculum.CascadeDeletionService;
import com.bruteforce.ila.subject.dto.SubjectRequest;
import com.bruteforce.ila.subject.dto.SubjectResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final com.bruteforce.ila.curriculum.CascadeDeletionService cascadeDeletionService;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public SubjectController(SubjectService subjectService, CascadeDeletionService cascadeDeletionService,
                             com.bruteforce.ila.security.AccessGuard accessGuard) {
        this.subjectService = subjectService;
        this.cascadeDeletionService = cascadeDeletionService;
        this.accessGuard = accessGuard;
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody SubjectRequest request,
                                                         Authentication authentication) {
        Long ownerId = (Long) authentication.getDetails();
        Subject subject = subjectService.createSubject(request.getName(), request.getDescription(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(subject));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        accessGuard.requireSubjectOwner(id);
        cascadeDeletionService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponse>> getMySubjects(Authentication authentication) {
        Long ownerId = (Long) authentication.getDetails();
        List<SubjectResponse> subjects = subjectService.getSubjectsForOwner(ownerId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getSubjectById(@PathVariable Long id) {
        accessGuard.requireSubjectOwner(id);
        return ResponseEntity.ok(toResponse(subjectService.getSubjectById(id)));
    }

    private SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getDescription(),
                subject.getCreatedAt(), subject.getUpdatedAt());
    }
}