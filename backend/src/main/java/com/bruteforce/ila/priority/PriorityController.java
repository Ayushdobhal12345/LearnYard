package com.bruteforce.ila.priority;

import com.bruteforce.ila.priority.dto.PriorityBreakdownResponse;
import com.bruteforce.ila.syllabus.SyllabusUnitRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/priority")
public class PriorityController {

    private final PriorityService priorityService;
    private final TopicRepository topicRepository;
    private final SyllabusUnitRepository unitRepository;

    public PriorityController(PriorityService priorityService, TopicRepository topicRepository,
                              SyllabusUnitRepository unitRepository) {
        this.priorityService = priorityService;
        this.topicRepository = topicRepository;
        this.unitRepository = unitRepository;
    }

    @GetMapping("/student/{studentId}/topic/{topicId}")
    public ResponseEntity<PriorityBreakdownResponse> getPriorityForTopic(
            @PathVariable Long studentId, @PathVariable Long topicId) {
        PriorityService.PriorityBreakdown breakdown =
                priorityService.calculatePriorityForTopic(studentId, topicId);
        return ResponseEntity.ok(toResponse(breakdown));
    }

    @GetMapping("/student/{studentId}/ranking")
    public ResponseEntity<List<PriorityBreakdownResponse>> getPriorityRanking(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long subjectId) {

        List<Topic> scopedTopics = (subjectId != null)
                ? unitRepository.findBySubjectIdOrderByUnitOrderAsc(subjectId).stream()
                .flatMap(unit -> topicRepository.findByUnitId(unit.getId()).stream())
                .toList()
                : topicRepository.findAll();

        List<PriorityBreakdownResponse> ranked = scopedTopics.stream()
                .map(topic -> toResponse(priorityService.calculatePriorityForTopic(studentId, topic.getId())))
                .sorted(Comparator.comparingDouble(PriorityBreakdownResponse::getFinalPriorityScore).reversed())
                .toList();

        return ResponseEntity.ok(ranked);
    }

    private PriorityBreakdownResponse toResponse(PriorityService.PriorityBreakdown b) {
        return new PriorityBreakdownResponse(
                b.topic.getId(), b.topic.getName(),
                b.weaknessScore, b.examImportance, b.prerequisiteImpact,
                b.forgettingRisk, b.difficultyScore, b.finalScore);
    }
}