package com.bruteforce.ila.ai;

import com.bruteforce.ila.material.StudyMaterial;
import com.bruteforce.ila.material.StudyMaterialRepository;
import com.bruteforce.ila.syllabus.Topic;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Saves AI answers in the database. A cache problem must never break a feature,
 * so every method here fails quietly and the caller simply generates a fresh answer.
 */
@Service
public class AiCacheService {

    /** The text plus whether it came from the cache. */
    public record CachedText(String text, boolean cached) {
    }

    private final AiCacheRepository repository;
    private final StudyMaterialRepository studyMaterialRepository;

    public AiCacheService(AiCacheRepository repository, StudyMaterialRepository studyMaterialRepository) {
        this.repository = repository;
        this.studyMaterialRepository = studyMaterialRepository;
    }

    public String get(String key) {
        try {
            return repository.findByCacheKey(key).map(AiCache::getContent).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public void put(String key, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        try {
            AiCache entry = repository.findByCacheKey(key).orElseGet(AiCache::new);
            entry.setCacheKey(key);
            entry.setContent(content);
            entry.setCreatedAt(LocalDateTime.now());
            repository.save(entry);
        } catch (Exception ignored) {
            // two requests saving the same key at once, or a database hiccup: skip caching
        }
    }

    /**
     * Changes whenever notes are added to (or removed from) the topic or its unit,
     * so cached answers are rebuilt from the new notes.
     */
    public String notesStamp(Topic topic) {
        try {
            List<StudyMaterial> topicNotes = studyMaterialRepository.findByTopicId(topic.getId());
            List<StudyMaterial> unitNotes = topic.getUnit() == null
                    ? List.of()
                    : studyMaterialRepository.findByUnitId(topic.getUnit().getId());

            long newest = 0;
            for (StudyMaterial m : topicNotes) {
                newest = Math.max(newest, m.getId());
            }
            for (StudyMaterial m : unitNotes) {
                newest = Math.max(newest, m.getId());
            }
            return (topicNotes.size() + unitNotes.size()) + "-" + newest;
        } catch (Exception e) {
            return "x";
        }
    }
}
