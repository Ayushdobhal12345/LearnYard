package com.bruteforce.ila.insights;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turns a student's quiz answers into a list of questions they got wrong.
 * A mistake is "fixed" once the most recent answer to that question was correct.
 * No database or Spring here, so it is easy to test.
 */
public final class MistakeCalculator {

    private MistakeCalculator() {
    }

    /** One answer a student gave. order grows with time (higher = more recent). */
    public record Attempt(long questionId, String selectedOption, boolean correct, long order) {
    }

    public record Mistake(long questionId, int timesWrong, int timesAnswered,
                          String lastWrongOption, long lastWrongOrder, boolean fixed) {
    }

    /** Only questions that were answered wrong at least once. Open mistakes first, then most often wrong. */
    public static List<Mistake> summarize(List<Attempt> attempts) {
        Map<Long, List<Attempt>> byQuestion = new LinkedHashMap<>();
        for (Attempt a : attempts) {
            byQuestion.computeIfAbsent(a.questionId(), k -> new ArrayList<>()).add(a);
        }

        List<Mistake> mistakes = new ArrayList<>();
        for (Map.Entry<Long, List<Attempt>> entry : byQuestion.entrySet()) {
            List<Attempt> history = new ArrayList<>(entry.getValue());
            history.sort(Comparator.comparingLong(Attempt::order));

            int wrong = 0;
            Attempt lastWrong = null;
            for (Attempt a : history) {
                if (!a.correct()) {
                    wrong++;
                    lastWrong = a;
                }
            }
            if (wrong == 0) {
                continue;
            }

            boolean fixed = history.get(history.size() - 1).correct();
            mistakes.add(new Mistake(entry.getKey(), wrong, history.size(),
                    lastWrong.selectedOption(), lastWrong.order(), fixed));
        }

        mistakes.sort(Comparator
                .comparing(Mistake::fixed)
                .thenComparing(Comparator.comparingInt(Mistake::timesWrong).reversed())
                .thenComparing(Comparator.comparingLong(Mistake::lastWrongOrder).reversed()));
        return mistakes;
    }
}
