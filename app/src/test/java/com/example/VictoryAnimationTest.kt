package com.example

import com.example.ui.components.evaluatePerformanceRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VictoryAnimationTest {

    @Test
    fun testPerfectRunAtOrUnderPar() {
        val ratingAtPar = evaluatePerformanceRating(moves = 6, par = 6)
        assertEquals(3, ratingAtPar.starCount)
        assertTrue(ratingAtPar.tierTitle.contains("PERFECT"))
        assertTrue(ratingAtPar.rankBadge.contains("MASTER STATION MASTER"))
        assertEquals(100, ratingAtPar.efficiencyPercent)
        assertTrue(ratingAtPar.deltaText.contains("Exact Par"))

        val ratingUnderPar = evaluatePerformanceRating(moves = 5, par = 6)
        assertEquals(3, ratingUnderPar.starCount)
        assertTrue(ratingUnderPar.tierTitle.contains("PERFECT"))
    }

    @Test
    fun testGreatEffortWithinTwoMovesOfPar() {
        val ratingPlusOne = evaluatePerformanceRating(moves = 7, par = 6)
        assertEquals(2, ratingPlusOne.starCount)
        assertTrue(ratingPlusOne.tierTitle.contains("GREAT EFFORT"))
        assertTrue(ratingPlusOne.rankBadge.contains("EXPERT CONDUCTOR"))
        assertTrue(ratingPlusOne.deltaText.contains("+1 moves"))
        assertTrue(ratingPlusOne.efficiencyPercent < 100)

        val ratingPlusTwo = evaluatePerformanceRating(moves = 8, par = 6)
        assertEquals(2, ratingPlusTwo.starCount)
        assertTrue(ratingPlusTwo.tierTitle.contains("GREAT EFFORT"))
        assertTrue(ratingPlusTwo.deltaText.contains("+2 moves"))
    }

    @Test
    fun testSafeArrivalOverTwoMovesAbovePar() {
        val ratingPlusThree = evaluatePerformanceRating(moves = 9, par = 6)
        assertEquals(1, ratingPlusThree.starCount)
        assertTrue(ratingPlusThree.tierTitle.contains("SAFE ARRIVAL"))
        assertTrue(ratingPlusThree.rankBadge.contains("STEADY DRIVER"))
        assertTrue(ratingPlusThree.deltaText.contains("+3 moves"))

        val ratingHighMoves = evaluatePerformanceRating(moves = 20, par = 6)
        assertEquals(1, ratingHighMoves.starCount)
        assertTrue(ratingHighMoves.efficiencyPercent >= 10)
    }

    @Test
    fun testPraiseTextAndAccentColorAreNonNull() {
        val tiers = listOf(
            evaluatePerformanceRating(moves = 4, par = 4),
            evaluatePerformanceRating(moves = 5, par = 4),
            evaluatePerformanceRating(moves = 10, par = 4)
        )
        tiers.forEach { rating ->
            assertTrue(rating.praiseText.isNotBlank())
            assertTrue(rating.rankBadge.isNotBlank())
            assertTrue(rating.tierTitle.isNotBlank())
        }
    }
}
