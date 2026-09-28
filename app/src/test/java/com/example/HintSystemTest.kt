package com.example

import com.example.engine.HintResult
import com.example.engine.HintSystem
import com.example.engine.RulesEngine
import com.example.levels.LevelRepository
import com.example.model.Axis
import com.example.model.Colour
import com.example.model.Direction
import com.example.model.EngineState
import com.example.model.GameStatus
import com.example.model.Vehicle
import com.example.model.VehicleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HintSystemTest {

    @Test
    fun testHintOnInitialStateL001() {
        val l1 = LevelRepository.levels.first { it.id == "L001" }
        val initialState = RulesEngine.createState(l1)

        val hint = HintSystem.getHint(initialState)
        assertTrue("Hint should be Success", hint is HintResult.Success)

        val success = hint as HintResult.Success
        assertEquals("v1", success.nextVehicleId)
        assertEquals(3, success.totalMovesRemaining)
        assertEquals(listOf("v1", "v2", "v3"), success.winningSequence)
        assertNotNull(success.nextVehicle)
        assertEquals(Colour.RED, success.nextVehicle.colour)
        assertTrue("Reason should describe action", success.reason.isNotEmpty())
        assertTrue("Exploration count should be positive", success.statesExplored > 0)
    }

    @Test
    fun testHintOnMidGameStateL001() {
        val l1 = LevelRepository.levels.first { it.id == "L001" }
        val initialState = RulesEngine.createState(l1)

        // Step 1: execute first optimal move v1
        val (s1, _) = RulesEngine.applyAction(initialState, "v1")
        assertEquals(GameStatus.PLAYING, s1.status)

        // Request hint from mid-game state s1
        val hintMid = HintSystem.getHint(s1)
        assertTrue("Hint should be Success", hintMid is HintResult.Success)

        val success = hintMid as HintResult.Success
        assertEquals("v2", success.nextVehicleId)
        assertEquals(2, success.totalMovesRemaining)
        assertEquals(listOf("v2", "v3"), success.winningSequence)
        assertEquals(Colour.BLUE, success.nextVehicle.colour)
    }

    @Test
    fun testHintOnNearWinStateL001() {
        val l1 = LevelRepository.levels.first { it.id == "L001" }
        val initialState = RulesEngine.createState(l1)

        val (s1, _) = RulesEngine.applyAction(initialState, "v1")
        val (s2, _) = RulesEngine.applyAction(s1, "v2")

        val hintNearWin = HintSystem.getHint(s2)
        assertTrue(hintNearWin is HintResult.Success)

        val success = hintNearWin as HintResult.Success
        assertEquals("v3", success.nextVehicleId)
        assertEquals(1, success.totalMovesRemaining)
        assertEquals(listOf("v3"), success.winningSequence)
        assertEquals(Colour.YELLOW, success.nextVehicle.colour)
    }

    @Test
    fun testHintOnWonStateReturnsAlreadyWon() {
        val l1 = LevelRepository.levels.first { it.id == "L001" }
        val initialState = RulesEngine.createState(l1)

        val (s1, _) = RulesEngine.applyAction(initialState, "v1")
        val (s2, _) = RulesEngine.applyAction(s1, "v2")
        val (s3, _) = RulesEngine.applyAction(s2, "v3")

        assertEquals(GameStatus.WON, s3.status)
        val hintWon = HintSystem.getHint(s3)
        assertEquals(HintResult.AlreadyWon, hintWon)
    }

    @Test
    fun testHintOnDeadlockDetectsGridlock() {
        // Construct a state where slots are completely full of RED vehicles,
        // but the passenger queue is looking for BLUE passengers.
        val redTrotro = Vehicle(
            id = "parked_red",
            type = VehicleType.CAR,
            colour = Colour.RED,
            row = 0,
            col = 0,
            length = 2,
            axis = Axis.HORIZONTAL,
            direction = Direction.RIGHT,
            seats = 2,
            boarded = 2 // full
        )

        val deadlockedState = EngineState(
            rows = 6,
            cols = 6,
            carPark = emptyList(),
            slots = listOf(redTrotro, redTrotro), // 0 free slots
            queue = listOf(Colour.BLUE, Colour.YELLOW), // Queue requires blue
            moves = 2,
            status = GameStatus.PLAYING
        )

        val hint = HintSystem.getHint(deadlockedState)
        assertTrue("Should detect deadlock when slots full without matching passenger", hint is HintResult.Deadlock)

        val deadlock = hint as HintResult.Deadlock
        assertTrue(deadlock.canUndo)
        assertTrue(deadlock.isSlotsFull)
        assertTrue(deadlock.message.contains("Undo") || deadlock.message.contains("occupied"))
    }
}
