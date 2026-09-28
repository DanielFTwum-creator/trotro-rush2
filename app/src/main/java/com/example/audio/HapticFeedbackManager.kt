package com.example.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticFeedbackManager {

    private var vibrator: Vibrator? = null
    var isEnabled: Boolean = true

    fun init(context: Context) {
        val appCtx = context.applicationContext
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appCtx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: (appCtx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            appCtx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Tactile feedback when a trotro successfully navigates its exit trajectory.
     * Crisp, satisfying pulse.
     */
    fun performMoveSuccess() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(35, 180))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback when a trotro hits an obstacle or is blocked by another vehicle.
     * Pronounced double-thump sensation conveying collision / obstruction.
     */
    fun performObstacleHit() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 40, 50, 45)
                val amplitudes = intArrayOf(0, 220, 0, 255)
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 40, 50, 45), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback when a parking bay is full (slots unavailable).
     */
    fun performNoSlot() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 40, 60)
                val amplitudes = intArrayOf(0, 180, 0, 180)
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 60, 40, 60), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Light tick when passengers board.
     */
    fun performBoard() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(18, 120))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    /**
     * Celebratory tactile pattern upon clearing the station.
     */
    fun performWin() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 60, 50, 60, 100)
                val amplitudes = intArrayOf(0, 160, 0, 200, 0, 255)
                v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(longArrayOf(0, 50, 60, 50, 60, 100), -1)
            }
        } catch (_: Exception) {}
    }
}
