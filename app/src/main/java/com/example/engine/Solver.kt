package com.example.engine

import com.example.model.EngineState
import com.example.model.GameStatus
import com.example.model.LevelData
import java.util.ArrayDeque

data class SolveResult(
    val isSolvable: Boolean,
    val par: Int = 0,
    val winningSequence: List<String> = emptyList(),
    val statesExplored: Int = 0,
    val maxBranching: Int = 0,
    val executionTimeMs: Long = 0
)

object Solver {

    data class QueueNode(
        val state: EngineState,
        val history: List<String>
    )

    /**
     * Solves a level using Breadth-First Search (BFS) to guarantee the shortest winning sequence (Par).
     */
    fun solve(level: LevelData, maxStates: Int = 50_000, timeLimitMs: Long = 10_000): SolveResult {
        val startTime = System.currentTimeMillis()
        val initialState = RulesEngine.createState(level)

        if (initialState.status == GameStatus.WON) {
            return SolveResult(
                isSolvable = true,
                par = 0,
                winningSequence = emptyList(),
                statesExplored = 1,
                maxBranching = 0,
                executionTimeMs = System.currentTimeMillis() - startTime
            )
        }

        val visited = HashSet<String>()
        val queue = ArrayDeque<QueueNode>()

        val initialHash = RulesEngine.hash(initialState)
        visited.add(initialHash)
        queue.add(QueueNode(initialState, emptyList()))

        var statesExplored = 0
        var maxBranching = 0

        while (queue.isNotEmpty()) {
            // Check timeout or excessive memory/state count
            if (System.currentTimeMillis() - startTime > timeLimitMs || statesExplored >= maxStates) {
                break
            }

            val current = queue.poll() ?: break
            statesExplored++

            val legalActions = RulesEngine.legalActions(current.state)
            if (legalActions.size > maxBranching) {
                maxBranching = legalActions.size
            }

            for (actionVehicleId in legalActions) {
                val (nextState, _) = RulesEngine.applyAction(current.state, actionVehicleId)
                val newHistory = current.history + actionVehicleId

                if (nextState.status == GameStatus.WON) {
                    return SolveResult(
                        isSolvable = true,
                        par = newHistory.size,
                        winningSequence = newHistory,
                        statesExplored = statesExplored,
                        maxBranching = maxBranching,
                        executionTimeMs = System.currentTimeMillis() - startTime
                    )
                }

                if (nextState.status == GameStatus.PLAYING) {
                    val stateHash = RulesEngine.hash(nextState)
                    if (visited.add(stateHash)) {
                        queue.add(QueueNode(nextState, newHistory))
                    }
                }
            }
        }

        return SolveResult(
            isSolvable = false,
            par = 0,
            winningSequence = emptyList(),
            statesExplored = statesExplored,
            maxBranching = maxBranching,
            executionTimeMs = System.currentTimeMillis() - startTime
        )
    }
}
