package com.bruteforce.ila.quiz;

import com.bruteforce.ila.quiz.dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;
    private final com.bruteforce.ila.ai.QuizGenerationService quizGenerationService;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public QuizController(QuizService quizService,
                          com.bruteforce.ila.ai.QuizGenerationService quizGenerationService,
                          com.bruteforce.ila.security.AccessGuard accessGuard) {
        this.quizService = quizService;
        this.quizGenerationService = quizGenerationService;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/questions")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        Question question = quizService.createQuestion(
                request.getQuestionText(), request.getOptionA(), request.getOptionB(),
                request.getOptionC(), request.getOptionD(), request.getCorrectOption(),
                request.getDifficultyLevel(), request.getTopicId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toQuestionResponse(question));
    }

    @GetMapping("/questions/by-topic/{topicId}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByTopic(@PathVariable Long topicId) {
        accessGuard.requireTopicOwner(topicId);
        List<QuestionResponse> result = quizService.getQuestionsByTopic(topicId)
                .stream().map(this::toQuestionResponse).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/submit")
    public ResponseEntity<java.util.Map<String, Object>> submitQuiz(@Valid @RequestBody SubmitQuizRequest request) {
        QuizAttempt attempt = quizService.submitQuiz(request.getStudentId(), request.getAnswers());
        double scorePercentage = (attempt.getCorrectAnswers() * 100.0) / attempt.getTotalQuestions();
        QuizResultResponse quizResult = new QuizResultResponse(
                attempt.getId(), attempt.getTotalQuestions(), attempt.getCorrectAnswers(),
                scorePercentage, attempt.getSubmittedAt());

        // Determine the subject being tested from the first answered question, then replan
        Long firstQuestionId = request.getAnswers().get(0).getQuestionId();
        Long topicId = quizService.getTopicIdForQuestion(firstQuestionId);
        Long subjectId = quizService.getSubjectIdForTopic(topicId);
        var replanResult = quizService.replanIfNeeded(request.getStudentId(), subjectId);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("quizResult", quizResult);
        response.put("replan", replanResult);
        return ResponseEntity.ok(response);
    }

    private QuestionResponse toQuestionResponse(Question q) {
        return new QuestionResponse(
                q.getId(), q.getQuestionText(), q.getOptionA(), q.getOptionB(),
                q.getOptionC(), q.getOptionD(), q.getDifficultyLevel(),
                q.getTopic().getId(), q.getTopic().getName());
    }

    @PostMapping("/generate")
    public ResponseEntity<List<QuestionResponse>> generateQuestions(
            @RequestParam Long topicId, @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "false") boolean refresh) {
        accessGuard.requireTopicOwner(topicId);

        // Questions already exist for this topic: reuse them instead of calling Gemini again.
        List<Question> existing = quizService.getQuestionsByTopic(topicId);
        if (!refresh && existing.size() >= count) {
            return ResponseEntity.ok(existing.stream().map(this::toQuestionResponse).toList());
        }

        List<Question> questions = quizGenerationService.generateQuestions(topicId, count);
        List<QuestionResponse> result = questions.stream().map(this::toQuestionResponse).toList();
        return ResponseEntity.ok(result);
    }
}