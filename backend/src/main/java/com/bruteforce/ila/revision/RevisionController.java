package com.bruteforce.ila.revision;

import com.bruteforce.ila.revision.dto.RevisionItemResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/revision")
public class RevisionController {

    private final RevisionSchedulerService revisionSchedulerService;

    public RevisionController(RevisionSchedulerService revisionSchedulerService) {
        this.revisionSchedulerService = revisionSchedulerService;
    }

    @GetMapping("/student/{studentId}/due")
    public ResponseEntity<List<RevisionItemResponse>> getTopicsDueForRevision(
            @PathVariable Long studentId, @RequestParam(required = false) Long subjectId) {
        return ResponseEntity.ok(revisionSchedulerService.getTopicsDueForRevision(studentId, subjectId));
    }

    @PostMapping("/student/{studentId}/topic/{topicId}/mark-revised")
    public ResponseEntity<String> markAsRevised(@PathVariable Long studentId, @PathVariable Long topicId) {
        revisionSchedulerService.markAsRevised(studentId, topicId);
        return ResponseEntity.ok("Topic " + topicId + " marked as revised for student " + studentId);
    }
}