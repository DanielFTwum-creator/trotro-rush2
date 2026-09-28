package com.example

import com.example.data.ProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeaderboardTest {

    @Test
    fun testMinimumMovesPreservedOnBetterRun() {
        // Initial state before run
        var currentBestMoves = 0
        var currentHighScore = 0

        // Run 1: completed in 6 moves, scored 1200
        val run1Moves = 6
        val run1Score = 1200
        currentBestMoves = if (currentBestMoves > 0) minOf(currentBestMoves, run1Moves) else run1Moves
        currentHighScore = maxOf(currentHighScore, run1Score)

        assertEquals(6, currentBestMoves)
        assertEquals(1200, currentHighScore)

        // Run 2: completed faster in 4 moves, scored 1800
        val run2Moves = 4
        val run2Score = 1800
        currentBestMoves = if (currentBestMoves > 0) minOf(currentBestMoves, run2Moves) else run2Moves
        currentHighScore = maxOf(currentHighScore, run2Score)

        // Best moves must improve to 4, high score to 1800
        assertEquals(4, currentBestMoves)
        assertEquals(1800, currentHighScore)

        // Run 3: completed in 5 moves (slower than best), scored 1500
        val run3Moves = 5
        val run3Score = 1500
        currentBestMoves = if (currentBestMoves > 0) minOf(currentBestMoves, run3Moves) else run3Moves
        currentHighScore = maxOf(currentHighScore, run3Score)

        // Best moves must STILL remain 4 (minimum), high score remains 1800
        assertEquals(4, currentBestMoves)
        assertEquals(1800, currentHighScore)
    }

    @Test
    fun testLeaderboardSortingOrder() {
        val list = listOf(
            ProgressEntity(levelId = "L003", isCompleted = true, bestMoves = 5, highScore = 1500),
            ProgressEntity(levelId = "L001", isCompleted = true, bestMoves = 3, highScore = 2800),
            ProgressEntity(levelId = "L002", isCompleted = true, bestMoves = 4, highScore = 2100)
        )

        // Sort by fewest moves
        val sortedByFewestMoves = list.sortedBy { it.bestMoves }
        assertEquals("L001", sortedByFewestMoves[0].levelId)
        assertEquals(3, sortedByFewestMoves[0].bestMoves)
        assertEquals("L002", sortedByFewestMoves[1].levelId)
        assertEquals("L003", sortedByFewestMoves[2].levelId)

        // Sort by highest score
        val sortedByHighScore = list.sortedByDescending { it.highScore }
        assertEquals("L001", sortedByHighScore[0].levelId)
        assertEquals(2800, sortedByHighScore[0].highScore)
        assertEquals("L002", sortedByHighScore[1].levelId)
        assertEquals("L003", sortedByHighScore[2].levelId)
    }

    @Test
    fun testParEfficiencyCalculation() {
        val par = 4
        val actualMoves = 3
        val delta = actualMoves - par
        assertTrue("Under par moves should have negative delta", delta < 0)
        assertEquals(-1, delta)
    }
}
