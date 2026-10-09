package com.bruteforce.ila.revision;

import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.mastery.StudentTopicMastery;
import com.bruteforce.ila.revision.dto.RevisionItemResponse;
import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.student.StudentRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class RevisionSchedulerService {

    // A topic is considered "due for revision" once forgetting risk crosses this line.
    // This is a project design choice, kept as a named constant so it's easy to tune.
    private static final double REVISION_DUE_THRESHOLD = 40.0;

    private final RevisionScheduleRepository revisionRepository;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;
    private final MasteryService masteryService;

    public RevisionSchedulerService(RevisionScheduleRepository revisionRepository,
                                    StudentRepository studentRepository,
                                    TopicRepository topicRepository,
                                    MasteryService masteryService) {
        this.revisionRepository = revisionRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
        this.masteryService = masteryService;
    }

    // Ensures a RevisionSchedule row exists to track this topic for a student
    public RevisionSchedule getOrCreateSchedule(Long studentId, Long topicId) {
        return revisionRepository.findByStudentIdAndTopicId(studentId, topicId)
                .orElseGet(() -> {
                    Student student = studentRepository.findById(studentId)
                            .orElseThrow(() -> new NoSuchElementException("Student with id " + studentId + " not found."));
                    Topic topic = topicRepository.findById(topicId)
                            .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));
                    return revisionRepository.save(new RevisionSchedule(student, topic));
                });
    }

    // Marks a topic as freshly revised right now
    public RevisionSchedule markAsRevised(Long studentId, Long topicId) {
        RevisionSchedule schedule = getOrCreateSchedule(studentId, topicId);
        schedule.setLastRevisedAt(LocalDateTime.now());
        return revisionRepository.save(schedule);
    }

    /**
     * Checks every topic the student has any mastery record for, and returns
     * the ones currently due for revision - i.e. where forgetting risk has
     * crossed the threshold. This is a live check using the Day 2
     * ForgettingRiskCalculator, not a stored date comparison.
     */
    public List<RevisionItemResponse> getTopicsDueForRevision(Long studentId, Long subjectId) {
        List<StudentTopicMastery> allMastery = masteryService.getAllMasteryForStudent(studentId);

        return allMastery.stream()
                .filter(m -> subjectId == null
                        || m.getTopic().getUnit().getSubject().getId().equals(subjectId))
                .map(mastery -> {
                    double forgettingRisk = masteryService.getForgettingRisk(
                            studentId, mastery.getTopic().getId());
                    boolean isDue = forgettingRisk >= REVISION_DUE_THRESHOLD;
                    return new RevisionItemResponse(
                            mastery.getTopic().getId(), mastery.getTopic().getName(),
                            mastery.getMasteryScore(), forgettingRisk, isDue);
                })
                .filter(RevisionItemResponse::getDueForRevision)
                .toList();
    }
}