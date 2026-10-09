package com.bruteforce.ila.session;

import com.bruteforce.ila.student.Student;
import com.bruteforce.ila.student.StudentRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StudySessionService {

    private final StudySessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;

    public StudySessionService(StudySessionRepository sessionRepository,
                               StudentRepository studentRepository,
                               TopicRepository topicRepository) {
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
    }

    public StudySession logSession(Long studentId, Long topicId, Integer durationMinutes) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student with id " + studentId + " not found."));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));

        StudySession session = new StudySession(student, topic, durationMinutes);
        return sessionRepository.save(session);
    }

    public List<StudySession> getSessionsForStudent(Long studentId) {
        return sessionRepository.findByStudentIdOrderBySessionDateDesc(studentId);
    }

    public Optional<StudySession> getMostRecentSession(Long studentId, Long topicId) {
        return sessionRepository.findFirstByStudentIdAndTopicIdOrderBySessionDateDesc(studentId, topicId);
    }
}