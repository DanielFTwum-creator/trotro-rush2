package com.example.engine

import com.example.model.Direction
import com.example.model.EngineState
import com.example.model.GameStatus
import com.example.model.Vehicle

sealed class HintResult {
    data class Success(
        val nextVehicleId: String,
        val nextVehicle: Vehicle,
        val totalMovesRemaining: Int,
        val winningSequence: List<String>,
        val reason: String,
        val statesExplored: Int,
        val executionTimeMs: Long
    ) : HintResult()

    data class Deadlock(
        val title: String,
        val message: String,
        val canUndo: Boolean,
        val isSlotsFull: Boolean,
        val statesExplored: Int
    ) : HintResult()

    data class NoSolutionFound(
        val message: String,
        val statesExplored: Int,
        val executionTimeMs: Long
    ) : HintResult()

    object AlreadyWon : HintResult()
}

object HintSystem {

    /**
     * Analyzes the current game state using the Breadth-First-Search CI solver
     * to provide the exact optimal next trotro to move and explain why.
     */
    fun getHint(
        currentState: EngineState,
        maxStates: Int = 35_000,
        timeLimitMs: Long = 3_000
    ): HintResult {
        if (currentState.status == GameStatus.WON) {
            return HintResult.AlreadyWon
        }

        if (currentState.status == GameStatus.LOST) {
            return HintResult.Deadlock(
                title = "Station Gridlock",
                message = "The parking bays are full and unable to board waiting passengers. Tap Undo to reverse your last moves or Restart.",
                canUndo = true,
                isSlotsFull = currentState.freeSlotsCount == 0,
                statesExplored = 0
            )
        }

        // Fast-path deadlock check:
        // If all bays are occupied and none match the waiting front passenger
        if (currentState.freeSlotsCount == 0 && currentState.queue.isNotEmpty()) {
            val nextPassenger = currentState.queue.first()
            val anySlotMatches = currentState.slots.any { it != null && it.colour == nextPassenger }
            if (!anySlotMatches) {
                return HintResult.Deadlock(
                    title = "Parking Bays Locked",
                    message = "All parking bays are occupied, and none match the waiting ${nextPassenger.displayName} passenger at the gate. Tap Undo to back up!",
                    canUndo = true,
                    isSlotsFull = true,
                    statesExplored = 0
                )
            }
        }

        val solveResult = Solver.solveFromState(
            currentState = currentState,
            maxStates = maxStates,
            timeLimitMs = timeLimitMs
        )

        if (solveResult.isSolvable && solveResult.winningSequence.isNotEmpty()) {
            val nextVehicleId = solveResult.winningSequence.first()
            val nextVehicle = currentState.carPark.find { it.id == nextVehicleId }
                ?: return HintResult.NoSolutionFound(
                    message = "Solver recommended trotro $nextVehicleId, but it was not found in car park.",
                    statesExplored = solveResult.statesExplored,
                    executionTimeMs = solveResult.executionTimeMs
                )

            val reason = generateHintReason(currentState, nextVehicle, solveResult.winningSequence)

            return HintResult.Success(
                nextVehicleId = nextVehicleId,
                nextVehicle = nextVehicle,
                totalMovesRemaining = solveResult.winningSequence.size,
                winningSequence = solveResult.winningSequence,
                reason = reason,
                statesExplored = solveResult.statesExplored,
                executionTimeMs = solveResult.executionTimeMs
            )
        }

        if (!solveResult.isSolvable) {
            // Check if deadlock or state budget reached
            val isLikelyDeadlock = currentState.freeSlotsCount == 0 ||
                    (solveResult.statesExplored < maxStates && solveResult.executionTimeMs < timeLimitMs)
            return if (isLikelyDeadlock) {
                HintResult.Deadlock(
                    title = "No Winning Path from Here",
                    message = "Current vehicle positions cannot reach the goal. Tap Undo to try a different move sequence, or Restart.",
                    canUndo = true,
                    isSlotsFull = currentState.freeSlotsCount == 0,
                    statesExplored = solveResult.statesExplored
                )
            } else {
                HintResult.NoSolutionFound(
                    message = "Solver reached search limit (${solveResult.statesExplored} states explored). Try clearing obvious exits first.",
                    statesExplored = solveResult.statesExplored,
                    executionTimeMs = solveResult.executionTimeMs
                )
            }
        }

        return HintResult.NoSolutionFound(
            message = "Unable to compute optimal hint at this moment.",
            statesExplored = solveResult.statesExplored,
            executionTimeMs = solveResult.executionTimeMs
        )
    }

    private fun generateHintReason(
        currentState: EngineState,
        vehicle: Vehicle,
        winningSequence: List<String>
    ): String {
        val headPassenger = currentState.queue.firstOrNull()
        val headingText = when (vehicle.direction) {
            Direction.UP -> "North (↑)"
            Direction.DOWN -> "South (↓)"
            Direction.LEFT -> "West (←)"
            Direction.RIGHT -> "East (→)"
        }

        return when {
            headPassenger != null && vehicle.colour == headPassenger -> {
                "Direct Match! Drive ${vehicle.colour.displayName} trotro ${headingText} into a bay to board the waiting ${headPassenger.displayName} passenger."
            }
            currentState.queue.any { it == vehicle.colour } -> {
                "Stage Trotro: Move ${vehicle.colour.displayName} trotro ${headingText} into an open bay ready for upcoming passengers."
            }
            winningSequence.size > 1 -> {
                "Clear Corridor: Drive ${vehicle.colour.displayName} trotro ${headingText} to unblock traffic for other vehicles."
            }
            else -> {
                "Final Exit: Drive ${vehicle.colour.displayName} trotro ${headingText} to complete the route!"
            }
        }
    }
}
