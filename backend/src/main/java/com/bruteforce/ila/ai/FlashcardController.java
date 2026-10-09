package com.bruteforce.ila.ai;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class FlashcardController {

    private final FlashcardService flashcardService;
    private final com.bruteforce.ila.security.AccessGuard accessGuard;

    public FlashcardController(FlashcardService flashcardService, com.bruteforce.ila.security.AccessGuard accessGuard) {
        this.flashcardService = flashcardService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/flashcards")
    public List<Map<String, String>> generate(@RequestParam Long topicId, @RequestParam(defaultValue = "8") int count,
                                              @RequestParam(defaultValue = "false") boolean refresh) {
        accessGuard.requireTopicOwner(topicId);
        return flashcardService.generateFlashcardsCached(topicId, Math.max(1, Math.min(20, count)), refresh);
    }
}