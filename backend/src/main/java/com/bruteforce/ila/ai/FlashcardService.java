package com.bruteforce.ila.ai;

import com.bruteforce.ila.rag.RagIngestionService;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class FlashcardService {

    private final ChatClient chatClient;
    private final TopicRepository topicRepository;
    private final RagIngestionService ragIngestionService;
    private final AiCacheService aiCacheService;

    public FlashcardService(ChatClient.Builder chatClientBuilder, TopicRepository topicRepository,
                            RagIngestionService ragIngestionService, AiCacheService aiCacheService) {
        this.chatClient = chatClientBuilder.build();
        this.topicRepository = topicRepository;
        this.ragIngestionService = ragIngestionService;
        this.aiCacheService = aiCacheService;
    }

    /** Saved flashcards for this topic and set of notes when available; refresh = true makes a new set. */
    public List<java.util.Map<String, String>> generateFlashcardsCached(Long topicId, int count, boolean refresh) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));
        String key = "flash:t" + topicId + ":n" + count + ":" + aiCacheService.notesStamp(topic);
        ObjectMapper mapper = new ObjectMapper();

        if (!refresh) {
            String saved = aiCacheService.get(key);
            if (saved != null) {
                try {
                    return mapper.readValue(saved, new TypeReference<List<java.util.Map<String, String>>>() { });
                } catch (Exception ignored) {
                    // unreadable saved copy: fall through and generate a new set
                }
            }
        }

        List<java.util.Map<String, String>> fresh = generateFlashcards(topicId, count);
        try {
            aiCacheService.put(key, mapper.writeValueAsString(fresh));
        } catch (Exception ignored) {
            // not saved, still returned
        }
        return fresh;
    }

    public List<java.util.Map<String, String>> generateFlashcards(Long topicId, int count) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));

        List<String> chunks = ragIngestionService.searchRelevantChunks(topic.getName(), topicId, 4);
        String notesContext = chunks.isEmpty() ? "" : "Base them on these notes: " + String.join(" ", chunks);

        String prompt = """
                Create exactly %d flashcards for the topic "%s". %s
                Respond with ONLY a raw JSON array, no markdown, no explanation:
                [{"front":"question or term","back":"concise answer"}]
                """.formatted(count, topic.getName(), notesContext);

        String raw = chatClient.prompt(prompt).call().content();
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1) throw new RuntimeException("AI did not return valid flashcards.");

        List<java.util.Map<String, String>> cards = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode array = mapper.readTree(raw.substring(start, end + 1));
            for (JsonNode item : array) {
                cards.add(java.util.Map.of("front", item.get("front").asText(), "back", item.get("back").asText()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not parse flashcards. Try again.", e);
        }
        return cards;
    }
}