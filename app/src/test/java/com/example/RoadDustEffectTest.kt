package com.example

import com.example.model.Direction
import com.example.ui.components.GridBurstData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadDustEffectTest {

    @Test
    fun testGridBurstDataProperties() {
        val now = System.currentTimeMillis()
        val burst = GridBurstData(
            row = 2,
            col = 3,
            direction = Direction.UP,
            timestamp = now
        )

        assertEquals(2, burst.row)
        assertEquals(3, burst.col)
        assertEquals(Direction.UP, burst.direction)
        assertEquals(now, burst.timestamp)
    }

    @Test
    fun testGridBurstAcrossAllDirections() {
        val directions = Direction.entries.toList()
        assertEquals(4, directions.size)

        directions.forEachIndexed { index, dir ->
            val burst = GridBurstData(
                row = index,
                col = index + 1,
                direction = dir,
                timestamp = 1000L + index
            )
            assertEquals(dir, burst.direction)
            assertTrue("Burst row must be valid", burst.row >= 0)
            assertTrue("Burst col must be valid", burst.col >= 0)
        }
    }

    @Test
    fun testDustTriggerTimestampDifference() {
        val start = System.currentTimeMillis()
        val burst = GridBurstData(row = 1, col = 1, direction = Direction.RIGHT, timestamp = start)
        assertNotNull(burst)
        assertTrue(burst.timestamp > 0)
    }
}
