package com.bruteforce.ila.insights;

import com.bruteforce.ila.quiz.AnswerAttempt;
import com.bruteforce.ila.quiz.AnswerAttemptRepository;
import com.bruteforce.ila.quiz.Question;
import com.bruteforce.ila.syllabus.Topic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The mistake notebook: every quiz question the student has answered wrongly, grouped by topic. */
@Service
public class MistakeService {

    private final AnswerAttemptRepository answerAttemptRepository;

    public MistakeService(AnswerAttemptRepository answerAttemptRepository) {
        this.answerAttemptRepository = answerAttemptRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> notebook(Long studentId, Long subjectId) {
        List<MistakeCalculator.Attempt> attempts = new ArrayList<>();
        Map<Long, Question> questions = new HashMap<>();
        Map<Long, java.time.LocalDateTime> answeredAt = new HashMap<>();

        for (AnswerAttempt a : answerAttemptRepository.findByQuizAttempt_Student_Id(studentId)) {
            Question q = a.getQuestion();
            Topic topic = q == null ? null : q.getTopic();
            if (topic == null || topic.getUnit() == null || topic.getUnit().getSubject() == null
                    || !subjectId.equals(topic.getUnit().getSubject().getId())) {
                continue;
            }
            questions.put(q.getId(), q);
            attempts.add(new MistakeCalculator.Attempt(
                    q.getId(), a.getSelectedOption(), Boolean.TRUE.equals(a.getWasCorrect()), a.getId()));
            if (!Boolean.TRUE.equals(a.getWasCorrect())) {
                answeredAt.put(a.getId(), a.getQuizAttempt().getSubmittedAt());
            }
        }

        List<MistakeCalculator.Mistake> mistakes = MistakeCalculator.summarize(attempts);

        // group by topic, keeping the order the calculator chose inside each topic
        Map<Long, Map<String, Object>> topicRows = new LinkedHashMap<>();
        Map<Long, Integer> openByTopic = new HashMap<>();
        int open = 0;
        int fixed = 0;

        for (MistakeCalculator.Mistake m : mistakes) {
            Question q = questions.get(m.questionId());
            Topic topic = q.getTopic();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionId", q.getId());
            row.put("questionText", q.getQuestionText());
            row.put("optionA", q.getOptionA());
            row.put("optionB", q.getOptionB());
            row.put("optionC", q.getOptionC());
            row.put("optionD", q.getOptionD());
            row.put("correctOption", q.getCorrectOption());
            row.put("yourOption", m.lastWrongOption());
            row.put("timesWrong", m.timesWrong());
            row.put("timesAnswered", m.timesAnswered());
            row.put("fixed", m.fixed());
            row.put("lastWrongAt", answeredAt.get(m.lastWrongOrder()));

            Map<String, Object> topicRow = topicRows.computeIfAbsent(topic.getId(), id -> {
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("topicId", topic.getId());
                t.put("topicName", topic.getName());
                t.put("mistakes", new ArrayList<Map<String, Object>>());
                return t;
            });
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) topicRow.get("mistakes");
            list.add(row);

            if (m.fixed()) {
                fixed++;
            } else {
                open++;
                openByTopic.merge(topic.getId(), 1, Integer::sum);
            }
        }

        List<Map<String, Object>> topics = new ArrayList<>(topicRows.values());
        for (Map<String, Object> t : topics) {
            t.put("openCount", openByTopic.getOrDefault((Long) t.get("topicId"), 0));
        }
        topics.sort(Comparator
                .comparingInt((Map<String, Object> t) -> (Integer) t.get("openCount")).reversed()
                .thenComparing(t -> (String) t.get("topicName")));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("openCount", open);
        out.put("fixedCount", fixed);
        out.put("topics", topics);
        return out;
    }
}
