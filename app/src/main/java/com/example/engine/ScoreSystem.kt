package com.example.engine

data class ScoreBreakdown(
    val baseClearPoints: Int,
    val moveEfficiencyBonus: Int,
    val speedBonus: Int,
    val passengerBonus: Int,
    val perfectParBonus: Int,
    val totalScore: Int,
    val targetSeconds: Int,
    val elapsedSeconds: Int,
    val moves: Int,
    val par: Int,
    val isNewHighScore: Boolean = false,
    val speedRating: String = "Good"
)

object ScoreSystem {

    private const val BASE_WIN_POINTS = 1000
    private const val BASE_MOVE_BONUS = 1500
    private const val MOVE_PENALTY_PER_EXCESS = 150
    private const val PERFECT_PAR_BONUS = 500
    private const val POINTS_PER_PASSENGER = 50
    private const val POINTS_PER_DEPARTED_TROTRO = 150
    private const val BASE_SPEED_BONUS = 1000
    private const val SPEED_POINTS_PER_SECOND_AHEAD = 30
    private const val SPEED_PENALTY_PER_SECOND_LATE = 20

    /**
     * Target seconds allowed to earn maximum speed points for a given par.
     */
    fun calculateTargetSeconds(par: Int): Int {
        return maxOf(25, par * 9)
    }

    /**
     * Speed rating label based on speed ratio compared to target.
     */
    fun getSpeedRating(elapsedSeconds: Int, targetSeconds: Int): String {
        return when {
            elapsedSeconds <= (targetSeconds * 0.5).toInt() -> "⚡ LIGHTNING SPEED"
            elapsedSeconds <= (targetSeconds * 0.75).toInt() -> "🔥 QUICK RUSH"
            elapsedSeconds <= targetSeconds -> "⏱️ ON TIME"
            elapsedSeconds <= (targetSeconds * 1.5).toInt() -> "🐢 SLOW RUSH"
            else -> "⏳ GRIDLOCK DELAY"
        }
    }

    /**
     * Calculates move efficiency bonus.
     * Rewards exact Par or Under-Par solutions generously, and scales down for excess moves.
     */
    fun calculateMoveBonus(moves: Int, par: Int): Int {
        if (moves <= par) {
            return BASE_MOVE_BONUS
        }
        val excessMoves = moves - par
        return maxOf(100, BASE_MOVE_BONUS - excessMoves * MOVE_PENALTY_PER_EXCESS)
    }

    /**
     * Calculates speed clearance bonus.
     * Faster clearance under target time awards significant bonus points.
     */
    fun calculateSpeedBonus(elapsedSeconds: Int, targetSeconds: Int): Int {
        return if (elapsedSeconds <= targetSeconds) {
            val secondsAhead = targetSeconds - elapsedSeconds
            BASE_SPEED_BONUS + secondsAhead * SPEED_POINTS_PER_SECOND_AHEAD
        } else {
            val secondsLate = elapsedSeconds - targetSeconds
            maxOf(100, BASE_SPEED_BONUS - secondsLate * SPEED_PENALTY_PER_SECOND_LATE)
        }
    }

    /**
     * Live dynamic score displayed at the top of the screen during gameplay.
     * Increases with passenger boarding and departures, and reflects live move & speed efficiency.
     */
    fun calculateLiveScore(
        moves: Int,
        par: Int,
        elapsedSeconds: Int,
        totalPassengers: Int,
        passengersRemaining: Int,
        trotrosDeparted: Int
    ): Int {
        val targetSeconds = calculateTargetSeconds(par)
        val boardedPassengers = (totalPassengers - passengersRemaining).coerceAtLeast(0)
        val boardingPoints = boardedPassengers * POINTS_PER_PASSENGER
        val departedPoints = trotrosDeparted * POINTS_PER_DEPARTED_TROTRO

        // Current move points potential
        val moveBonus = calculateMoveBonus(moves, par)

        // Current speed points potential
        val speedBonus = calculateSpeedBonus(elapsedSeconds, targetSeconds)

        // Combine weighted live metrics:
        // As you make progress, score accumulates visibly
        val progressScore = boardingPoints + departedPoints
        val efficiencyFactor = (moveBonus + speedBonus) / 2

        return maxOf(0, progressScore + efficiencyFactor)
    }

    /**
     * Final score calculation when a level is cleared.
     */
    fun calculateFinalScore(
        moves: Int,
        par: Int,
        elapsedSeconds: Int,
        totalPassengers: Int,
        existingHighScore: Int
    ): ScoreBreakdown {
        val targetSeconds = calculateTargetSeconds(par)
        val moveBonus = calculateMoveBonus(moves, par)
        val speedBonus = calculateSpeedBonus(elapsedSeconds, targetSeconds)
        val parBonus = if (moves <= par) PERFECT_PAR_BONUS else 0
        val passengerBonus = totalPassengers * POINTS_PER_PASSENGER
        val totalScore = BASE_WIN_POINTS + moveBonus + speedBonus + parBonus + passengerBonus
        val isNewHighScore = totalScore > existingHighScore

        return ScoreBreakdown(
            baseClearPoints = BASE_WIN_POINTS,
            moveEfficiencyBonus = moveBonus,
            speedBonus = speedBonus,
            passengerBonus = passengerBonus,
            perfectParBonus = parBonus,
            totalScore = totalScore,
            targetSeconds = targetSeconds,
            elapsedSeconds = elapsedSeconds,
            moves = moves,
            par = par,
            isNewHighScore = isNewHighScore,
            speedRating = getSpeedRating(elapsedSeconds, targetSeconds)
        )
    }

    /**
     * Helper to format seconds into MM:SS format.
     */
    fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
