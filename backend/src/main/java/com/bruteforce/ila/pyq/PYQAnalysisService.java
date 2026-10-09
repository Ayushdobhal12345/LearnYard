package com.bruteforce.ila.pyq;

import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class PYQAnalysisService {

    private final PYQPaperRepository paperRepository;
    private final PYQQuestionRepository questionRepository;
    private final TopicRepository topicRepository;

    public java.util.List<PYQPaper> getPapersForSubject(Long subjectId) {
        return paperRepository.findBySubjectId(subjectId);
    }

    public PYQAnalysisService(PYQPaperRepository paperRepository,
                              PYQQuestionRepository questionRepository,
                              TopicRepository topicRepository) {
        this.paperRepository = paperRepository;
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
    }

    public PYQPaper createPaper(Integer year, com.bruteforce.ila.subject.Subject subject) {
        return paperRepository.save(new PYQPaper(year, subject));
    }

    public PYQQuestion addQuestion(String questionText, Integer marks, Long paperId, Long topicId) {
        PYQPaper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new NoSuchElementException("PYQ Paper with id " + paperId + " not found."));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));
        PYQQuestion question = new PYQQuestion(questionText, marks, paper, topic);
        return questionRepository.save(question);
    }

    /**
     * Calculates a 0-100 Exam Importance score for a topic, based on how
     * frequently it appears in PYQ papers relative to the MOST frequent
     * topic in the same subject. This is the direct input to the Priority
     * Formula's "Exam Importance" component.
     */
    public double calculateExamImportance(Long topicId, Long subjectId) {
        long thisTopicFrequency = questionRepository.countByTopicId(topicId);

        List<Long> allFrequencies = questionRepository.getFrequencyCountsForSubject(subjectId);
        long maxFrequency = allFrequencies.isEmpty() ? 1 : allFrequencies.get(0);

        if (maxFrequency == 0) {
            return 0.0;
        }

        // Scale this topic's frequency relative to the most-tested topic, capped at 100
        double examImportance = (thisTopicFrequency / (double) maxFrequency) * 100.0;
        return Math.min(100.0, examImportance);
    }

    public long getFrequency(Long topicId) {
        return questionRepository.countByTopicId(topicId);
    }

    public Integer getTotalMarks(Long topicId) {
        return questionRepository.sumMarksByTopicId(topicId);
    }
}