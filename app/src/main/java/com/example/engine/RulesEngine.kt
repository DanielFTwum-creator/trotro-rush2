package com.example.engine

import com.example.model.Axis
import com.example.model.Colour
import com.example.model.Direction
import com.example.model.EngineEvent
import com.example.model.EngineState
import com.example.model.GameStatus
import com.example.model.LevelData
import com.example.model.Vehicle

object RulesEngine {

    fun createState(level: LevelData): EngineState {
        val initialSlots = List<Vehicle?>(level.slots) { null }
        var state = EngineState(
            rows = level.gridRows,
            cols = level.gridCols,
            carPark = level.vehicles,
            slots = initialSlots,
            queue = level.queue,
            moves = 0,
            status = GameStatus.PLAYING
        )
        // Perform initial boarding check if any vehicle was pre-placed in slots (none by default, but pure)
        val (finalState, _) = processBoardingCascade(state)
        return finalState
    }

    /**
     * Tests whether all cells between the vehicle's leading edge and the grid boundary
     * in the vehicle's direction are free of other vehicles.
     */
    fun testExitPath(state: EngineState, vehicle: Vehicle): Boolean {
        // Collect all occupied cells by OTHER vehicles on the car park grid
        val occupiedCells = HashSet<Pair<Int, Int>>()
        for (v in state.carPark) {
            if (v.id != vehicle.id) {
                occupiedCells.addAll(v.occupiedCells())
            }
        }

        when (vehicle.direction) {
            Direction.UP -> {
                // Leading edge is top row: row - 1 down to 0
                for (r in (vehicle.row - 1) downTo 0) {
                    if (occupiedCells.contains(Pair(r, vehicle.col))) return false
                }
            }
            Direction.DOWN -> {
                // Leading edge is row + length down to rows - 1
                val leadingRow = vehicle.row + vehicle.length
                for (r in leadingRow until state.rows) {
                    if (occupiedCells.contains(Pair(r, vehicle.col))) return false
                }
            }
            Direction.LEFT -> {
                // Leading edge is left col: col - 1 down to 0
                for (c in (vehicle.col - 1) downTo 0) {
                    if (occupiedCells.contains(Pair(vehicle.row, c))) return false
                }
            }
            Direction.RIGHT -> {
                // Leading edge is col + length to cols - 1
                val leadingCol = vehicle.col + vehicle.length
                for (c in leadingCol until state.cols) {
                    if (occupiedCells.contains(Pair(vehicle.row, c))) return false
                }
            }
        }
        return true
    }

    /**
     * Determines legal actions (list of vehicle IDs that can currently exit to a free slot).
     */
    fun legalActions(state: EngineState): List<String> {
        if (state.status != GameStatus.PLAYING) return emptyList()
        // Must have at least one free slot
        if (state.freeSlotsCount == 0) return emptyList()

        return state.carPark.filter { vehicle ->
            testExitPath(state, vehicle)
        }.map { it.id }
    }

    /**
     * Applies a vehicle exit action. Pure and deterministic: does not mutate inputs.
     */
    fun applyAction(state: EngineState, vehicleId: String): Pair<EngineState, List<EngineEvent>> {
        if (state.status != GameStatus.PLAYING) {
            return Pair(state, emptyList())
        }

        val vehicle = state.carPark.find { it.id == vehicleId }
            ?: return Pair(state, emptyList())

        val events = mutableListOf<EngineEvent>()

        // 1. Check if exit path is clear
        if (!testExitPath(state, vehicle)) {
            events.add(EngineEvent.Blocked(vehicleId))
            return Pair(state, events)
        }

        // 2. Check if a parking slot is free
        val freeSlotIndex = state.slots.indexOfFirst { it == null }
        if (freeSlotIndex == -1) {
            events.add(EngineEvent.NoFreeSlot)
            return Pair(state, events)
        }

        // 3. Move vehicle to parking slot (leftmost free slot, REQ-SLOT-002)
        val newCarPark = state.carPark.filter { it.id != vehicleId }
        val newSlots = state.slots.toMutableList()
        newSlots[freeSlotIndex] = vehicle
        events.add(EngineEvent.Moved(vehicleId, freeSlotIndex))

        val movedState = state.copy(
            carPark = newCarPark,
            slots = newSlots,
            moves = state.moves + 1
        )

        // 4. Run automatic boarding and departure cascade (REQ-QUEUE-002, REQ-QUEUE-003, REQ-SLOT-003)
        val (cascadedState, cascadeEvents) = processBoardingCascade(movedState)
        events.addAll(cascadeEvents)

        // 5. Check win / lose condition
        val finalStatus = evaluateStatus(cascadedState)
        val finalState = cascadedState.copy(status = finalStatus)

        if (finalStatus == GameStatus.WON && state.status != GameStatus.WON) {
            events.add(EngineEvent.Won)
        } else if (finalStatus == GameStatus.LOST && state.status != GameStatus.LOST) {
            events.add(EngineEvent.Lost)
        }

        return Pair(finalState, events)
    }

