package com.bruteforce.ila.insights;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The student and subject in the URL are checked by StudentScopeInterceptor. */
@RestController
@RequestMapping("/api/insights/mistakes")
public class MistakeController {

    private final MistakeService mistakeService;

    public MistakeController(MistakeService mistakeService) {
        this.mistakeService = mistakeService;
    }

    @GetMapping("/{studentId}")
    public Map<String, Object> getNotebook(@PathVariable Long studentId, @RequestParam Long subjectId) {
        return mistakeService.notebook(studentId, subjectId);
    }
}
