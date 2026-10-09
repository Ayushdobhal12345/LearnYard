package com.bruteforce.ila.ai;

import com.bruteforce.ila.quiz.Question;
import com.bruteforce.ila.quiz.QuizService;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QuizGenerationService {

    private final ChatClient chatClient;
    private final QuizService quizService;
    private final TopicRepository topicRepository;

    public QuizGenerationService(ChatClient.Builder chatClientBuilder, QuizService quizService,
                                 TopicRepository topicRepository) {
        this.chatClient = chatClientBuilder.build();
        this.quizService = quizService;
        this.topicRepository = topicRepository;
    }

    public List<Question> generateQuestions(Long topicId, int count) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));

        String prompt = """
                Generate exactly %d multiple-choice questions about "%s" (%s).
                Respond with ONLY a raw JSON array, no markdown code fences, no preamble,
                no explanation before or after. Exact format:
                [{"question":"...","optionA":"...","optionB":"...","optionC":"...","optionD":"...","correctOption":"A","difficultyLevel":2}]
                correctOption must be exactly one uppercase letter: A, B, C, or D.
                difficultyLevel must be exactly 1, 2, or 3.
                """.formatted(count, topic.getName(),
                topic.getDescription() != null ? topic.getDescription() : "");

        String raw = chatClient.prompt(prompt).call().content();
        String jsonArray = extractJsonArray(raw);

        // Skip a question the topic already has, so repeated generation never stores duplicates.
        java.util.Set<String> knownQuestions = new java.util.HashSet<>();
        for (Question existing : quizService.getQuestionsByTopic(topicId)) {
            knownQuestions.add(existing.getQuestionText().trim().toLowerCase());
        }

        List<Question> created = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode array = mapper.readTree(jsonArray);
            for (JsonNode item : array) {
                if (!knownQuestions.add(item.get("question").asText().trim().toLowerCase())) {
                    continue;
                }
                String correctOption = sanitizeOption(item.get("correctOption").asText());
                int difficulty = clampDifficulty(item.has("difficultyLevel") ? item.get("difficultyLevel").asInt() : 2);

                Question q = quizService.createQuestion(
                        item.get("question").asText(),
                        item.get("optionA").asText(),
                        item.get("optionB").asText(),
                        item.get("optionC").asText(),
                        item.get("optionD").asText(),
                        correctOption,
                        difficulty,
                        topicId);
                created.add(q);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "AI returned an unexpected format while generating questions. Please try again. (" + e.getMessage() + ")", e);
        }

        if (created.isEmpty()) {
            throw new RuntimeException("AI did not generate any valid questions. Please try again.");
        }
        return created;
    }

    private String extractJsonArray(String raw) {
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1 || end < start) {
            throw new RuntimeException("AI did not return a valid question list. Please try again.");
        }
        return raw.substring(start, end + 1);
    }

    private String sanitizeOption(String raw) {
        String trimmed = raw.trim().toUpperCase();
        if (trimmed.isEmpty()) {
            throw new RuntimeException("AI returned an empty correct answer.");
        }
        char first = trimmed.charAt(0);
        if (first != 'A' && first != 'B' && first != 'C' && first != 'D') {
            throw new RuntimeException("AI returned an invalid correct answer letter: " + raw);
        }
        return String.valueOf(first);
    }

    private int clampDifficulty(int value) {
        return Math.max(1, Math.min(3, value));
    }
}