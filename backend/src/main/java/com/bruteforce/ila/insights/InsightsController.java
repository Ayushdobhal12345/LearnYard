package com.bruteforce.ila.insights;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Readiness and root-cause insights. The student and subject in the URL are checked
 * against the logged-in student by StudentScopeInterceptor.
 */
@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/status")
    public Map<String, Object> getSetupStatus(@RequestParam Long subjectId) {
        return insightsService.setupStatus(subjectId);
    }

    @GetMapping("/readiness/{studentId}")
    public Map<String, Object> getReadiness(@PathVariable Long studentId,
                                            @RequestParam Long subjectId,
                                            @RequestParam(required = false) Integer daysLeft) {
        return insightsService.readiness(studentId, subjectId, daysLeft);
    }

    @GetMapping("/root-causes/{studentId}")
    public Map<String, Object> getRootCauses(@PathVariable Long studentId, @RequestParam Long subjectId) {
        return insightsService.rootCauses(studentId, subjectId);
    }
}