    /**
     * Boarding cascade:
     * While head passenger matches a parked vehicle with available seats:
     *  - Head boards leftmost matching vehicle.
     *  - If vehicle becomes full, it departs immediately, freeing its slot.
     * Continues until queue is empty or head passenger cannot board any parked vehicle.
     */
    fun processBoardingCascade(initialState: EngineState): Pair<EngineState, List<EngineEvent>> {
        var currentQueue = initialState.queue
        val currentSlots = initialState.slots.toMutableList()
        val events = mutableListOf<EngineEvent>()

        var progressMade = true
        while (progressMade && currentQueue.isNotEmpty()) {
            progressMade = false
            val headPassenger = currentQueue.first()

            // Find leftmost parked vehicle with matching colour and free seats
            val matchIndex = currentSlots.indexOfFirst { v ->
                v != null && v.colour == headPassenger && !v.isFull
            }

            if (matchIndex != -1) {
                progressMade = true
                val matchedVehicle = currentSlots[matchIndex]!!
                val updatedVehicle = matchedVehicle.copy(boarded = matchedVehicle.boarded + 1)
                currentSlots[matchIndex] = updatedVehicle
                currentQueue = currentQueue.drop(1)

                events.add(
                    EngineEvent.Boarded(
                        colour = headPassenger,
                        slotIndex = matchIndex,
                        remainingSeats = updatedVehicle.freeSeats
                    )
                )

                // If vehicle is full, it departs immediately (REQ-SLOT-003)
                if (updatedVehicle.isFull) {
                    currentSlots[matchIndex] = null
                    events.add(EngineEvent.Departed(updatedVehicle.id, matchIndex))
                }
            }
        }

        val nextState = initialState.copy(
            slots = currentSlots,
            queue = currentQueue
        )
        return Pair(nextState, events)
    }

    /**
     * Evaluate win and lose conditions:
     * - WON: Queue is empty
     * - LOST: Queue is not empty and no legal actions can be performed
     */
    fun evaluateStatus(state: EngineState): GameStatus {
        if (state.queue.isEmpty()) {
            return GameStatus.WON
        }

        // If slots are all full, and head passenger cannot board anything, check if any vehicle can exit
        val hasLegalMoves = legalActions(state).isNotEmpty()
        if (!hasLegalMoves) {
            // Cannot make any vehicle exit. Can any passenger board?
            val head = state.queue.first()
            val canBoard = state.slots.any { it != null && it.colour == head && !it.isFull }
            if (!canBoard) {
                return GameStatus.LOST
            }
        }

        return GameStatus.PLAYING
    }

    /**
     * Canonical hash of the state, excluding move count, so the solver can detect visited states.
     */
    fun hash(state: EngineState): String {
        val sb = StringBuilder()
        // Car park vehicles: sorted by ID for determinism
        state.carPark.sortedBy { it.id }.forEach { v ->
            sb.append(v.id).append(':').append(v.row).append(',').append(v.col).append(';')
        }
        sb.append('|')
        // Slots: index and parked vehicle ID and boarded count
        state.slots.forEachIndexed { idx, v ->
            if (v != null) {
                sb.append(idx).append(':').append(v.id).append('#').append(v.boarded).append(';')
            }
        }
        sb.append('|')
        // Queue: colour sequence
        state.queue.forEach { c ->
            sb.append(c.idName[0])
        }
        sb.append('|').append(state.status)
        return sb.toString()
    }
}
