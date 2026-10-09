package com.bruteforce.ila.quiz;

import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.quiz.dto.SubmitAnswerRequest;
import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.student.StudentRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final AnswerAttemptRepository answerAttemptRepository;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;
    private final MasteryService masteryService;
    private final com.bruteforce.ila.studyplan.StudyPlanService studyPlanService;
    private final com.bruteforce.ila.studyplan.StudyPlanRepository studyPlanRepository;

    public QuizService(QuestionRepository questionRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       AnswerAttemptRepository answerAttemptRepository,
                       StudentRepository studentRepository,
                       TopicRepository topicRepository,
                       MasteryService masteryService,
                       com.bruteforce.ila.studyplan.StudyPlanService studyPlanService,
                       com.bruteforce.ila.studyplan.StudyPlanRepository studyPlanRepository) {
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.answerAttemptRepository = answerAttemptRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
        this.masteryService = masteryService;
        this.studyPlanService = studyPlanService;
        this.studyPlanRepository = studyPlanRepository;
    }

    public Question createQuestion(String questionText, String optionA, String optionB,
                                   String optionC, String optionD, String correctOption,
                                   Integer difficultyLevel, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Topic with id " + topicId + " not found."));

        Question question = new Question(
                questionText,
                optionA,
                optionB,
                optionC,
                optionD,
                correctOption,
                difficultyLevel,
                topic
        );

        return questionRepository.save(question);
    }

    public List<Question> getQuestionsByTopic(Long topicId) {
        return questionRepository.findByTopicId(topicId);
    }

    public Long getSubjectIdForTopic(Long topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Topic with id " + topicId + " not found."))
                .getUnit()
                .getSubject()
                .getId();
    }

    public Long getTopicIdForQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Question with id " + questionId + " not found."))
                .getTopic()
                .getId();
    }

    /**
     * THE CORE METHOD: submits a full quiz attempt.
     * For each answer: checks correctness, saves the AnswerAttempt,
     * and updates mastery for that question's topic via MasteryService.
     * @Transactional ensures ALL of this succeeds together, or none of it does.
     */
    @Transactional
    public QuizAttempt submitQuiz(Long studentId, List<SubmitAnswerRequest> answers) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Student with id " + studentId + " not found."));

        QuizAttempt attempt = new QuizAttempt(student);
        attempt = quizAttemptRepository.save(attempt);

        int correctCount = 0;

        for (SubmitAnswerRequest answerRequest : answers) {
            Question question = questionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Question with id " + answerRequest.getQuestionId() + " not found."));

            boolean wasCorrect = question.getCorrectOption()
                    .equalsIgnoreCase(answerRequest.getSelectedOption());

            if (wasCorrect) {
                correctCount++;
            }

            AnswerAttempt answerAttempt = new AnswerAttempt(
                    attempt,
                    question,
                    answerRequest.getSelectedOption(),
                    wasCorrect
            );

            answerAttemptRepository.save(answerAttempt);

            // Every answer updates mastery
            masteryService.recordAnswer(
                    studentId,
                    question.getTopic().getId(),
                    wasCorrect,
                    question.getDifficultyLevel()
            );
        }

        attempt.setTotalQuestions(answers.size());
        attempt.setCorrectAnswers(correctCount);

        return quizAttemptRepository.save(attempt);
    }

    /**
     * The Adaptive Replanner.
     *
     * Called automatically after a quiz is submitted.
     * Looks for an active study plan for the specific subject,
     * then regenerates that plan using the existing generatePlan(...) logic.
     */
    public com.bruteforce.ila.studyplan.dto.ReplanResultResponse replanIfNeeded(
            Long studentId,
            Long subjectId) {

        return studyPlanRepository
                .findByStudentIdAndSubjectIdAndIsActiveTrue(studentId, subjectId)
                .map(activePlan -> {

                    Long oldPlanId = activePlan.getId();

                    var newPlan = studyPlanService.generatePlan(
                            studentId,
                            subjectId,
                            activePlan.getAvailableHours(),
                            activePlan.getHoursUntilExam()
                    );

                    return new com.bruteforce.ila.studyplan.dto.ReplanResultResponse(
                            true,
                            oldPlanId,
                            newPlan.getId(),
                            "Study plan automatically regenerated based on updated mastery."
                    );
                })
                .orElseGet(() ->
                        new com.bruteforce.ila.studyplan.dto.ReplanResultResponse(
                                false,
                                null,
                                null,
                                "No active study plan found - nothing to replan."
                        )
                );
    }
}