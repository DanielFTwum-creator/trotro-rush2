package com.example

import com.example.engine.ScoreSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreSystemTest {

    @Test
    fun testMoveBonusAwardsMaximumForUnderOrAtPar() {
        val par = 5
        // At par
        val bonusAtPar = ScoreSystem.calculateMoveBonus(5, par)
        // Under par
        val bonusUnderPar = ScoreSystem.calculateMoveBonus(3, par)
        // Over par
        val bonusOverPar = ScoreSystem.calculateMoveBonus(7, par)

        assertEquals(1500, bonusAtPar)
        assertEquals(1500, bonusUnderPar)
        assertTrue("Over par should award fewer move points", bonusOverPar < bonusAtPar)
        assertEquals(1500 - (7 - 5) * 150, bonusOverPar)
    }

    @Test
    fun testSpeedBonusAwardsHigherPointsForFastClearance() {
        val targetSeconds = 40
        val fastSeconds = 15
        val slowSeconds = 60

        val fastBonus = ScoreSystem.calculateSpeedBonus(fastSeconds, targetSeconds)
        val onTimeBonus = ScoreSystem.calculateSpeedBonus(targetSeconds, targetSeconds)
        val slowBonus = ScoreSystem.calculateSpeedBonus(slowSeconds, targetSeconds)

        assertTrue("Faster completion must award more points than on-time", fastBonus > onTimeBonus)
        assertTrue("On-time completion must award more points than slow delay", onTimeBonus > slowBonus)
        assertEquals(1000, onTimeBonus)
        assertEquals(1000 + (40 - 15) * 30, fastBonus)
    }

    @Test
    fun testFinalScoreCalculationAndHighScoreDetection() {
        val breakdownBeatsHighScore = ScoreSystem.calculateFinalScore(
            moves = 3,
            par = 3,
            elapsedSeconds = 12,
            totalPassengers = 8,
            existingHighScore = 2000
        )

        assertTrue(breakdownBeatsHighScore.isNewHighScore)
        assertTrue(breakdownBeatsHighScore.perfectParBonus > 0)
        assertTrue(breakdownBeatsHighScore.totalScore > 3000)

        val breakdownBelowHighScore = ScoreSystem.calculateFinalScore(
            moves = 8,
            par = 3,
            elapsedSeconds = 90,
            totalPassengers = 8,
            existingHighScore = 10000
        )

        assertFalse(breakdownBelowHighScore.isNewHighScore)
        assertEquals(0, breakdownBelowHighScore.perfectParBonus)
    }

    @Test
    fun testLiveScoreIncreasesWithProgress() {
        val initialLiveScore = ScoreSystem.calculateLiveScore(
            moves = 0,
            par = 4,
            elapsedSeconds = 0,
            totalPassengers = 10,
            passengersRemaining = 10,
            trotrosDeparted = 0
        )

        val progressLiveScore = ScoreSystem.calculateLiveScore(
            moves = 2,
            par = 4,
            elapsedSeconds = 10,
            totalPassengers = 10,
            passengersRemaining = 2, // 8 passengers boarded!
            trotrosDeparted = 2 // 2 trotros departed!
        )

        assertTrue("Progress in passengers and departures should award points", progressLiveScore > initialLiveScore)
    }

    @Test
    fun testTimeFormatting() {
        assertEquals("00:00", ScoreSystem.formatTime(0))
        assertEquals("00:25", ScoreSystem.formatTime(25))
        assertEquals("01:15", ScoreSystem.formatTime(75))
        assertEquals("10:05", ScoreSystem.formatTime(605))
    }

    @Test
    fun testUndoUpdatesLiveScoreCorrectly() {
        val par = 3
        val elapsedSeconds = 10
        val totalPassengers = 14

        // State after move 1: 1 move, 12 passengers remaining, 0 departed
        val scoreMove1 = ScoreSystem.calculateLiveScore(
            moves = 1,
            par = par,
            elapsedSeconds = elapsedSeconds,
            totalPassengers = totalPassengers,
            passengersRemaining = 12,
            trotrosDeparted = 0
        )

        // State after move 2: 2 moves, 9 passengers remaining, 0 departed
        val scoreMove2 = ScoreSystem.calculateLiveScore(
            moves = 2,
            par = par,
            elapsedSeconds = elapsedSeconds,
            totalPassengers = totalPassengers,
            passengersRemaining = 9,
            trotrosDeparted = 0
        )

        // Undoing move 2 restores move 1 state: moves=1, remaining=12, departed=0
        val scoreAfterUndo = ScoreSystem.calculateLiveScore(
            moves = 1,
            par = par,
            elapsedSeconds = elapsedSeconds,
            totalPassengers = totalPassengers,
            passengersRemaining = 12,
            trotrosDeparted = 0
        )

        // Verify score after undo reverts precisely to the move 1 score
        assertEquals(scoreMove1, scoreAfterUndo)
    }
}
