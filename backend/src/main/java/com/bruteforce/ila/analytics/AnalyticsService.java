package com.bruteforce.ila.analytics;

import com.bruteforce.ila.analytics.dto.DashboardSummaryResponse;
import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.mastery.StudentTopicMastery;
import com.bruteforce.ila.quiz.QuizAttemptRepository;
import com.bruteforce.ila.revision.RevisionSchedulerService;
import com.bruteforce.ila.studyplan.StudyPlanRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private static final double WEAKNESS_THRESHOLD = 50.0;

    private final MasteryService masteryService;
    private final RevisionSchedulerService revisionSchedulerService;
    private final StudyPlanRepository studyPlanRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final com.bruteforce.ila.session.StudySessionRepository studySessionRepository;

    public AnalyticsService(MasteryService masteryService,
                            RevisionSchedulerService revisionSchedulerService,
                            StudyPlanRepository studyPlanRepository,
                            QuizAttemptRepository quizAttemptRepository,
                            com.bruteforce.ila.session.StudySessionRepository studySessionRepository) {
        this.masteryService = masteryService;
        this.revisionSchedulerService = revisionSchedulerService;
        this.studyPlanRepository = studyPlanRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.studySessionRepository = studySessionRepository;
    }

    public DashboardSummaryResponse getDashboardSummary(Long studentId) {
        List<StudentTopicMastery> allMastery =
                masteryService.getAllMasteryForStudent(studentId);

        double overallMastery = allMastery.stream()
                .mapToDouble(StudentTopicMastery::getMasteryScore)
                .average()
                .orElse(0.0);

        List<StudentTopicMastery> weakTopics =
                masteryService.getWeakTopics(studentId, WEAKNESS_THRESHOLD);

        List<String> weakTopicNames = weakTopics.stream()
                .map(m -> m.getTopic().getName())
                .toList();

        int dueForRevisionCount = revisionSchedulerService
                .getTopicsDueForRevision(studentId, null)
                .size();

        boolean hasActivePlan = studyPlanRepository
                .existsByStudentIdAndIsActiveTrue(studentId);

        int totalQuizAttempts = quizAttemptRepository
                .findByStudentId(studentId)
                .size();

        int streakDays = computeStreak(studentId);

        return new DashboardSummaryResponse(
                Math.round(overallMastery * 10.0) / 10.0,
                allMastery.size(),
                weakTopics.size(),
                weakTopicNames,
                dueForRevisionCount,
                hasActivePlan,
                totalQuizAttempts,
                streakDays);
    }

    private int computeStreak(Long studentId) {
        java.util.Set<java.time.LocalDate> activeDates =
                new java.util.HashSet<>();

        for (var attempt : quizAttemptRepository.findByStudentId(studentId)) {
            if (attempt.getSubmittedAt() != null) {
                activeDates.add(
                        attempt.getSubmittedAt().toLocalDate()
                );
            }
        }

        for (var session :
                studySessionRepository
                        .findByStudentIdOrderBySessionDateDesc(studentId)) {

            if (session.getSessionDate() != null) {
                activeDates.add(
                        session.getSessionDate().toLocalDate()
                );
            }
        }

        int streak = 0;
        java.time.LocalDate cursor = java.time.LocalDate.now();

        if (!activeDates.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }

        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }
}