package com.example

import com.example.audio.HapticFeedbackManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticFeedbackTest {

    @Test
    fun testHapticFeedbackToggle() {
        HapticFeedbackManager.isEnabled = true
        assertTrue(HapticFeedbackManager.isEnabled)

        // Ensure safe calls without crash even before init / without hardware vibrator
        HapticFeedbackManager.performMoveSuccess()
        HapticFeedbackManager.performObstacleHit()
        HapticFeedbackManager.performNoSlot()
        HapticFeedbackManager.performBoard()
        HapticFeedbackManager.performWin()

        HapticFeedbackManager.isEnabled = false
        assertFalse(HapticFeedbackManager.isEnabled)

        HapticFeedbackManager.performMoveSuccess()
        HapticFeedbackManager.performObstacleHit()

        // Re-enable
        HapticFeedbackManager.isEnabled = true
        assertTrue(HapticFeedbackManager.isEnabled)
    }
}
