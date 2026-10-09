package com.bruteforce.ila.analytics;

import com.bruteforce.ila.analytics.dto.DashboardSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/student/{studentId}/dashboard")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(@PathVariable Long studentId) {
        return ResponseEntity.ok(analyticsService.getDashboardSummary(studentId));
    }
}