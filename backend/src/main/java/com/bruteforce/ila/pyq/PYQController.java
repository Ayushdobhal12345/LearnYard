package com.bruteforce.ila.pyq;

import com.bruteforce.ila.pyq.dto.*;
import com.bruteforce.ila.subject.Subject;
import com.bruteforce.ila.subject.SubjectRepository;
import jakarta.validation.Valid;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pyq")
public class PYQController {

    private final PYQAnalysisService pyqAnalysisService;
    private final SubjectRepository subjectRepository;

    public PYQController(PYQAnalysisService pyqAnalysisService, SubjectRepository subjectRepository) {
        this.pyqAnalysisService = pyqAnalysisService;
        this.subjectRepository = subjectRepository;
    }

    @PostMapping("/papers")
    public ResponseEntity<String> createPaper(@Valid @RequestBody PYQPaperRequest request) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Subject with id " + request.getSubjectId() + " not found."));
        PYQPaper paper = pyqAnalysisService.createPaper(request.getYear(), subject);
        return ResponseEntity.status(HttpStatus.CREATED).body("Created PYQ paper with id " + paper.getId());
    }

    @PostMapping("/questions")
    public ResponseEntity<String> addQuestion(@Valid @RequestBody PYQQuestionRequest request) {
        PYQQuestion question = pyqAnalysisService.addQuestion(
                request.getQuestionText(), request.getMarks(), request.getPaperId(), request.getTopicId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Created PYQ question with id " + question.getId());
    }

    @GetMapping("/exam-importance/{topicId}")
    public ResponseEntity<ExamImportanceResponse> getExamImportance(
            @PathVariable Long topicId, @RequestParam Long subjectId) {
        long frequency = pyqAnalysisService.getFrequency(topicId);
        Integer totalMarks = pyqAnalysisService.getTotalMarks(topicId);
        double score = pyqAnalysisService.calculateExamImportance(topicId, subjectId);
        return ResponseEntity.ok(new ExamImportanceResponse(topicId, frequency, totalMarks, score));
    }

    @GetMapping("/papers/by-subject/{subjectId}")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getPapers(@PathVariable Long subjectId) {
        var papers = pyqAnalysisService.getPapersForSubject(subjectId).stream()
                .map(p -> java.util.Map.<String, Object>of("id", p.getId(), "year", p.getYear()))
                .toList();
        return ResponseEntity.ok(papers);
    }
}