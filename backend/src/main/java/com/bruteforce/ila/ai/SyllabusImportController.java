package com.bruteforce.ila.ai;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/syllabus-import")
public class SyllabusImportController {

    private final SyllabusImportService syllabusImportService;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public SyllabusImportController(SyllabusImportService syllabusImportService,
                                    com.bruteforce.ila.security.AccessGuard accessGuard) {
        this.syllabusImportService = syllabusImportService;
        this.accessGuard = accessGuard;
    }

    @PostMapping(value = "/subject", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importForSubject(
            @RequestParam("file") MultipartFile file, @RequestParam("subjectId") Long subjectId) {
        accessGuard.requireSubjectOwner(subjectId);
        int count = syllabusImportService.importSyllabus(file, subjectId);
        return ResponseEntity.ok(Map.of("topicsCreated", count));
    }

    @PostMapping(value = "/unit", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> importForUnit(
            @RequestParam("file") MultipartFile file, @RequestParam("unitId") Long unitId) {
        accessGuard.requireUnitOwner(unitId);
        int count = syllabusImportService.importTopicsIntoUnit(file, unitId);
        return ResponseEntity.ok(Map.of("topicsCreated", count));
    }
}