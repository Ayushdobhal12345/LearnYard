package com.bruteforce.ila.insights;

import com.bruteforce.ila.dependency.TopicDependency;
import com.bruteforce.ila.dependency.TopicDependencyRepository;
import com.bruteforce.ila.mastery.ForgettingRiskCalculator;
import com.bruteforce.ila.mastery.MasteryHistory;
import com.bruteforce.ila.mastery.MasteryHistoryRepository;
import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.mastery.StudentTopicMastery;
import com.bruteforce.ila.material.StudyMaterialRepository;
import com.bruteforce.ila.pyq.PYQAnalysisService;
import com.bruteforce.ila.syllabus.SyllabusUnit;
import com.bruteforce.ila.syllabus.SyllabusUnitRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Readiness score and root-cause finder, built only from data the app already stores. */
@Service
public class InsightsService {

    private final SyllabusUnitRepository unitRepository;
    private final TopicRepository topicRepository;
    private final TopicDependencyRepository dependencyRepository;
    private final MasteryService masteryService;
    private final MasteryHistoryRepository historyRepository;
    private final PYQAnalysisService pyqAnalysisService;
    private final StudyMaterialRepository studyMaterialRepository;

    public InsightsService(SyllabusUnitRepository unitRepository, TopicRepository topicRepository,
                           TopicDependencyRepository dependencyRepository, MasteryService masteryService,
                           MasteryHistoryRepository historyRepository, PYQAnalysisService pyqAnalysisService,
                           StudyMaterialRepository studyMaterialRepository) {
        this.unitRepository = unitRepository;
        this.topicRepository = topicRepository;
        this.dependencyRepository = dependencyRepository;
        this.masteryService = masteryService;
        this.historyRepository = historyRepository;
        this.pyqAnalysisService = pyqAnalysisService;
        this.studyMaterialRepository = studyMaterialRepository;
    }

    /** How much the student has set up for a subject, so the app can guide them instead of showing errors. */
    @Transactional(readOnly = true)
    public Map<String, Object> setupStatus(Long subjectId) {
        int units = 0;
        int topics = 0;
        int materials = 0;

        for (SyllabusUnit unit : unitRepository.findBySubjectIdOrderByUnitOrderAsc(subjectId)) {
            units++;
            materials += studyMaterialRepository.findByUnitId(unit.getId()).size();
            for (Topic topic : topicRepository.findByUnitId(unit.getId())) {
                topics++;
                materials += studyMaterialRepository.findByTopicId(topic.getId()).size();
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("unitCount", units);
        out.put("topicCount", topics);
        out.put("materialCount", materials);
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> readiness(Long studentId, Long subjectId, Integer daysLeft) {
        List<InsightCalculator.TopicState> states = loadStates(studentId, subjectId);
        InsightCalculator.Readiness r = InsightCalculator.readiness(states);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalTopics", r.totalTopics());
        out.put("practicedTopics", r.practicedTopics());
        out.put("readiness", round1(r.readiness()));
        out.put("label", InsightCalculator.label(r.readiness()));
        out.put("weeklyChange", round1(r.weeklyChange()));

        if (daysLeft != null && daysLeft > 0 && r.practicedTopics() > 0) {
            out.put("daysLeft", daysLeft);
            out.put("projected", round1(InsightCalculator.project(r.readiness(), r.weeklyChange(), daysLeft)));
        }

        List<Map<String, Object>> wins = new ArrayList<>();
        for (InsightCalculator.Win w : r.biggestWins()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("topicId", w.id());
            row.put("topicName", w.name());
            row.put("practiced", w.practiced());
            row.put("mastery", Math.round(w.mastery()));
            row.put("gain", round1(w.gain()));
            wins.add(row);
        }
        out.put("biggestWins", wins);
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> rootCauses(Long studentId, Long subjectId) {
        List<InsightCalculator.TopicState> states = loadStates(studentId, subjectId);

        Map<Long, List<Long>> prerequisites = new HashMap<>();
        for (TopicDependency d : dependencyRepository.findBySubjectId(subjectId)) {
            prerequisites.computeIfAbsent(d.getTopic().getId(), k -> new ArrayList<>())
                    .add(d.getPrerequisiteTopic().getId());
        }

        InsightCalculator.RootCauseReport report = InsightCalculator.rootCauses(states, prerequisites);

        List<Map<String, Object>> startHere = new ArrayList<>();
        for (InsightCalculator.StartHere s : report.startHere()) {
            Map<String, Object> row = gapRow(s.topic());
            row.put("blocks", s.blocks());
            startHere.add(row);
        }

        List<Map<String, Object>> findings = new ArrayList<>();
        for (InsightCalculator.Finding f : report.findings()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("topicId", f.id());
            row.put("topicName", f.name());
            row.put("mastery", Math.round(f.mastery()));
            row.put("verdict", f.verdict());
            List<Map<String, Object>> roots = new ArrayList<>();
            for (InsightCalculator.Gap g : f.rootCauses()) {
                roots.add(gapRow(g));
            }
            row.put("rootCauses", roots);
            findings.add(row);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("startHere", startHere);
        out.put("findings", findings);
        return out;
    }

    private Map<String, Object> gapRow(InsightCalculator.Gap g) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("topicId", g.id());
        row.put("topicName", g.name());
        row.put("practiced", g.practiced());
        row.put("mastery", Math.round(g.mastery()));
        return row;
    }

    private List<InsightCalculator.TopicState> loadStates(Long studentId, Long subjectId) {
        List<Topic> topics = new ArrayList<>();
        for (SyllabusUnit unit : unitRepository.findBySubjectIdOrderByUnitOrderAsc(subjectId)) {
            topics.addAll(topicRepository.findByUnitId(unit.getId()));
        }

        Map<Long, StudentTopicMastery> masteryByTopic = new HashMap<>();
        for (StudentTopicMastery m : masteryService.getAllMasteryForStudent(studentId)) {
            masteryByTopic.put(m.getTopic().getId(), m);
        }

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<InsightCalculator.TopicState> states = new ArrayList<>();

        for (Topic topic : topics) {
            StudentTopicMastery m = masteryByTopic.get(topic.getId());
            boolean practiced = m != null && m.getTotalAttempts() != null && m.getTotalAttempts() > 0;

            double mastery = practiced ? m.getMasteryScore() : 0.0;
            double risk = practiced
                    ? ForgettingRiskCalculator.calculateForgettingRisk(m.getMasteryScore(), m.getLastPracticedAt())
                    : 0.0;
            double importance = pyqAnalysisService.calculateExamImportance(topic.getId(), subjectId);

            double before = 0.0;
            if (practiced) {
                for (MasteryHistory h : historyRepository
                        .findByStudentIdAndTopicIdOrderByRecordedAtAsc(studentId, topic.getId())) {
                    if (h.getRecordedAt() != null && !h.getRecordedAt().isAfter(weekAgo)) {
                        before = h.getMasteryScore();
                    }
                }
            }

            states.add(new InsightCalculator.TopicState(
                    topic.getId(), topic.getName(), practiced, mastery, risk, importance, before));
        }
        return states;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
