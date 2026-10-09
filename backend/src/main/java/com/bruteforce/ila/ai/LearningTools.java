package com.bruteforce.ila.ai;

import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.mastery.StudentTopicMastery;
import com.bruteforce.ila.priority.PriorityService;
import com.bruteforce.ila.rag.RagIngestionService;
import com.bruteforce.ila.revision.RevisionSchedulerService;
import com.bruteforce.ila.revision.dto.RevisionItemResponse;
import com.bruteforce.ila.studyplan.StudyPlanService;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Controlled tools the AI is allowed to call. Every method here delegates
 * to an already-built, already-tested service - no new business logic
 * exists in this class. This is the enforced boundary between AI
 * conversation and deterministic backend calculation.
 */
@Component
public class LearningTools {

    private final MasteryService masteryService;
    private final PriorityService priorityService;
    private final RevisionSchedulerService revisionSchedulerService;
    private final StudyPlanService studyPlanService;
    private final TopicRepository topicRepository;
    private final RagIngestionService ragIngestionService;

    public LearningTools(MasteryService masteryService,
                         PriorityService priorityService,
                         RevisionSchedulerService revisionSchedulerService,
                         StudyPlanService studyPlanService,
                         TopicRepository topicRepository,
                         RagIngestionService ragIngestionService) {
        this.masteryService = masteryService;
        this.priorityService = priorityService;
        this.revisionSchedulerService = revisionSchedulerService;
        this.studyPlanService = studyPlanService;
        this.topicRepository = topicRepository;
        this.ragIngestionService = ragIngestionService;
    }

    @Tool(description = "Get the student's current mastery percentage (0-100) for a specific topic by its exact name")
    public String getTopicMastery(
            @ToolParam(description = "The exact topic name, e.g. 'Normalization'") String topicName,
            @ToolParam(description = "The student's id") Long studentId) {

        Topic topic = findTopicByName(topicName);

        if (topic == null) {
            return "No topic found with the name '" + topicName + "'.";
        }

        StudentTopicMastery mastery =
                masteryService.getOrCreateMastery(
                        studentId,
                        topic.getId()
                );

        return topicName
                + ": mastery is "
                + Math.round(mastery.getMasteryScore())
                + "%.";
    }

    @Tool(description = "Get the list of topics the student is currently weak in (mastery below 50%)")
    public String getWeakTopics(
            @ToolParam(description = "The student's id") Long studentId) {

        List<StudentTopicMastery> weak =
                masteryService.getWeakTopics(studentId, 50.0);

        if (weak.isEmpty()) {
            return "The student has no weak topics right now.";
        }

        StringBuilder sb = new StringBuilder("Weak topics: ");

        for (StudentTopicMastery m : weak) {
            sb.append(m.getTopic().getName())
                    .append(" (")
                    .append(Math.round(m.getMasteryScore()))
                    .append("%), ");
        }

        return sb.toString();
    }

    @Tool(description = "Get the single highest-priority topic the student should study next, with the reason why")
    public String getNextRecommendedTopic(
            @ToolParam(description = "The student's id") Long studentId,
            @ToolParam(description = "The subject's id") Long subjectId) {

        List<Topic> allTopics = topicRepository.findAll();

        Topic top = null;
        PriorityService.PriorityBreakdown topBreakdown = null;

        for (Topic topic : allTopics) {

            PriorityService.PriorityBreakdown breakdown =
                    priorityService.calculatePriorityForTopic(
                            studentId,
                            topic.getId()
                    );

            if (topBreakdown == null
                    || breakdown.finalScore > topBreakdown.finalScore) {

                top = topic;
                topBreakdown = breakdown;
            }
        }

        if (top == null) {
            return "No topics found to recommend.";
        }

        return String.format(
                "Recommended topic: %s (priority score %.0f). Reasons - weakness: %.0f, exam importance: %.0f, "
                        + "prerequisite impact: %.0f, forgetting risk: %.0f, difficulty: %.0f.",
                top.getName(),
                topBreakdown.finalScore,
                topBreakdown.weaknessScore,
                topBreakdown.examImportance,
                topBreakdown.prerequisiteImpact,
                topBreakdown.forgettingRisk,
                topBreakdown.difficultyScore
        );
    }

    @Tool(description = "Get the topics currently due for revision because they may be forgotten soon")
    public String getTopicsDueForRevision(
            @ToolParam(description = "The student's id") Long studentId) {

        List<RevisionItemResponse> due =
                revisionSchedulerService.getTopicsDueForRevision(
                        studentId,
                        null
                );

        if (due.isEmpty()) {
            return "Nothing is due for revision right now.";
        }

        StringBuilder sb = new StringBuilder("Due for revision: ");

        for (RevisionItemResponse item : due) {
            sb.append(item.getTopicName())
                    .append(", ");
        }

        return sb.toString();
    }

    @Tool(description = "Get a summary of the student's current active study plan, including allocated hours per topic")
    public String getCurrentStudyPlan(
            @ToolParam(description = "The student's id") Long studentId) {

        try {

            var plan = studyPlanService.getActivePlan(
                    studentId,
                    null
            );

            var items =
                    studyPlanService.getItemsForPlan(
                            plan.getId()
                    );

            StringBuilder sb =
                    new StringBuilder(
                            "Current study plan ("
                                    + plan.getAvailableHours()
                                    + " hours total): "
                    );

            for (var item : items) {
                sb.append(item.getTopic().getName())
                        .append(" - ")
                        .append(item.getAllocatedHours())
                        .append("h, ");
            }

            return sb.toString();

        } catch (Exception e) {
            return "The student does not have an active study plan yet.";
        }
    }

    @Tool(description = "Search the student's uploaded study material for content relevant to a topic or question, to ground an explanation in their actual notes")
    public String searchStudyMaterial(
            @ToolParam(description = "The topic name to search within, e.g. 'Normalization'") String topicName,
            @ToolParam(description = "The student's question or what they want explained") String query) {

        Topic topic = findTopicByName(topicName);

        if (topic == null) {
            return "No topic found with the name '" + topicName + "'.";
        }

        List<String> chunks =
                ragIngestionService.searchRelevantChunks(
                        query,
                        topic.getId(),
                        3
                );

        if (chunks.isEmpty()) {
            return "No uploaded study material found for this topic yet.";
        }

        return "Relevant notes: "
                + String.join(" ", chunks);
    }

    private Topic findTopicByName(String name) {

        return topicRepository.findAll()
                .stream()
                .filter(t ->
                        t.getName()
                                .equalsIgnoreCase(name.trim())
                )
                .max(Comparator.comparing(Topic::getId))
                .orElse(null);
    }
}