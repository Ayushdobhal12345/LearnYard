package com.bruteforce.ila.studyplan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic proportional time allocation. Given a list of (topicId, priorityScore)
 * pairs and a total number of available hours, splits those hours proportionally
 * to each topic's priority score - a topic with double the priority gets double
 * the time, out of whatever total is available.
 */
public class StudyTimeAllocator {

    // Minimum hours any topic in the plan gets, so no topic is allocated a
    // meaningless sliver of time like 0.02 hours
    private static final double MIN_ALLOCATION_HOURS = 0.25;

    public static Map<Long, Double> allocate(Map<Long, Double> topicIdToPriority, double availableHours) {
        Map<Long, Double> allocation = new HashMap<>();

        double totalPriority = topicIdToPriority.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (totalPriority <= 0 || availableHours <= 0) {
            // Nothing to allocate - return zero for every topic rather than dividing by zero
            for (Long topicId : topicIdToPriority.keySet()) {
                allocation.put(topicId, 0.0);
            }
            return allocation;
        }

        for (Map.Entry<Long, Double> entry : topicIdToPriority.entrySet()) {
            double proportionalShare = (entry.getValue() / totalPriority) * availableHours;
            double allocatedHours = Math.max(MIN_ALLOCATION_HOURS, proportionalShare);
            allocation.put(entry.getKey(), roundToQuarterHour(allocatedHours));
        }

        return allocation;
    }

    // Rounds to the nearest 0.25 hours (15 minutes) - more realistic than an exact
    // decimal like 2.3782 hours, which nobody actually plans around
    private static double roundToQuarterHour(double hours) {
        return Math.round(hours * 4.0) / 4.0;
    }
}