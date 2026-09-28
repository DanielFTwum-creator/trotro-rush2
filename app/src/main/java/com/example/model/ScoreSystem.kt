package com.example.model

data class ScoreBreakdown(
    val baseClearPoints: Int,
    val moveEfficiencyBonus: Int,
    val speedClearanceBonus: Int,
    val passengerBoardingPoints: Int,
    val totalScore: Int,
    val targetSeconds: Int,
    val elapsedSeconds: Int,
    val isNewHighScore: Boolean = false
)

object ScoreSystem {
    const val BASE_CLEAR_POINTS = 1000
    const val PASSENGER_POINTS = 50
    const val DEPARTURE_BONUS = 100

    /**
     * Target speed in seconds to clear the station yard based on level Par.
     * E.g., Par 3 = 24 seconds, Par 5 = 40 seconds.
     */
    fun calculateTargetSeconds(par: Int): Int = (par * 8).coerceAtLeast(15)

    /**
     * Calculates move efficiency points based on actual moves vs level par.
     * Rewarding par and sub-par play with up to 2500+ points.
     */
    fun calculateMoveBonus(moves: Int, par: Int): Int {
        val diff = moves - par
        return if (diff <= 0) {
            2000 + (-diff) * 250
        } else {
            (2000 - diff * 150).coerceAtLeast(200)
        }
    }

    /**
     * Calculates speed bonus based on elapsed seconds vs target seconds.
     * Faster clearance awards a generous time bonus.
     */
    fun calculateSpeedBonus(elapsedSeconds: Int, par: Int): Int {
        val targetSeconds = calculateTargetSeconds(par)
        return if (elapsedSeconds <= targetSeconds) {
            1500 + (targetSeconds - elapsedSeconds) * 50
        } else {
            (1500 - (elapsedSeconds - targetSeconds) * 20).coerceAtLeast(100)
        }
    }

    /**
     * Real-time score displayed at the top of the screen during gameplay.
     */
    fun calculateLiveScore(
        passengersBoarded: Int,
        departedVehicles: Int,
        moves: Int,
        par: Int,
        elapsedSeconds: Int
    ): Int {
        val passengerPts = passengersBoarded * PASSENGER_POINTS + departedVehicles * DEPARTURE_BONUS
        val moveBonus = calculateMoveBonus(moves, par)
        val speedBonus = calculateSpeedBonus(elapsedSeconds, par)
        // Combine into live running total
        return BASE_CLEAR_POINTS + passengerPts + moveBonus + speedBonus
    }

    /**
     * Final score calculation upon winning the level.
     */
    fun calculateFinalScore(
        passengersBoarded: Int,
        departedVehicles: Int,
        moves: Int,
        par: Int,
        elapsedSeconds: Int,
        previousHighScore: Int
    ): ScoreBreakdown {
        val base = BASE_CLEAR_POINTS
        val moveBonus = calculateMoveBonus(moves, par)
        val speedBonus = calculateSpeedBonus(elapsedSeconds, par)
        val passengerPts = passengersBoarded * PASSENGER_POINTS + departedVehicles * DEPARTURE_BONUS
        val total = base + moveBonus + speedBonus + passengerPts
        val targetSeconds = calculateTargetSeconds(par)

        return ScoreBreakdown(
            baseClearPoints = base,
            moveEfficiencyBonus = moveBonus,
            speedClearanceBonus = speedBonus,
            passengerBoardingPoints = passengerPts,
            totalScore = total,
            targetSeconds = targetSeconds,
            elapsedSeconds = elapsedSeconds,
            isNewHighScore = total > previousHighScore
        )
    }

    /**
     * Formats seconds into MM:SS string.
     */
    fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
