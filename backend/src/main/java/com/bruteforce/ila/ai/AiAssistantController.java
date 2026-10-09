package com.bruteforce.ila.ai;

import com.bruteforce.ila.ai.dto.ChatRequest;
import com.bruteforce.ila.ai.dto.ChatResponse;
import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.material.StudyMaterialRepository;
import com.bruteforce.ila.mastery.StudentTopicMastery;
import com.bruteforce.ila.priority.PriorityService;
import com.bruteforce.ila.rag.RagIngestionService;
import com.bruteforce.ila.revision.RevisionSchedulerService;
import com.bruteforce.ila.revision.dto.RevisionItemResponse;
import com.bruteforce.ila.student.StudentRepository;
import com.bruteforce.ila.studyplan.StudyPlanService;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private static final List<String> EXPLAIN_KEYWORDS = List.of(
            "explain",
            "notes",
            "understand",
            "describe",
            "what is",
            "how does",
            "summar",
            "meaning of"
    );

    private final ChatClient chatClient;
    private final MasteryService masteryService;
    private final RevisionSchedulerService revisionSchedulerService;
    private final StudyPlanService studyPlanService;
    private final TopicRepository topicRepository;
    private final RagIngestionService ragIngestionService;
    private final PriorityService priorityService;
    private final ChatMessageRepository chatMessageRepository;
    private final StudentRepository studentRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public AiAssistantController(
            ChatClient.Builder chatClientBuilder,
            MasteryService masteryService,
            RevisionSchedulerService revisionSchedulerService,
            StudyPlanService studyPlanService,
            TopicRepository topicRepository,
            RagIngestionService ragIngestionService,
            PriorityService priorityService,
            ChatMessageRepository chatMessageRepository,
            StudentRepository studentRepository,
            StudyMaterialRepository studyMaterialRepository,
            com.bruteforce.ila.security.AccessGuard accessGuard) {

        this.chatClient = chatClientBuilder.build();
        this.masteryService = masteryService;
        this.revisionSchedulerService = revisionSchedulerService;
        this.studyPlanService = studyPlanService;
        this.topicRepository = topicRepository;
        this.ragIngestionService = ragIngestionService;
        this.priorityService = priorityService;
        this.chatMessageRepository = chatMessageRepository;
        this.studentRepository = studentRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.accessGuard = accessGuard;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {

        Long studentId = request.getStudentId();
        String message = request.getMessage();

        accessGuard.requireSubjectOwner(request.getSubjectId());
        String context = buildContext(studentId, request.getSubjectId(), message);

        String fullPrompt = """
        You are a helpful study assistant for an adaptive learning platform.
        You can discuss anything the student asks - general academic questions,
        explanations of concepts, study tips, or casual conversation - using
        your own knowledge freely.

        However, for anything about THIS student specifically - their mastery,
        priority topics, study plan, or revision schedule - you must use ONLY
        the real data provided below and never invent numbers. Only reference
        the "Relevant notes" section when they specifically ask you to explain
        or summarize a topic from their notes.

        Keep answers concise and encouraging.

        --- REAL STUDENT DATA (use only for personal/app-specific questions) ---
        %s
        --- END OF DATA ---

        Student's question: %s
        """.formatted(context, message);

        String reply = chatClient.prompt(fullPrompt).call().content();

        var student = studentRepository.findById(studentId).orElseThrow();

        chatMessageRepository.save(
                new ChatMessage(student, "user", message)
        );

        chatMessageRepository.save(
                new ChatMessage(student, "assistant", reply)
        );

        return ResponseEntity.ok(new ChatResponse(reply));
    }

    private String buildContext(Long studentId, Long subjectId, String message) {

        StringBuilder sb = new StringBuilder();
        String lowerMessage = message.toLowerCase();

        // Only look at topics from the subject the student is currently studying.
        List<Topic> scopeTopics = topicsInScope(subjectId);
        java.util.Set<Long> scopeIds = scopeTopics.stream()
                .map(Topic::getId)
                .collect(java.util.stream.Collectors.toSet());

        List<StudentTopicMastery> weak =
                masteryService.getWeakTopics(studentId, 50.0)
                        .stream()
                        .filter(m -> scopeIds.contains(m.getTopic().getId()))
                        .toList();

        sb.append("Weak topics: ")
                .append(
                        weak.isEmpty()
                                ? "none"
                                : weak.stream()
                                .map(m -> m.getTopic().getName()
                                        + " ("
                                        + Math.round(m.getMasteryScore())
                                        + "%)")
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("none")
                )
                .append("\n");

        boolean wantsRecommendation =
                lowerMessage.contains("next")
                        || lowerMessage.contains("study")
                        || lowerMessage.contains("recommend")
                        || lowerMessage.contains("priority")
                        || lowerMessage.contains("plan");

        if (wantsRecommendation) {

            List<Topic> allTopics = scopeTopics;

            Topic topPriorityTopic = null;
            double topScore = -1;

            for (Topic t : allTopics) {

                var breakdown =
                        priorityService.calculatePriorityForTopic(
                                studentId,
                                t.getId()
                        );

                if (breakdown.finalScore > topScore) {
                    topScore = breakdown.finalScore;
                    topPriorityTopic = t;
                }
            }

            if (topPriorityTopic != null) {
                sb.append("Top recommended topic to study next: ")
                        .append(topPriorityTopic.getName())
                        .append(" (priority score ")
                        .append(Math.round(topScore))
                        .append(")\n");
            }
        }

        List<RevisionItemResponse> due =
                revisionSchedulerService.getTopicsDueForRevision(
                        studentId,
                        subjectId
                );

        sb.append("Due for revision: ")
                .append(
                        due.isEmpty()
                                ? "none"
                                : due.stream()
                                .map(RevisionItemResponse::getTopicName)
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("none")
                )
                .append("\n");

        try {

            var plan = studyPlanService.getActivePlan(
                    studentId,
                    subjectId
            );

            var items =
                    studyPlanService.getItemsForPlan(plan.getId());

            sb.append("Current study plan (")
                    .append(plan.getAvailableHours())
                    .append("h total): ");

            for (var item : items) {
                sb.append(item.getTopic().getName())
                        .append(" - ")
                        .append(item.getAllocatedHours())
                        .append("h, ");
            }

            sb.append("\n");

        } catch (Exception e) {
            sb.append("No active study plan.\n");
        }

        boolean wantsExplanation =
                EXPLAIN_KEYWORDS.stream()
                        .anyMatch(lowerMessage::contains);

        Topic mentionedTopic =
                findMentionedTopic(message, scopeTopics);

        if (mentionedTopic != null) {

            StudentTopicMastery mastery =
                    masteryService.getOrCreateMastery(
                            studentId,
                            mentionedTopic.getId()
                    );

            sb.append(mentionedTopic.getName())
                    .append(" mastery: ")
                    .append(Math.round(mastery.getMasteryScore()))
                    .append("%\n");

            if (wantsExplanation) {

                List<String> chunks =
                        ragIngestionService.searchRelevantChunks(
                                message,
                                mentionedTopic.getId(),
                                3
                        );

                if (!chunks.isEmpty()) {
                    sb.append("Relevant notes on ")
                            .append(mentionedTopic.getName())
                            .append(": ")
                            .append(String.join(" ", chunks))
                            .append("\n");
                }
            }
        }

        if (wantsExplanation && mentionedTopic == null) {

            Topic notesTopic = findTopicWithNotes(message, scopeTopics);

            if (notesTopic != null) {

                List<String> chunks =
                        ragIngestionService.searchRelevantChunks(
                                message,
                                notesTopic.getId(),
                                3
                        );

                if (!chunks.isEmpty()) {
                    sb.append("Relevant notes on ")
                            .append(notesTopic.getName())
                            .append(": ")
                            .append(String.join(" ", chunks))
                            .append("\n");
                }
            }
        }

        return sb.toString();
    }

    /** Topics of one subject, or every topic when no subject is given. */
    private List<Topic> topicsInScope(Long subjectId) {

        List<Topic> all = topicRepository.findAll();

        if (subjectId == null) {
            return all;
        }

        return all.stream()
                .filter(t -> t.getUnit() != null
                        && t.getUnit().getSubject() != null
                        && subjectId.equals(t.getUnit().getSubject().getId()))
                .toList();
    }

    /**
     * Used when the student asks about their notes without typing a full topic name.
     * Picks the uploaded-notes topic whose name shares the most words with the message,
     * or the most recently created topic that has notes if nothing matches.
     */
    private Topic findTopicWithNotes(String message, List<Topic> scopeTopics) {

        List<Topic> withNotes = scopeTopics
                .stream()
                .filter(t -> !studyMaterialRepository.findByTopicId(t.getId()).isEmpty())
                .toList();

        if (withNotes.isEmpty()) {
            return null;
        }

        java.util.Set<String> words = java.util.Arrays
                .stream(message.toLowerCase().split("[^a-z0-9]+"))
                .filter(w -> w.length() > 3)
                .collect(java.util.stream.Collectors.toSet());

        Topic best = null;
        int bestScore = 0;

        for (Topic t : withNotes) {
            int score = 0;
            for (String w : t.getName().toLowerCase().split("[^a-z0-9]+")) {
                if (w.length() > 3 && words.contains(w)) {
                    score++;
                }
            }
            if (score > bestScore) {
                best = t;
                bestScore = score;
            }
        }

        if (best == null) {
            best = withNotes.stream()
                    .max(Comparator.comparing(Topic::getId))
                    .orElse(null);
        }

        return best;
    }

    private Topic findMentionedTopic(String message, List<Topic> scopeTopics) {

        String lowerMessage = message.toLowerCase();

        return scopeTopics
                .stream()
                .filter(t ->
                        lowerMessage.contains(
                                t.getName().toLowerCase()
                        )
                )
                .max(Comparator.comparing(Topic::getId))
                .orElse(null);
    }

    @GetMapping("/chat/history/{studentId}")
    public ResponseEntity<List<java.util.Map<String, String>>> getHistory(
            @PathVariable Long studentId) {

        var history =
                chatMessageRepository
                        .findByStudentIdOrderByCreatedAtAsc(studentId)
                        .stream()
                        .map(m -> java.util.Map.of(
                                "role", m.getRole(),
                                "text", m.getContent(),
                                "createdAt", m.getCreatedAt().toString()
                        ))
                        .toList();

        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/chat/history/{studentId}")
    public ResponseEntity<Void> clearHistory(
            @PathVariable Long studentId) {

        chatMessageRepository.deleteByStudentId(studentId);

        return ResponseEntity.noContent().build();
    }
}