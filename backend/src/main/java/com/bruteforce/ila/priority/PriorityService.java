package com.bruteforce.ila.priority;

import com.bruteforce.ila.dependency.TopicDependencyService;
import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.mastery.StudentTopicMastery;
import com.bruteforce.ila.pyq.PYQAnalysisService;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class PriorityService {

    private final MasteryService masteryService;
    private final PYQAnalysisService pyqAnalysisService;
    private final TopicDependencyService dependencyService;
    private final TopicRepository topicRepository;

    public PriorityService(MasteryService masteryService,
                           PYQAnalysisService pyqAnalysisService,
                           TopicDependencyService dependencyService,
                           TopicRepository topicRepository) {
        this.masteryService = masteryService;
        this.pyqAnalysisService = pyqAnalysisService;
        this.dependencyService = dependencyService;
        this.topicRepository = topicRepository;
    }

    public PriorityBreakdown calculatePriorityForTopic(Long studentId, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));

        Long subjectId = topic.getUnit().getSubject().getId();

        // 1. Weakness Score - the inverse of mastery. Low mastery = high weakness.
        StudentTopicMastery mastery = masteryService.getOrCreateMastery(studentId, topicId);
        double weaknessScore = 100.0 - mastery.getMasteryScore();

        // 2. Exam Importance - from PYQ frequency analysis, already 0-100 scaled
        double examImportance = pyqAnalysisService.calculateExamImportance(topicId, subjectId);

        // 3. Prerequisite Impact - how many topics depend on this one, scaled to 0-100.
        // Each dependent topic contributes 25 points, capped at 100
        // (so a topic blocking 4+ others maxes out the score).
        int dependentCount = dependencyService.getTopicsThatDependOn(topicId).size();
        double prerequisiteImpact = Math.min(100.0, dependentCount * 25.0);

        // 4. Forgetting Risk - already 0-100 scaled
        double forgettingRisk = masteryService.getForgettingRisk(studentId, topicId);

        // 5. Difficulty - topic's difficultyLevel (1-3) scaled to 0-100
        int difficultyLevel = topic.getDifficultyLevel() != null ? topic.getDifficultyLevel() : 1;
        double difficultyScore = (difficultyLevel / 3.0) * 100.0;

        double finalScore = PriorityCalculator.calculatePriority(
                weaknessScore, examImportance, prerequisiteImpact, forgettingRisk, difficultyScore);

        return new PriorityBreakdown(topic, weaknessScore, examImportance,
                prerequisiteImpact, forgettingRisk, difficultyScore, finalScore);
    }

    // A simple internal carrier class - not a DTO itself, kept inside the service
    // since it needs the full Topic entity, converted to a DTO in the controller
    public static class PriorityBreakdown {
        public final Topic topic;
        public final double weaknessScore;
        public final double examImportance;
        public final double prerequisiteImpact;
        public final double forgettingRisk;
        public final double difficultyScore;
        public final double finalScore;

        public PriorityBreakdown(Topic topic, double weaknessScore, double examImportance,
                                 double prerequisiteImpact, double forgettingRisk,
                                 double difficultyScore, double finalScore) {
            this.topic = topic;
            this.weaknessScore = weaknessScore;
            this.examImportance = examImportance;
            this.prerequisiteImpact = prerequisiteImpact;
            this.forgettingRisk = forgettingRisk;
            this.difficultyScore = difficultyScore;
            this.finalScore = finalScore;
        }
    }
}