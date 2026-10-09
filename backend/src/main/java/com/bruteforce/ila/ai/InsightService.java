package com.bruteforce.ila.ai;

import com.bruteforce.ila.mastery.MasteryService;
import com.bruteforce.ila.mastery.StudentTopicMastery;
import com.bruteforce.ila.quiz.QuizAttemptRepository;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class InsightService {

    private final ChatClient chatClient;
    private final MasteryService masteryService;
    private final QuizAttemptRepository quizAttemptRepository;

    public InsightService(ChatClient.Builder chatClientBuilder, MasteryService masteryService,
                          QuizAttemptRepository quizAttemptRepository) {
        this.chatClient = chatClientBuilder.build();
        this.masteryService = masteryService;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public String generateWeeklyInsight(Long studentId) {
        List<StudentTopicMastery> allMastery = masteryService.getAllMasteryForStudent(studentId);
        if (allMastery.isEmpty()) {
            return "Once you start taking quizzes, I'll give you a personalized weekly progress summary here.";
        }

        List<StudentTopicMastery> weak = masteryService.getWeakTopics(studentId, 50.0);
        int totalAttempts = quizAttemptRepository.findByStudentId(studentId).size();
        double avgMastery = allMastery.stream().mapToDouble(StudentTopicMastery::getMasteryScore).average().orElse(0);

        String data = "Average mastery: " + Math.round(avgMastery) + "%\n"
                + "Topics tracked: " + allMastery.size() + "\n"
                + "Weak topics: " + (weak.isEmpty() ? "none" :
                weak.stream().map(m -> m.getTopic().getName()).reduce((a, b) -> a + ", " + b).orElse("none")) + "\n"
                + "Total quiz attempts: " + totalAttempts;

        String prompt = """
                Based on this real student data, write a short 2-3 sentence encouraging
                progress check-in, like a coach. Mention one specific strength and one
                area to focus on. Never invent numbers not given below.

                %s
                """.formatted(data);

        return chatClient.prompt(prompt).call().content();
    }
}