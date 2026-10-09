package com.bruteforce.ila.ai;

import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.rag.RagIngestionService;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class LessonService {

    private final ChatClient chatClient;
    private final TopicRepository topicRepository;
    private final MasteryService masteryService;
    private final RagIngestionService ragIngestionService;
    private final AiCacheService aiCacheService;

    public LessonService(ChatClient.Builder chatClientBuilder, TopicRepository topicRepository,
                         MasteryService masteryService, RagIngestionService ragIngestionService,
                         AiCacheService aiCacheService) {
        this.chatClient = chatClientBuilder.build();
        this.topicRepository = topicRepository;
        this.masteryService = masteryService;
        this.ragIngestionService = ragIngestionService;
        this.aiCacheService = aiCacheService;
    }

    /**
     * Returns the saved lesson when one exists for this topic, mastery level and set of notes;
     * otherwise asks Gemini and saves the result. refresh = true always asks Gemini again.
     */
    public AiCacheService.CachedText teachTopicCached(Long studentId, Long topicId, boolean refresh) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));

        double mastery = masteryService.getOrCreateMastery(studentId, topicId).getMasteryScore();
        String band = mastery >= 70 ? "adv" : mastery >= 40 ? "mid" : "beg";
        String key = "lesson:s" + studentId + ":t" + topicId + ":" + band + ":" + aiCacheService.notesStamp(topic);

        if (!refresh) {
            String saved = aiCacheService.get(key);
            if (saved != null) {
                return new AiCacheService.CachedText(saved, true);
            }
        }

        String fresh = teachTopic(studentId, topicId);
        aiCacheService.put(key, fresh);
        return new AiCacheService.CachedText(fresh, false);
    }

    public String teachTopic(Long studentId, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));

        double mastery = masteryService.getOrCreateMastery(studentId, topicId).getMasteryScore();
        String level = mastery >= 70 ? "advanced, assume strong prior understanding"
                : mastery >= 40 ? "intermediate, explain fundamentals but move at a normal pace"
                : "beginner, start from absolute basics, avoid jargon without explaining it";

        List<String> notesChunks = ragIngestionService.searchRelevantChunks(topic.getName(), topicId, 4);
        String notesContext = notesChunks.isEmpty() ? "" :
                "The student's own notes on this topic say:\n" + String.join(" ", notesChunks) + "\n\n";

        String prompt = """
                Teach the student the topic "%s" as a structured lesson. Their current
                mastery level is %.0f%%, so explain at a %s level.
                %s
                Structure your lesson with these exact section headers:
                ## Overview
                ## Key Concepts
                ## Example
                ## Common Mistakes
                ## Quick Recap

                Keep it focused and genuinely educational, not just a summary.
                """.formatted(topic.getName(), mastery, level, notesContext);

        return chatClient.prompt(prompt).call().content();
    }
}