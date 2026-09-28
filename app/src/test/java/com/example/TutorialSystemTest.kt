package com.example

import com.example.model.TutorialStep
import com.example.model.TutorialTargetSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialSystemTest {

    @Test
    fun testTutorialStepsInStrictSequentialOrder() {
        val steps = TutorialStep.entries.toList()
        assertEquals(4, steps.size)

        assertEquals(TutorialStep.TAP_TO_MOVE, steps[0])
        assertEquals(TutorialStep.BLOCKED_MOVE, steps[1])
        assertEquals(TutorialStep.BOARDING, steps[2])
        assertEquals(TutorialStep.FULL_SLOTS, steps[3])

        // Verify step indices and total count
        assertEquals(1, TutorialStep.TAP_TO_MOVE.stepIndex)
        assertEquals(2, TutorialStep.BLOCKED_MOVE.stepIndex)
        assertEquals(3, TutorialStep.BOARDING.stepIndex)
        assertEquals(4, TutorialStep.FULL_SLOTS.stepIndex)

        steps.forEach { step ->
            assertEquals(4, step.totalSteps)
            assertTrue("Title should be non-empty", step.title.isNotBlank())
            assertTrue("Subtitle should be non-empty", step.subtitle.isNotBlank())
            assertTrue("Message should be non-empty", step.message.isNotBlank())
            assertTrue("Mate tip should be non-empty", step.mateTip.isNotBlank())
        }
    }

    @Test
    fun testTutorialNextStepProgression() {
        assertEquals(TutorialStep.BLOCKED_MOVE, TutorialStep.TAP_TO_MOVE.nextStep())
        assertEquals(TutorialStep.BOARDING, TutorialStep.BLOCKED_MOVE.nextStep())
        assertEquals(TutorialStep.FULL_SLOTS, TutorialStep.BOARDING.nextStep())
        assertNull(TutorialStep.FULL_SLOTS.nextStep())
    }

    @Test
    fun testTutorialTargetSectionsAndVehicles() {
        // Step 1: Tap to Move targets the car park and v1 (clear path)
        assertEquals(TutorialTargetSection.CAR_PARK, TutorialStep.TAP_TO_MOVE.targetSection)
        assertEquals("v1", TutorialStep.TAP_TO_MOVE.targetVehicleId)
        assertNotNull(TutorialStep.TAP_TO_MOVE.targetBadge)

        // Step 2: Blocked move targets the car park and v2 (blocking vehicle to clear)
        assertEquals(TutorialTargetSection.CAR_PARK, TutorialStep.BLOCKED_MOVE.targetSection)
        assertEquals("v2", TutorialStep.BLOCKED_MOVE.targetVehicleId)
        assertNotNull(TutorialStep.BLOCKED_MOVE.targetBadge)

        // Step 3: Boarding targets the passenger queue & gate
        assertEquals(TutorialTargetSection.QUEUE, TutorialStep.BOARDING.targetSection)

        // Step 4: Full slots targets the parking bay row
        assertEquals(TutorialTargetSection.SLOTS, TutorialStep.FULL_SLOTS.targetSection)
    }
}
