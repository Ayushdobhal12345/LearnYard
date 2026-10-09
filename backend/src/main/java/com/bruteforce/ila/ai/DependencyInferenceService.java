package com.bruteforce.ila.ai;

import com.bruteforce.ila.dependency.TopicDependencyService;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import com.bruteforce.ila.syllabus.SyllabusUnitRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class DependencyInferenceService {

    private final ChatClient chatClient;
    private final TopicRepository topicRepository;
    private final SyllabusUnitRepository unitRepository;
    private final TopicDependencyService dependencyService;

    public DependencyInferenceService(ChatClient.Builder chatClientBuilder, TopicRepository topicRepository,
                                      SyllabusUnitRepository unitRepository, TopicDependencyService dependencyService) {
        this.chatClient = chatClientBuilder.build();
        this.topicRepository = topicRepository;
        this.unitRepository = unitRepository;
        this.dependencyService = dependencyService;
    }

    public int inferDependenciesForSubject(Long subjectId) {
        List<Topic> topics = unitRepository.findBySubjectIdOrderByUnitOrderAsc(subjectId).stream()
                .flatMap(unit -> topicRepository.findByUnitId(unit.getId()).stream())
                .toList();

        if (topics.size() < 2) return 0;

        String topicList = topics.stream()
                .map(t -> "\"" + t.getName() + "\"")
                .reduce((a, b) -> a + ", " + b).orElse("");

        String prompt = """
                Given this list of academic topics from one subject's syllabus: [%s]

                Identify genuine prerequisite relationships - which topics MUST be
                understood before another topic makes sense. Only include clear,
                well-established prerequisites, not loose thematic connections.
                Do not create cycles. Keep the list focused (usually 3-10 relationships
                for a syllabus this size, fewer if topics are mostly independent).

                Respond with ONLY a raw JSON array, no markdown, no explanation:
                [{"topic":"exact name","requires":"exact name of prerequisite"}]
                """.formatted(topicList);

        String raw = chatClient.prompt(prompt).call().content();
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1) return 0;

        int created = 0;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode array = mapper.readTree(raw.substring(start, end + 1));
            for (JsonNode item : array) {
                Topic topic = findByName(topics, item.get("topic").asText());
                Topic prereq = findByName(topics, item.get("requires").asText());
                if (topic == null || prereq == null || topic.getId().equals(prereq.getId())) continue;

                try {
                    dependencyService.createDependency(topic.getId(), prereq.getId());
                    created++;
                } catch (Exception ignoredDuplicateOrInvalid) {
                    // skip - already exists or self-reference caught by service validation
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not parse AI-inferred prerequisites. Try again.", e);
        }
        return created;
    }

    private Topic findByName(List<Topic> topics, String name) {
        return topics.stream()
                .filter(t -> t.getName().equalsIgnoreCase(name.trim()))
                .max(Comparator.comparing(Topic::getId))
                .orElse(null);
    }
}