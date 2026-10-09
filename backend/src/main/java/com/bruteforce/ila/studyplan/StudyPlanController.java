package com.bruteforce.ila.studyplan;

import com.bruteforce.ila.studyplan.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/study-plans")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    @PostMapping("/generate")
    public ResponseEntity<StudyPlanResponse> generatePlan(@Valid @RequestBody GeneratePlanRequest request) {
        StudyPlan plan = studyPlanService.generatePlan(
                request.getStudentId(), request.getSubjectId(),
                request.getAvailableHours(), request.getHoursUntilExam());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(plan));
    }

    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<StudyPlanResponse> getActivePlan(@PathVariable Long studentId, @RequestParam Long subjectId) {
        StudyPlan plan = studyPlanService.getActivePlan(studentId, subjectId);
        return ResponseEntity.ok(toResponse(plan));
    }

    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<List<StudyPlanResponse>> getPlanHistory(@PathVariable Long studentId, @RequestParam Long subjectId) {
        List<StudyPlanResponse> history = studyPlanService.getPlanHistory(studentId, subjectId)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(history);
    }

    private StudyPlanResponse toResponse(StudyPlan plan) {
        List<StudyPlanItemResponse> itemResponses = studyPlanService.getItemsForPlan(plan.getId())
                .stream()
                .map(item -> new StudyPlanItemResponse(
                        item.getTopic().getId(), item.getTopic().getName(),
                        item.getPriorityScoreSnapshot(), item.getAllocatedHours(), item.getItemOrder()))
                .toList();

        return new StudyPlanResponse(
                plan.getId(), plan.getStudent().getId(), plan.getAvailableHours(),
                plan.getHoursUntilExam(), plan.getGeneratedAt(), plan.getIsActive(), itemResponses);
    }
}