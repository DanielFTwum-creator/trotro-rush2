package com.example.audio

/**
 * Backward-compatible facade forwarding to [TrotroSoundManager].
 */
object SoundPlayer {
    var isMuted: Boolean
        get() = TrotroSoundManager.isMuted
        set(value) {
            TrotroSoundManager.isMuted = value
        }

    fun playClick() = TrotroSoundManager.playClick()
    fun playMove() = TrotroSoundManager.playMove()
    fun playBlocked() = TrotroSoundManager.playBlocked()
    fun playNoSlot() = TrotroSoundManager.playNoSlot()
    fun playBoard() = TrotroSoundManager.playBoard()
    fun playDepart() = TrotroSoundManager.playDepart()
    fun playWin() = TrotroSoundManager.playWin()
    fun playLose() = TrotroSoundManager.playLose()

    // New Trotro Audio Effects
    fun playEngineRev(durationMs: Int = 320, amplitude: Double = 0.55) =
        TrotroSoundManager.playEngineRev(durationMs, amplitude)

    fun playBrakeScreech(durationMs: Int = 280, amplitude: Double = 0.45) =
        TrotroSoundManager.playBrakeScreech(durationMs, amplitude)

    fun playHornHonk(pattern: HornPattern = HornPattern.DOUBLE) =
        TrotroSoundManager.playHornHonk(pattern)

    fun playVehicleMove(isBlocked: Boolean = false) =
        TrotroSoundManager.playVehicleMove(isBlocked)
}
