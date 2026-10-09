package com.bruteforce.ila.insights;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The maths behind the Readiness page. No database or Spring here, so it is easy to test.
 * All scores are on a 0 to 100 scale.
 */
public final class InsightCalculator {

    /** A prerequisite below this mastery counts as a gap. */
    public static final double GAP_BELOW = 50.0;

    /** A practised topic below this mastery needs help. */
    public static final double SYMPTOM_BELOW = 60.0;

    /** Forgetting can lower a topic's contribution by at most this share. */
    public static final double MAX_FORGETTING_DISCOUNT = 0.30;

    private InsightCalculator() {
    }

    /** What we know about one topic. Mastery is 0 for a topic that was never practised. */
    public record TopicState(long id, String name, boolean practiced, double mastery,
                             double forgettingRisk, double examImportance, double masteryWeekAgo) {
    }

    public record Win(long id, String name, boolean practiced, double mastery, double gain) {
    }

    public record Readiness(double readiness, double weeklyChange, int practicedTopics,
                            int totalTopics, List<Win> biggestWins) {
    }

    public record Gap(long id, String name, boolean practiced, double mastery) {
    }

    public record Finding(long id, String name, double mastery, List<Gap> rootCauses, String verdict) {
    }

    public record StartHere(Gap topic, List<String> blocks) {
    }

    public record RootCauseReport(List<StartHere> startHere, List<Finding> findings) {
    }

    // ------------------------------------------------------------------ readiness

    /** Topics that appear more often in past papers count up to twice as much. */
    private static double weight(TopicState t) {
        return 1.0 + Math.max(0.0, Math.min(100.0, t.examImportance())) / 100.0;
    }

    /** Mastery, lowered a little for topics at risk of being forgotten. Never-practised topics count as 0. */
    private static double effectiveMastery(TopicState t) {
        if (!t.practiced()) {
            return 0.0;
        }
        double risk = Math.max(0.0, Math.min(100.0, t.forgettingRisk())) / 100.0;
        return t.mastery() * (1.0 - MAX_FORGETTING_DISCOUNT * risk);
    }

    public static Readiness readiness(List<TopicState> topics) {
        if (topics.isEmpty()) {
            return new Readiness(0, 0, 0, 0, List.of());
        }

        double totalWeight = 0;
        double weighted = 0;
        double plainNow = 0;
        double plainBefore = 0;
        int practiced = 0;

        for (TopicState t : topics) {
            double w = weight(t);
            totalWeight += w;
            weighted += w * effectiveMastery(t);
            plainNow += w * (t.practiced() ? t.mastery() : 0.0);
            plainBefore += w * t.masteryWeekAgo();
            if (t.practiced()) {
                practiced++;
            }
        }

        double readiness = weighted / totalWeight;
        double weeklyChange = (plainNow - plainBefore) / totalWeight;

        List<Win> wins = new ArrayList<>();
        for (TopicState t : topics) {
            double gain = weight(t) * (100.0 - effectiveMastery(t)) / totalWeight;
            if (gain >= 0.5) {
                wins.add(new Win(t.id(), t.name(), t.practiced(), t.mastery(), gain));
            }
        }
        wins.sort(Comparator.comparingDouble(Win::gain).reversed().thenComparing(Win::name));

        return new Readiness(readiness, weeklyChange, practiced, topics.size(),
                wins.size() > 5 ? new ArrayList<>(wins.subList(0, 5)) : wins);
    }

    /** Where the score ends up on exam day if the student keeps the pace of the last 7 days. */
    public static double project(double readiness, double weeklyChange, int daysLeft) {
        if (daysLeft <= 0) {
            return readiness;
        }
        double weeks = daysLeft / 7.0;
        return Math.max(0.0, Math.min(100.0, readiness + Math.max(0.0, weeklyChange) * weeks));
    }

    public static String label(double readiness) {
        if (readiness >= 80) {
            return "Exam ready";
        }
        if (readiness >= 60) {
            return "On track";
        }
        if (readiness >= 35) {
            return "Building up";
        }
        return "Needs work";
    }

    // ----------------------------------------------------------------- root causes

    /**
     * For every weak topic, walks back through its prerequisites to find the earliest topics
     * that are themselves weak or untested. Those are where to start.
     *
     * @param prerequisites topic id to the ids of the topics it requires
     */
    public static RootCauseReport rootCauses(List<TopicState> topics, Map<Long, List<Long>> prerequisites) {
        Map<Long, TopicState> byId = new LinkedHashMap<>();
        for (TopicState t : topics) {
            byId.put(t.id(), t);
        }

        List<Finding> findings = new ArrayList<>();
        Map<Long, List<String>> blocksByRoot = new LinkedHashMap<>();

        for (TopicState symptom : topics) {
            if (!symptom.practiced() || symptom.mastery() >= SYMPTOM_BELOW) {
                continue;
            }

            Set<Long> gaps = new HashSet<>();
            for (Long prerequisiteId : allPrerequisites(symptom.id(), prerequisites)) {
                TopicState p = byId.get(prerequisiteId);
                if (p != null && isGap(p)) {
                    gaps.add(prerequisiteId);
                }
            }

            // the earliest gaps: ones with no gap further back
            List<Gap> roots = new ArrayList<>();
            for (Long gapId : gaps) {
                boolean hasEarlierGap = false;
                for (Long earlier : allPrerequisites(gapId, prerequisites)) {
                    if (gaps.contains(earlier)) {
                        hasEarlierGap = true;
                        break;
                    }
                }
                if (!hasEarlierGap) {
                    TopicState g = byId.get(gapId);
                    roots.add(new Gap(g.id(), g.name(), g.practiced(), g.mastery()));
                    blocksByRoot.computeIfAbsent(g.id(), k -> new ArrayList<>()).add(symptom.name());
                }
            }
            roots.sort(Comparator.comparingDouble((Gap g) -> g.practiced() ? g.mastery() : -1.0)
                    .thenComparing(Gap::name));

            findings.add(new Finding(symptom.id(), symptom.name(), symptom.mastery(), roots,
                    roots.isEmpty() ? "own_gap" : "prerequisite_gap"));
        }
        findings.sort(Comparator.comparingDouble(Finding::mastery).thenComparing(Finding::name));

        List<StartHere> startHere = new ArrayList<>();
        for (Map.Entry<Long, List<String>> entry : blocksByRoot.entrySet()) {
            TopicState g = byId.get(entry.getKey());
            startHere.add(new StartHere(new Gap(g.id(), g.name(), g.practiced(), g.mastery()), entry.getValue()));
        }
        startHere.sort(Comparator
                .comparingInt((StartHere s) -> s.blocks().size()).reversed()
                .thenComparingDouble(s -> s.topic().practiced() ? s.topic().mastery() : -1.0)
                .thenComparing(s -> s.topic().name()));

        return new RootCauseReport(startHere, findings);
    }

    private static boolean isGap(TopicState t) {
        return !t.practiced() || t.mastery() < GAP_BELOW;
    }

    /** Every topic the given topic requires, directly or through other topics. */
    private static Set<Long> allPrerequisites(long topicId, Map<Long, List<Long>> prerequisites) {
        Set<Long> seen = new HashSet<>();
        Deque<Long> toVisit = new ArrayDeque<>(prerequisites.getOrDefault(topicId, List.of()));
        while (!toVisit.isEmpty()) {
            Long current = toVisit.pop();
            if (current != topicId && seen.add(current)) {
                toVisit.addAll(prerequisites.getOrDefault(current, List.of()));
            }
        }
        return seen;
    }
}
