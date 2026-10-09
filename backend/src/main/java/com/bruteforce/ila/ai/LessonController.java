package com.bruteforce.ila.ai;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class LessonController {

    private final LessonService lessonService;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public LessonController(LessonService lessonService, com.bruteforce.ila.security.AccessGuard accessGuard) {
        this.lessonService = lessonService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/lesson")
    public Map<String, Object> teach(@RequestParam Long studentId, @RequestParam Long topicId,
                                     @RequestParam(defaultValue = "false") boolean refresh) {
        accessGuard.requireTopicOwner(topicId);
        AiCacheService.CachedText result = lessonService.teachTopicCached(studentId, topicId, refresh);
        return Map.of("lesson", result.text(), "cached", result.cached());
    }
}