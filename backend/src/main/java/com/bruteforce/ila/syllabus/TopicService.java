package com.bruteforce.ila.syllabus;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;

@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final SyllabusUnitRepository unitRepository;

    public TopicService(TopicRepository topicRepository, SyllabusUnitRepository unitRepository) {
        this.topicRepository = topicRepository;
        this.unitRepository = unitRepository;
    }

    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }

    public Topic createTopic(String name, String description, Integer difficultyLevel, Long unitId) {
        SyllabusUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new NoSuchElementException("Unit with id " + unitId + " not found."));
        Topic topic = new Topic(name, description, difficultyLevel, unit);
        return topicRepository.save(topic);
    }

    public List<Topic> getTopicsByUnit(Long unitId) {
        return topicRepository.findByUnitId(unitId);
    }

    public Topic getTopicById(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Topic with id " + id + " not found."));
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }
}