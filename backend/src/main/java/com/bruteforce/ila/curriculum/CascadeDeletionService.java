package com.bruteforce.ila.curriculum;

import com.bruteforce.ila.mastery.MasteryHistoryRepository;
import com.bruteforce.ila.mastery.StudentTopicMasteryRepository;
import com.bruteforce.ila.pyq.PYQPaperRepository;
import com.bruteforce.ila.pyq.PYQQuestionRepository;
import com.bruteforce.ila.quiz.AnswerAttemptRepository;
import com.bruteforce.ila.quiz.QuestionRepository;
import com.bruteforce.ila.revision.RevisionScheduleRepository;
import com.bruteforce.ila.session.StudySessionRepository;
import com.bruteforce.ila.studyplan.StudyPlanItemRepository;
import com.bruteforce.ila.studyplan.StudyPlanRepository;
import com.bruteforce.ila.subject.SubjectRepository;
import com.bruteforce.ila.syllabus.SyllabusUnitRepository;
import com.bruteforce.ila.syllabus.TopicRepository;
import com.bruteforce.ila.material.StudyMaterialRepository;
import com.bruteforce.ila.dependency.TopicDependencyRepository;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CascadeDeletionService {

    private final SubjectRepository subjectRepository;
    private final SyllabusUnitRepository unitRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;
    private final AnswerAttemptRepository answerAttemptRepository;
    private final PYQQuestionRepository pyqQuestionRepository;
    private final PYQPaperRepository pyqPaperRepository;
    private final StudentTopicMasteryRepository masteryRepository;
    private final MasteryHistoryRepository masteryHistoryRepository;
    private final TopicDependencyRepository dependencyRepository;
    private final StudyPlanItemRepository studyPlanItemRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final RevisionScheduleRepository revisionScheduleRepository;
    private final StudySessionRepository studySessionRepository;
    private final StudyMaterialRepository studyMaterialRepository;

    public CascadeDeletionService(SubjectRepository subjectRepository, SyllabusUnitRepository unitRepository,
                                  TopicRepository topicRepository, QuestionRepository questionRepository,
                                  AnswerAttemptRepository answerAttemptRepository,
                                  PYQQuestionRepository pyqQuestionRepository, PYQPaperRepository pyqPaperRepository,
                                  StudentTopicMasteryRepository masteryRepository,
                                  MasteryHistoryRepository masteryHistoryRepository,
                                  TopicDependencyRepository dependencyRepository,
                                  StudyPlanItemRepository studyPlanItemRepository,
                                  StudyPlanRepository studyPlanRepository,
                                  RevisionScheduleRepository revisionScheduleRepository,
                                  StudySessionRepository studySessionRepository,
                                  StudyMaterialRepository studyMaterialRepository) {
        this.subjectRepository = subjectRepository;
        this.unitRepository = unitRepository;
        this.topicRepository = topicRepository;
        this.questionRepository = questionRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.pyqQuestionRepository = pyqQuestionRepository;
        this.pyqPaperRepository = pyqPaperRepository;
        this.masteryRepository = masteryRepository;
        this.masteryHistoryRepository = masteryHistoryRepository;
        this.dependencyRepository = dependencyRepository;
        this.studyPlanItemRepository = studyPlanItemRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.revisionScheduleRepository = revisionScheduleRepository;
        this.studySessionRepository = studySessionRepository;
        this.studyMaterialRepository = studyMaterialRepository;
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        answerAttemptRepository.deleteByQuestion_Topic_Id(topicId);
        questionRepository.deleteByTopicId(topicId);
        pyqQuestionRepository.deleteByTopicId(topicId);
        masteryRepository.deleteByTopicId(topicId);
        masteryHistoryRepository.deleteByTopicId(topicId);
        dependencyRepository.deleteByTopicId(topicId);
        dependencyRepository.deleteByPrerequisiteTopicId(topicId);
        studyPlanItemRepository.deleteByTopicId(topicId);
        revisionScheduleRepository.deleteByTopicId(topicId);
        studySessionRepository.deleteByTopicId(topicId);
        studyMaterialRepository.deleteByTopicId(topicId);
        topicRepository.deleteById(topicId);
    }

    @Transactional
    public void deleteUnit(Long unitId) {
        topicRepository.findByUnitId(unitId).forEach(t -> deleteTopic(t.getId()));
        // notes uploaded for the whole unit still point at it, so they must go first
        studyMaterialRepository.deleteByUnitId(unitId);
        unitRepository.deleteById(unitId);
    }

    @Transactional
    public void deleteSubject(Long subjectId) {
        unitRepository.findBySubjectIdOrderByUnitOrderAsc(subjectId).forEach(u -> deleteUnit(u.getId()));
        pyqPaperRepository.deleteBySubjectId(subjectId);
        // saved study plans for this subject still point at it: remove their items, then the plans
        studyPlanItemRepository.deleteByStudyPlan_Subject_Id(subjectId);
        studyPlanRepository.deleteBySubjectId(subjectId);
        if (!subjectRepository.existsById(subjectId)) {
            throw new NoSuchElementException("Subject with id " + subjectId + " not found.");
        }
        subjectRepository.deleteById(subjectId);
    }
}