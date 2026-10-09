package com.bruteforce.ila.ai;

import com.bruteforce.ila.rag.TextExtractor;
import com.bruteforce.ila.subject.Subject;
import com.bruteforce.ila.subject.SubjectService;
import com.bruteforce.ila.syllabus.SyllabusUnit;
import com.bruteforce.ila.syllabus.SyllabusUnitService;
import com.bruteforce.ila.syllabus.TopicService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SyllabusImportService {

    private final ChatClient chatClient;
    private final TextExtractor textExtractor;
    private final SubjectService subjectService;
    private final SyllabusUnitService unitService;
    private final TopicService topicService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public SyllabusImportService(ChatClient.Builder chatClientBuilder, TextExtractor textExtractor,
                                 SubjectService subjectService, SyllabusUnitService unitService,
                                 TopicService topicService) {
        this.chatClient = chatClientBuilder.build();
        this.textExtractor = textExtractor;
        this.subjectService = subjectService;
        this.unitService = unitService;
        this.topicService = topicService;
    }

    /** Whole-syllabus mode: AI groups content into units AND topics. */
    public int importSyllabus(MultipartFile file, Long subjectId) {
        Subject subject = subjectService.getSubjectById(subjectId);
        String trimmedText = extractTrimmed(file);

        String prompt = """
                You are analyzing a full course syllabus. Extract its structure as JSON
                only, no markdown, no explanation. Group related topics under logical
                units/chapters, using your own judgment if the source has no clear
                divisions. Exact format:
                [{"unitName":"Unit 1: ...","topics":[{"name":"...","difficultyLevel":2}]}]
                difficultyLevel must be exactly 1, 2, or 3.

                Syllabus text:
                %s
                """.formatted(trimmedText);

        String jsonArray = extractJsonArray(chatClient.prompt(prompt).call().content());
        int topicCount = 0;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode units = mapper.readTree(jsonArray);
            int order = 1;
            for (JsonNode unitNode : units) {
                SyllabusUnit unit = unitService.createUnit(unitNode.get("unitName").asText(), "", order++, subjectId);
                for (JsonNode topicNode : unitNode.get("topics")) {
                    int difficulty = clampDifficulty(topicNode);
                    topicService.createTopic(topicNode.get("name").asText(), "", difficulty, unit.getId());
                    topicCount++;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not understand the syllabus structure. Try a clearer file. (" + e.getMessage() + ")", e);
        }
        return topicCount;
    }

    /** Single-unit mode: content becomes topics inside ONE already-existing unit. */
    public int importTopicsIntoUnit(MultipartFile file, Long unitId) {
        String trimmedText = extractTrimmed(file);

        String prompt = """
                You are analyzing notes/content for ONE chapter of a course. Extract a
                list of distinct topics covered, as JSON only, no markdown, no explanation.
                Exact format:
                [{"name":"...","difficultyLevel":2}]
                difficultyLevel must be exactly 1, 2, or 3.

                Content:
                %s
                """.formatted(trimmedText);

        String jsonArray = extractJsonArray(chatClient.prompt(prompt).call().content());
        int topicCount = 0;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode topicsNode = mapper.readTree(jsonArray);
            for (JsonNode topicNode : topicsNode) {
                int difficulty = clampDifficulty(topicNode);
                topicService.createTopic(topicNode.get("name").asText(), "", difficulty, unitId);
                topicCount++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not understand the content structure. Try a clearer file. (" + e.getMessage() + ")", e);
        }
        return topicCount;
    }

    private int clampDifficulty(JsonNode topicNode) {
        int val = topicNode.has("difficultyLevel") ? topicNode.get("difficultyLevel").asInt() : 2;
        return Math.max(1, Math.min(3, val));
    }

    private String extractTrimmed(MultipartFile file) {
        String tempPath = saveTemp(file);
        String rawText = textExtractor.extractText(tempPath);
        return rawText.length() > 8000 ? rawText.substring(0, 8000) : rawText;
    }

    private String saveTemp(MultipartFile file) {
        try {
            Path uploadPath = Path.of(uploadDir);
            Files.createDirectories(uploadPath);
            String storedFileName = "syllabus-" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetPath = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    private String extractJsonArray(String raw) {
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start == -1 || end == -1 || end < start) {
            throw new RuntimeException("AI did not return a valid structure. Please try again.");
        }
        return raw.substring(start, end + 1);
    }
}