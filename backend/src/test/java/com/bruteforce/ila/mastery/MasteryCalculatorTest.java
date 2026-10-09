package com.bruteforce.ila.mastery;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MasteryCalculatorTest {

    @Test
    void correctAnswerOnMediumQuestionIncreasesMastery() {
        double result = MasteryCalculator.calculateNewMastery(50.0, true, 2);
        assertEquals(58.0, result, 0.01);
    }

    @Test
    void wrongAnswerOnEasyQuestionPenalizesMore() {
        double result = MasteryCalculator.calculateNewMastery(50.0, false, 1);
        assertEquals(44.0, result, 0.01);
    }

    @Test
    void correctAnswerOnHardQuestionIncreasesMoreThanEasy() {
        double hard = MasteryCalculator.calculateNewMastery(50.0, true, 3);
        double easy = MasteryCalculator.calculateNewMastery(50.0, true, 1);
        assertEquals(50.0 + 12.0, hard, 0.01);
        assertEquals(50.0 + 4.0, easy, 0.01);
    }

    @Test
    void masteryNeverExceeds100() {
        double result = MasteryCalculator.calculateNewMastery(98.0, true, 3);
        assertEquals(100.0, result, 0.01);
    }

    @Test
    void masteryNeverGoesBelowZero() {
        double result = MasteryCalculator.calculateNewMastery(2.0, false, 1);
        assertEquals(0.0, result, 0.01);
    }
}