package com.bruteforce.ila.ai;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class DependencyInferenceController {

    private final DependencyInferenceService dependencyInferenceService;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public DependencyInferenceController(DependencyInferenceService dependencyInferenceService,
                                         com.bruteforce.ila.security.AccessGuard accessGuard) {
        this.dependencyInferenceService = dependencyInferenceService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/infer-dependencies/{subjectId}")
    public ResponseEntity<Map<String, Object>> infer(@PathVariable Long subjectId) {
        accessGuard.requireSubjectOwner(subjectId);
        int count = dependencyInferenceService.inferDependenciesForSubject(subjectId);
        return ResponseEntity.ok(Map.of("dependenciesCreated", count));
    }
}