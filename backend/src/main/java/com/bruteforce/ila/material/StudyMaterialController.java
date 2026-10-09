package com.bruteforce.ila.material;

import com.bruteforce.ila.material.dto.StudyMaterialResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/materials")
public class StudyMaterialController {

    private final StudyMaterialService studyMaterialService;
    private final com.bruteforce.ila.rag.RagIngestionService ragIngestionService;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public StudyMaterialController(StudyMaterialService studyMaterialService,
                                   com.bruteforce.ila.rag.RagIngestionService ragIngestionService,
                                   com.bruteforce.ila.security.AccessGuard accessGuard) {
        this.studyMaterialService = studyMaterialService;
        this.ragIngestionService = ragIngestionService;
        this.accessGuard = accessGuard;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<StudyMaterialResponse> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("topicId") Long topicId) {

        accessGuard.requireTopicOwner(topicId);

        StudyMaterial material = studyMaterialService.uploadMaterial(file, topicId);

        ragIngestionService.processMaterial(
                material.getStoredFilePath(),
                topicId,
                material.getId());

        studyMaterialService.markAsProcessed(material.getId());
        material.setIsProcessed(true);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(material));
    }

    @PostMapping(value = "/by-unit", consumes = "multipart/form-data")
    public ResponseEntity<StudyMaterialResponse> uploadForUnit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("unitId") Long unitId) {

        accessGuard.requireUnitOwner(unitId);

        StudyMaterial material =
                studyMaterialService.uploadMaterialForUnit(file, unitId);

        ragIngestionService.processMaterialForUnit(
                material.getStoredFilePath(),
                unitId,
                material.getId());

        studyMaterialService.markAsProcessed(material.getId());
        material.setIsProcessed(true);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(material));
    }

    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<List<StudyMaterialResponse>> getMaterialsForTopic(
            @PathVariable Long topicId) {

        accessGuard.requireTopicOwner(topicId);

        List<StudyMaterialResponse> result =
                studyMaterialService.getMaterialsForTopic(topicId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<List<StudyMaterialResponse>> getMaterialsForUnit(
            @PathVariable Long unitId) {

        accessGuard.requireUnitOwner(unitId);

        List<StudyMaterialResponse> result =
                studyMaterialService.getMaterialsForUnit(unitId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(result);
    }

    private StudyMaterialResponse toResponse(StudyMaterial m) {
        return new StudyMaterialResponse(
                m.getId(),
                m.getOriginalFileName(),
                m.getTopic() != null ? m.getTopic().getId() : null,
                m.getTopic() != null ? m.getTopic().getName() : null,
                m.getUnit() != null ? m.getUnit().getId() : null,
                m.getUnit() != null ? m.getUnit().getName() : null,
                m.getIsProcessed(),
                m.getUploadedAt());
    }
}