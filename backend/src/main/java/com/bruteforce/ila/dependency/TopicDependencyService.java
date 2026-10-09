package com.bruteforce.ila.dependency;

import com.bruteforce.ila.common.exception.DuplicateResourceException;
import com.bruteforce.ila.syllabus.Topic;
import com.bruteforce.ila.syllabus.TopicRepository;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class TopicDependencyService {

    private final TopicDependencyRepository dependencyRepository;
    private final TopicRepository topicRepository;

    public TopicDependencyService(TopicDependencyRepository dependencyRepository,
                                  TopicRepository topicRepository) {
        this.dependencyRepository = dependencyRepository;
        this.topicRepository = topicRepository;
    }

    public List<TopicDependency> getDependenciesForSubject(Long subjectId) {
        return dependencyRepository.findBySubjectId(subjectId);
    }

    public void deleteDependency(Long id) {
        dependencyRepository.deleteById(id);
    }

    public TopicDependency createDependency(Long topicId, Long prerequisiteTopicId) {
        if (topicId.equals(prerequisiteTopicId)) {
            throw new IllegalArgumentException("A topic cannot depend on itself.");
        }

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + topicId + " not found."));
        Topic prerequisite = topicRepository.findById(prerequisiteTopicId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Prerequisite topic with id " + prerequisiteTopicId + " not found."));

        if (dependencyRepository.existsByTopicIdAndPrerequisiteTopicId(topicId, prerequisiteTopicId)) {
            throw new DuplicateResourceException("This dependency already exists.");
        }

        if (wouldCreateCycle(topicId, prerequisiteTopicId)) {
            throw new IllegalArgumentException("That would create a circular prerequisite: \""
                    + prerequisite.getName() + "\" already depends on \"" + topic.getName()
                    + "\" (directly or through other topics).");
        }

        TopicDependency dependency = new TopicDependency(topic, prerequisite);
        return dependencyRepository.save(dependency);
    }

    /**
     * Adding "topic requires prerequisite" makes a loop when the prerequisite
     * already requires the topic, directly or through a chain of other topics.
     */
    private boolean wouldCreateCycle(Long topicId, Long prerequisiteTopicId) {
        Set<Long> seen = new HashSet<>();
        Deque<Long> toVisit = new ArrayDeque<>();
        toVisit.push(prerequisiteTopicId);

        while (!toVisit.isEmpty()) {
            Long current = toVisit.pop();
            if (current.equals(topicId)) {
                return true;
            }
            if (!seen.add(current)) {
                continue;
            }
            for (TopicDependency d : dependencyRepository.findByTopicId(current)) {
                toVisit.push(d.getPrerequisiteTopic().getId());
            }
        }
        return false;
    }

    // All prerequisites a topic requires before it can be studied
    public List<TopicDependency> getPrerequisitesOf(Long topicId) {
        return dependencyRepository.findByTopicId(topicId);
    }

    // All topics that require this topic as a prerequisite
    // The SIZE of this list is exactly the "Prerequisite Impact" score input
    public List<TopicDependency> getTopicsThatDependOn(Long topicId) {
        return dependencyRepository.findByPrerequisiteTopicId(topicId);
    }
}