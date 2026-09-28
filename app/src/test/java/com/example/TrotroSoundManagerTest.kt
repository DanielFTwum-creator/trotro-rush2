package com.example

import com.example.audio.HornPattern
import com.example.audio.TrotroSoundManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrotroSoundManagerTest {

    @Test
    fun testSoundManagerMuteToggle() {
        TrotroSoundManager.isMuted = false
        assertFalse(TrotroSoundManager.isMuted)

        // Verify calling sound methods does not crash or throw exceptions
        TrotroSoundManager.playEngineRev()
        TrotroSoundManager.playBrakeScreech()
        TrotroSoundManager.playHornHonk(HornPattern.SINGLE)
        TrotroSoundManager.playHornHonk(HornPattern.DOUBLE)
        TrotroSoundManager.playHornHonk(HornPattern.SHORT_BLAST)
        TrotroSoundManager.playVehicleMove(isBlocked = false)
        TrotroSoundManager.playVehicleMove(isBlocked = true)

        TrotroSoundManager.isMuted = true
        assertTrue(TrotroSoundManager.isMuted)

        // When muted, calls should return safely without execution
        TrotroSoundManager.playEngineRev()
        TrotroSoundManager.playBrakeScreech()
        TrotroSoundManager.playHornHonk()
        TrotroSoundManager.playVehicleMove(isBlocked = false)

        TrotroSoundManager.isMuted = false
        assertFalse(TrotroSoundManager.isMuted)
    }

    @Test
    fun testHornPatternsExist() {
        val patterns = HornPattern.values()
        assertTrue(patterns.contains(HornPattern.SINGLE))
        assertTrue(patterns.contains(HornPattern.DOUBLE))
        assertTrue(patterns.contains(HornPattern.SHORT_BLAST))
    }
}
