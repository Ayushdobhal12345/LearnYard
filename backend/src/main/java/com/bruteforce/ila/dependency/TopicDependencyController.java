package com.bruteforce.ila.dependency;

import com.bruteforce.ila.dependency.dto.DependencyRequest;
import com.bruteforce.ila.dependency.dto.DependencyResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dependencies")
public class TopicDependencyController {

    private final TopicDependencyService dependencyService;

    public TopicDependencyController(TopicDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @PostMapping
    public ResponseEntity<DependencyResponse> createDependency(@Valid @RequestBody DependencyRequest request) {
        TopicDependency dependency = dependencyService.createDependency(
                request.getTopicId(), request.getPrerequisiteTopicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(dependency));
    }

    @GetMapping("/prerequisites-of/{topicId}")
    public ResponseEntity<List<DependencyResponse>> getPrerequisitesOf(@PathVariable Long topicId) {
        List<DependencyResponse> result = dependencyService.getPrerequisitesOf(topicId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/depends-on/{topicId}")
    public ResponseEntity<List<DependencyResponse>> getTopicsThatDependOn(@PathVariable Long topicId) {
        List<DependencyResponse> result = dependencyService.getTopicsThatDependOn(topicId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(result);
    }

    private DependencyResponse toResponse(TopicDependency dependency) {
        return new DependencyResponse(
                dependency.getId(),
                dependency.getTopic().getId(), dependency.getTopic().getName(),
                dependency.getPrerequisiteTopic().getId(), dependency.getPrerequisiteTopic().getName());
    }

    @GetMapping("/by-subject/{subjectId}")
    public ResponseEntity<List<DependencyResponse>> getBySubject(@PathVariable Long subjectId) {
        List<DependencyResponse> result = dependencyService.getDependenciesForSubject(subjectId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDependency(@PathVariable Long id) {
        dependencyService.deleteDependency(id);
        return ResponseEntity.noContent().build();
    }
}