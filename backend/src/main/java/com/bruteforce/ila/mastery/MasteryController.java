package com.bruteforce.ila.mastery;

import com.bruteforce.ila.mastery.dto.MasteryResponse;
import com.bruteforce.ila.mastery.dto.RecordAnswerRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mastery")
public class MasteryController {

    private final MasteryService masteryService;

    public MasteryController(MasteryService masteryService) {
        this.masteryService = masteryService;
    }

    // Simulates recording one quiz answer and updating mastery accordingly
    @PostMapping("/record-answer")
    public ResponseEntity<MasteryResponse> recordAnswer(@Valid @RequestBody RecordAnswerRequest request) {
        StudentTopicMastery mastery = masteryService.recordAnswer(
                request.getStudentId(), request.getTopicId(),
                request.getWasCorrect(), request.getDifficultyLevel());
        return ResponseEntity.ok(toResponse(mastery));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MasteryResponse>> getAllMasteryForStudent(@PathVariable Long studentId) {
        List<MasteryResponse> result = masteryService.getAllMasteryForStudent(studentId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/student/{studentId}/weak-topics")
    public ResponseEntity<List<MasteryResponse>> getWeakTopics(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "40.0") double threshold) {
        List<MasteryResponse> result = masteryService.getWeakTopics(studentId, threshold)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(result);
    }

    private MasteryResponse toResponse(StudentTopicMastery mastery) {
        return new MasteryResponse(
                mastery.getId(), mastery.getStudent().getId(),
                mastery.getTopic().getId(), mastery.getTopic().getName(),
                mastery.getMasteryScore(), mastery.getTotalAttempts(),
                mastery.getCorrectAttempts(), mastery.getLastPracticedAt());
    }

    @GetMapping("/student/{studentId}/topic/{topicId}/forgetting-risk")
    public ResponseEntity<Double> getForgettingRisk(
            @PathVariable Long studentId, @PathVariable Long topicId) {
        return ResponseEntity.ok(masteryService.getForgettingRisk(studentId, topicId));
    }

    @GetMapping("/student/{studentId}/topic/{topicId}/history")
    public ResponseEntity<List<com.bruteforce.ila.mastery.dto.MasteryHistoryResponse>> getHistory(
            @PathVariable Long studentId, @PathVariable Long topicId) {
        // inject MasteryHistoryRepository via constructor alongside masteryService if not already available
        return ResponseEntity.ok(masteryService.getMasteryHistory(studentId, topicId));
    }
}