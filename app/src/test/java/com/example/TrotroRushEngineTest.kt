package com.example

import com.example.engine.RulesEngine
import com.example.engine.Solver
import com.example.levels.LevelRepository
import com.example.model.Axis
import com.example.model.Colour
import com.example.model.Direction
import com.example.model.EngineEvent
import com.example.model.GameStatus
import com.example.model.LevelData
import com.example.model.Vehicle
import com.example.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrotroRushEngineTest {

    @Test
    fun testL001WorkedExampleWalkthrough() {
        // Load Worked Example Level L001 from Appendix D.2 of TUC-ICT-SRS-2026-030
        val l1 = LevelRepository.levels.first()
        assertEquals("L001", l1.id)
        assertEquals(3, l1.par)

        var state = RulesEngine.createState(l1)
        assertEquals(3, state.carPark.size)
        assertEquals(4, state.slots.size)
        assertEquals(14, state.queue.size)
        assertEquals(0, state.moves)
        assertEquals(GameStatus.PLAYING, state.status)

        // Step 1: v1 (red car, arrow UP) has clear path -> should succeed
        val (s1, events1) = RulesEngine.applyAction(state, "v1")
        assertEquals(1, s1.moves)
        assertTrue(events1.any { it is EngineEvent.Moved })
        // red, red should board
        assertTrue(events1.any { it is EngineEvent.Boarded })
        assertEquals(12, s1.queue.size) // 14 - 2 = 12

        // Step 2: v3 (yellow car, arrow DOWN) is blocked by v2 (blue minibus)
        val (s2, events2) = RulesEngine.applyAction(s1, "v3")
        assertEquals(1, s2.moves) // no state change
        assertTrue(events2.any { it is EngineEvent.Blocked })
        assertEquals(12, s2.queue.size)

        // Step 3: v2 (blue minibus, arrow RIGHT) has clear path -> should exit
        val (s3, events3) = RulesEngine.applyAction(s1, "v2")
        assertEquals(2, s3.moves)
        assertTrue(events3.any { it is EngineEvent.Moved })
        // 3 blue passengers board v2
        assertTrue(events3.any { it is EngineEvent.Boarded })
        assertEquals(9, s3.queue.size) // 12 - 3 = 9

        // Step 4: Now v3 (yellow car) path is clear!
        val (s4, events4) = RulesEngine.applyAction(s3, "v3")
        assertEquals(3, s4.moves)
        // All remaining passengers cascade board and all vehicles fill and depart
        assertEquals(0, s4.queue.size)
        assertEquals(0, s4.carPark.size)
        assertEquals(GameStatus.WON, s4.status)
        assertTrue(events4.any { it is EngineEvent.Won })
    }

    @Test
    fun testSolverCalculatesExactPar3() {
        val l1 = LevelRepository.levels.first()
        val result = Solver.solve(l1)
        assertTrue(result.isSolvable)
        assertEquals(3, result.par)
        assertTrue(result.statesExplored > 0)
    }

    @Test
    fun testAll40LevelsSatisfyColourCountRule() {
        // REQ-LVL-003: For every colour, passengers in queue == total seats of vehicles of that colour
        for (level in LevelRepository.levels) {
            for (colour in level.colours) {
                val totalSeats = level.vehicles.filter { it.colour == colour }.sumOf { it.seats }
                val totalPassengers = level.queue.count { it == colour }
                assertEquals(
                    "Level ${level.id} colour ${colour.name} passenger count must equal vehicle seat count",
                    totalSeats,
                    totalPassengers
                )
            }
        }
    }

    @Test
    fun testAll40LevelsHaveNonOverlappingVehiclesWithinBounds() {
        for (level in LevelRepository.levels) {
            val occupied = mutableSetOf<Pair<Int, Int>>()
            for (v in level.vehicles) {
                val cells = v.occupiedCells()
                for ((r, c) in cells) {
                    assertTrue(
                        "Level ${level.id} vehicle ${v.id} cell ($r, $c) must be within grid rows [0, ${level.gridRows})",
                        r in 0 until level.gridRows
                    )
                    assertTrue(
                        "Level ${level.id} vehicle ${v.id} cell ($r, $c) must be within grid cols [0, ${level.gridCols})",
                        c in 0 until level.gridCols
                    )
                    assertTrue(
                        "Level ${level.id} vehicle ${v.id} overlaps with another vehicle at ($r, $c)!",
                        occupied.add(Pair(r, c))
                    )
                }
            }
        }
    }

    @Test
    fun testProgressiveLevelDifficultyTiers() {
        val l1 = LevelRepository.getLevelById("L001")!!
        val l10 = LevelRepository.getLevelById("L010")!!
        val l20 = LevelRepository.getLevelById("L020")!!
        val l30 = LevelRepository.getLevelById("L030")!!
        val l40 = LevelRepository.getLevelById("L040")!!

        assertEquals(com.example.model.LevelTier.BEGINNER, l1.difficultyTier)
        assertEquals(com.example.model.LevelTier.BEGINNER, l10.difficultyTier)
        assertEquals(com.example.model.LevelTier.INTERMEDIATE, l20.difficultyTier)
        assertEquals(com.example.model.LevelTier.ADVANCED, l30.difficultyTier)
        assertEquals(com.example.model.LevelTier.EXPERT, l40.difficultyTier)

        assertTrue("Expert levels must have more vehicles than Beginner levels", l40.vehicles.size > l1.vehicles.size)
        assertTrue("Expert levels must have larger or equal grid dimensions", l40.gridRows >= l1.gridRows)
    }

    @Test
    fun testNoFreeSlotsBlocksMovement() {
        // Create fixture with 1 slot and a vehicle parked, but queue still able to board so game is PLAYING
        val v1 = Vehicle("v1", VehicleType.CAR, Colour.BLUE, row = 0, col = 0, length = 2, axis = Axis.HORIZONTAL, direction = Direction.RIGHT, seats = 4)
        val v2 = Vehicle("v2", VehicleType.CAR, Colour.RED, row = 2, col = 0, length = 2, axis = Axis.HORIZONTAL, direction = Direction.RIGHT, seats = 4)
        val fixture = LevelData(
            id = "F001",
            name = "Full Slot Fixture",
            gridCols = 6,
            gridRows = 6,
            slots = 1,
            colours = listOf(Colour.RED, Colour.BLUE),
            vehicles = listOf(v1, v2),
            queue = listOf(Colour.BLUE, Colour.RED), // Head is BLUE, so v1 in slot can board, keeping game PLAYING
            par = 2
        )

        val initialState = RulesEngine.createState(fixture)
        // Manually place v1 into the only slot with 1/4 boarded
        val stateWithSlotFull = initialState.copy(
            carPark = listOf(v2),
            slots = listOf(v1.copy(boarded = 1)),
            queue = listOf(Colour.BLUE, Colour.RED),
            status = GameStatus.PLAYING
        )
        assertEquals(0, stateWithSlotFull.freeSlotsCount)
        assertEquals(GameStatus.PLAYING, RulesEngine.evaluateStatus(stateWithSlotFull))

        // Now attempting to move v2 must produce NoFreeSlot because all slots are occupied
        val (s2, events2) = RulesEngine.applyAction(stateWithSlotFull, "v2")
        assertEquals(0, s2.moves)
        assertTrue(events2.any { it is EngineEvent.NoFreeSlot })
    }

    @Test
    fun testEngineDeterminismAndImmutability() {
        val l1 = LevelRepository.levels.first()
        val s0 = RulesEngine.createState(l1)
        val hash0 = RulesEngine.hash(s0)

        val (s1, _) = RulesEngine.applyAction(s0, "v1")
        // s0 must remain completely untouched
        assertEquals(hash0, RulesEngine.hash(s0))
        assertEquals(0, s0.moves)
        assertEquals(1, s1.moves)
    }

    @Test
    fun testUndoRevertsMoveCountAndGameState() {
        val l1 = LevelRepository.levels.first()
        val s0 = RulesEngine.createState(l1)
        val initialCarParkSize = s0.carPark.size
        val initialQueueSize = s0.queue.size
        assertEquals(0, s0.moves)

        // Stack to simulate user moves
        val undoStack = mutableListOf<com.example.model.EngineState>()

        // Move 1
        undoStack.add(s0)
        val (s1, _) = RulesEngine.applyAction(s0, "v1")
        assertEquals(1, s1.moves)
        assertEquals(initialCarParkSize - 1, s1.carPark.size)

        // Move 2
        undoStack.add(s1)
        val (s2, _) = RulesEngine.applyAction(s1, "v2")
        assertEquals(2, s2.moves)
        assertEquals(initialCarParkSize - 2, s2.carPark.size)

        // Undo Move 2: pop from stack and restore state
        val restored1 = undoStack.removeAt(undoStack.lastIndex)
        assertEquals(1, restored1.moves)
        assertEquals(s1.moves, restored1.moves)
        assertEquals(s1.carPark.size, restored1.carPark.size)
        assertEquals(s1.slots.size, restored1.slots.size)
        assertEquals(s1.queue.size, restored1.queue.size)

        // Undo Move 1: pop from stack and restore initial state
        val restored0 = undoStack.removeAt(undoStack.lastIndex)
        assertEquals(0, restored0.moves)
        assertEquals(initialCarParkSize, restored0.carPark.size)
        assertEquals(initialQueueSize, restored0.queue.size)
        assertTrue(undoStack.isEmpty())
    }
}
