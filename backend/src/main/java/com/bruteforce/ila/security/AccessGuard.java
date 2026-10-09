package com.bruteforce.ila.security;

import com.bruteforce.ila.dependency.TopicDependency;
import com.bruteforce.ila.dependency.TopicDependencyRepository;
import com.bruteforce.ila.pyq.PYQPaper;
import com.bruteforce.ila.pyq.PYQPaperRepository;
import com.bruteforce.ila.subject.Subject;
import com.bruteforce.ila.subject.SubjectRepository;
import com.bruteforce.ila.syllabus.SyllabusUnit;
import com.bruteforce.ila.syllabus.SyllabusUnitRepository;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Makes sure a logged-in student can only touch their own data.
 * The student id comes from the login token, never from what the browser sends.
 *
 * Every check walks topic to unit to subject to owner, which are lazily loaded. The interceptor runs
 * before Spring opens its per-request database session, so each check runs in its own read-only
 * transaction. Without it those lazy loads fail with a LazyInitializationException (HTTP 500).
 */
@Component
@Transactional(readOnly = true)
public class AccessGuard {

    private final SubjectRepository subjectRepository;
    private final SyllabusUnitRepository unitRepository;
    private final TopicRepository topicRepository;
    private final TopicDependencyRepository dependencyRepository;
    private final PYQPaperRepository paperRepository;

    public AccessGuard(SubjectRepository subjectRepository, SyllabusUnitRepository unitRepository,
                       TopicRepository topicRepository, TopicDependencyRepository dependencyRepository,
                       PYQPaperRepository paperRepository) {
        this.subjectRepository = subjectRepository;
        this.unitRepository = unitRepository;
        this.topicRepository = topicRepository;
        this.dependencyRepository = dependencyRepository;
        this.paperRepository = paperRepository;
    }

    /** The id of the logged-in student, or null when nobody is logged in. */
    public static Long currentStudentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long id) {
            return id;
        }
        return null;
    }

    public void requireSelf(Long studentId) {
        Long me = currentStudentId();
        if (me != null && studentId != null && !me.equals(studentId)) {
            throw new AccessDeniedException("You can only access your own data.");
        }
    }

    public void requireSubjectOwner(Long subjectId) {
        Long me = currentStudentId();
        if (me == null || subjectId == null) {
            return;
        }
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NoSuchElementException("Subject with id " + subjectId + " not found."));
        checkOwner(me, subject);
    }

    public void requireUnitOwner(Long unitId) {
        Long me = currentStudentId();
        if (me == null || unitId == null) {
            return;
        }
        SyllabusUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new NoSuchElementException("Unit with id " + unitId + " not found."));
        checkOwner(me, unit.getSubject());
    }

    public void requireTopicOwner(Long topicId) {
        Long me = currentStudentId();
        if (me == null || topicId == null) {
            return;
        }
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));
        checkOwner(me, subjectOf(topic));
    }

    public void requireDependencyOwner(Long dependencyId) {
        Long me = currentStudentId();
        if (me == null || dependencyId == null) {
            return;
        }
        TopicDependency dependency = dependencyRepository.findById(dependencyId)
                .orElseThrow(() -> new NoSuchElementException("Dependency with id " + dependencyId + " not found."));
        checkOwner(me, subjectOf(dependency.getTopic()));
    }

    public void requirePaperOwner(Long paperId) {
        Long me = currentStudentId();
        if (me == null || paperId == null) {
            return;
        }
        PYQPaper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new NoSuchElementException("PYQ paper with id " + paperId + " not found."));
        checkOwner(me, paper.getSubject());
    }

    /** True when the topic belongs to the logged-in student (used to filter lists). */
    public boolean ownsTopic(Topic topic) {
        Long me = currentStudentId();
        if (me == null) {
            return true;
        }
        Subject subject = subjectOf(topic);
        return subject != null && subject.getOwner() != null && me.equals(subject.getOwner().getId());
    }

    /**
     * Checks the id carried by a request field. The field name says what kind of thing it is:
     * subjectId, unitId, topicId, prerequisiteTopicId or paperId. Other names are ignored.
     */
    public void requireOwnerOfField(String fieldName, Long id) {
        if (fieldName == null || id == null) {
            return;
        }
        switch (fieldName) {
            case "subjectId" -> requireSubjectOwner(id);
            case "unitId" -> requireUnitOwner(id);
            case "topicId", "prerequisiteTopicId" -> requireTopicOwner(id);
            case "paperId" -> requirePaperOwner(id);
            default -> { }
        }
    }

    private Subject subjectOf(Topic topic) {
        return topic == null || topic.getUnit() == null ? null : topic.getUnit().getSubject();
    }

    private void checkOwner(Long me, Subject subject) {
        if (subject == null || subject.getOwner() == null || !me.equals(subject.getOwner().getId())) {
            throw new AccessDeniedException("You do not have access to this item.");
        }
    }
}
