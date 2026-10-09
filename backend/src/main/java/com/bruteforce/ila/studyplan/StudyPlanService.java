package com.bruteforce.ila.studyplan;

import com.bruteforce.ila.priority.PriorityService;
import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.student.StudentRepository;
import com.bruteforce.ila.subject.Subject;
import com.bruteforce.ila.syllabus.SyllabusUnitRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyPlanService {

    private final StudyPlanRepository studyPlanRepository;
    private final StudyPlanItemRepository studyPlanItemRepository;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;
    private final SyllabusUnitRepository unitRepository;
    private final PriorityService priorityService;

    public StudyPlanService(StudyPlanRepository studyPlanRepository,
                            StudyPlanItemRepository studyPlanItemRepository,
                            StudentRepository studentRepository,
                            TopicRepository topicRepository,
                            SyllabusUnitRepository unitRepository,
                            PriorityService priorityService) {
        this.studyPlanRepository = studyPlanRepository;
        this.studyPlanItemRepository = studyPlanItemRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
        this.unitRepository = unitRepository;
        this.priorityService = priorityService;
    }

    /**
     * Generates a brand new study plan for a student, covering every topic in
     * a subject. Deactivates any previous plan first, calculates priority for
     * every topic, then allocates hours proportionally to those priorities.
     */
    @Transactional
    public StudyPlan generatePlan(Long studentId, Long subjectId, Double availableHours, Double hoursUntilExam) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student with id " + studentId + " not found."));

        Subject subjectEntity = null;
        // Deactivate any existing active plan for this student, in this subject
        studyPlanRepository.findByStudentIdAndSubjectIdAndIsActiveTrue(studentId, subjectId)
                .ifPresent(oldPlan -> {
                    oldPlan.setIsActive(false);
                    studyPlanRepository.save(oldPlan);
                });

        // Gather every topic belonging to this subject (across all its units)
        List<Topic> allTopicsInSubject = unitRepository.findBySubjectIdOrderByUnitOrderAsc(subjectId)
                .stream()
                .flatMap(unit -> topicRepository.findByUnitId(unit.getId()).stream())
                .toList();

        if (allTopicsInSubject.isEmpty()) {
            throw new IllegalArgumentException("Subject with id " + subjectId + " has no topics yet.");
        }

        // Calculate priority for every topic
        Map<Long, Double> topicIdToPriority = new HashMap<>();
        Map<Long, PriorityService.PriorityBreakdown> breakdownsByTopicId = new HashMap<>();
        for (Topic topic : allTopicsInSubject) {
            PriorityService.PriorityBreakdown breakdown =
                    priorityService.calculatePriorityForTopic(studentId, topic.getId());
            topicIdToPriority.put(topic.getId(), breakdown.finalScore);
            breakdownsByTopicId.put(topic.getId(), breakdown);
        }

        // Allocate hours proportionally to priority
        Map<Long, Double> allocation = StudyTimeAllocator.allocate(topicIdToPriority, availableHours);

        // Create the new plan
        StudyPlan newPlan = new StudyPlan(student, availableHours, hoursUntilExam);
        newPlan.setSubject(topicRepository.findById(allTopicsInSubject.get(0).getId()).get().getUnit().getSubject());
        newPlan = studyPlanRepository.save(newPlan);
        // Sort topics by priority descending, so item order reflects "study this first"
        List<Topic> sortedTopics = allTopicsInSubject.stream()
                .sorted(Comparator.comparingDouble(
                        (Topic t) -> topicIdToPriority.get(t.getId())).reversed())
                .toList();

        List<StudyPlanItem> items = new ArrayList<>();
        int order = 1;
        for (Topic topic : sortedTopics) {
            StudyPlanItem item = new StudyPlanItem(
                    newPlan, topic,
                    topicIdToPriority.get(topic.getId()),
                    allocation.get(topic.getId()),
                    order++);
            items.add(studyPlanItemRepository.save(item));
        }
        newPlan.setItems(items);

        return newPlan;
    }

    public StudyPlan getActivePlan(Long studentId, Long subjectId) {
        return studyPlanRepository.findByStudentIdAndSubjectIdAndIsActiveTrue(studentId, subjectId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No active study plan found for this subject."));
    }

    public List<StudyPlan> getPlanHistory(Long studentId, Long subjectId) {
        return studyPlanRepository.findByStudentIdAndSubjectIdOrderByGeneratedAtDesc(studentId, subjectId);
    }

    public List<StudyPlanItem> getItemsForPlan(Long studyPlanId) {
        return studyPlanItemRepository.findByStudyPlanIdOrderByItemOrderAsc(studyPlanId);
    }
}