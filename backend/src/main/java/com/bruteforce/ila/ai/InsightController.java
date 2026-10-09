package com.bruteforce.ila.ai;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/insight/{studentId}")
    public Map<String, String> getInsight(@PathVariable Long studentId) {
        return Map.of("insight", insightService.generateWeeklyInsight(studentId));
    }
}