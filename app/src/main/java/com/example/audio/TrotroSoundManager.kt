package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

enum class HornPattern {
    SINGLE,
    DOUBLE,
    SHORT_BLAST
}

/**
 * Sound Manager responsible for all in-game sound effects including
 * engine revving, brake screeching, and horn honking.
 */
object TrotroSoundManager {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.Default)

    var isMuted: Boolean = false

    // ---------------------------------------------------------
    // Core Trotro Vehicle Sounds
    // ---------------------------------------------------------

    /**
     * Engine revving: Low roaring harmonic acceleration simulating a Ghanaian
     * diesel trotro minibus accelerating forward.
     */
    fun playEngineRev(durationMs: Int = 320, amplitude: Double = 0.55) {
        if (isMuted) return
        scope.launch {
            val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
            val buffer = ShortArray(numSamples)
            var phase = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                // Accelerate frequency from ~95Hz to ~240Hz
                val freq = 95.0 + 145.0 * sin(progress * PI * 0.9)
                phase += 2.0 * PI * freq / SAMPLE_RATE

                // Engine cylinder stroke pulse (18 Hz modulation)
                val pistonPulse = 0.72 + 0.28 * sin(2.0 * PI * 18.0 * (i.toDouble() / SAMPLE_RATE))

                // Fundamental + 2nd harmonic + subharmonic rumble
                val wave = sin(phase) + 0.42 * sin(phase * 2.0) + 0.28 * sin(phase * 0.5)

                // Attack-decay envelope
                val envelope = when {
                    progress < 0.12 -> progress / 0.12
                    progress > 0.70 -> (1.0 - progress) / 0.30
                    else -> 1.0
                }

                val clamped = (wave / 1.7 * amplitude * pistonPulse * envelope * Short.MAX_VALUE).toInt()
                buffer[i] = clamped.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAndPlay(buffer)
        }
    }

    /**
     * Brake screeching: High-pitched friction noise simulating tires and brake shoes
     * grabbing as a trotro stops suddenly or encounters an obstacle.
     */
    fun playBrakeScreech(durationMs: Int = 280, amplitude: Double = 0.45) {
        if (isMuted) return
        scope.launch {
            val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
            val buffer = ShortArray(numSamples)
            var phase1 = 0.0
            var phase2 = 0.0
            val random = Random(1337)

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val freq1 = 2650.0 - 450.0 * progress
                val freq2 = 3200.0 - 550.0 * progress
                val jitter = (random.nextDouble() - 0.5) * 60.0

                phase1 += 2.0 * PI * (freq1 + jitter) / SAMPLE_RATE
                phase2 += 2.0 * PI * (freq2 + jitter) / SAMPLE_RATE

                val wave = (sin(phase1) + sin(phase2)) * 0.5
                val envelope = when {
                    progress < 0.08 -> progress / 0.08
                    progress > 0.65 -> (1.0 - progress) / 0.35
                    else -> 1.0
                }

                val clamped = (wave * amplitude * envelope * Short.MAX_VALUE).toInt()
                buffer[i] = clamped.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAndPlay(buffer)
        }
    }

    /**
     * Horn honking: Iconic dual-tone automotive horn (Ghanaian trotro horn).
     */
    fun playHornHonk(pattern: HornPattern = HornPattern.DOUBLE) {
        if (isMuted) return
        scope.launch {
            when (pattern) {
                HornPattern.SINGLE -> {
                    playHornTone(durationMs = 180, amplitude = 0.5)
                }
                HornPattern.DOUBLE -> {
                    // Characteristic "Pip-Pip!" trotro sound
                    playHornTone(durationMs = 90, amplitude = 0.48)
                    delay(70)
                    playHornTone(durationMs = 110, amplitude = 0.52)
                }
                HornPattern.SHORT_BLAST -> {
                    playHornTone(durationMs = 240, amplitude = 0.55)
                }
            }
        }
    }

    /**
     * Compound trotro vehicle movement sound:
     * - Successful move: Engine revs up and trotro gives a cheerful honk!
     * - Blocked / obstacle: Engine starts, then screeches brakes abruptly with alert horn!
     */
    fun playVehicleMove(isBlocked: Boolean = false) {
        if (isMuted) return
        scope.launch {
            if (isBlocked) {
                // Engine rev start
                playEngineRev(durationMs = 140, amplitude = 0.5)
                delay(120)
                // Sudden brake screech
                playBrakeScreech(durationMs = 260, amplitude = 0.5)
                delay(180)
                // Alert warning horn blast
                playHornHonk(HornPattern.SHORT_BLAST)
            } else {
                // Satisfying acceleration rev
                playEngineRev(durationMs = 280, amplitude = 0.5)
                delay(200)
                // Cheerful arrival horn
                playHornHonk(HornPattern.DOUBLE)
            }
        }
    }

    // ---------------------------------------------------------
    // General Game Sound Effects
    // ---------------------------------------------------------

    fun playClick() {
        if (isMuted) return
        scope.launch {
            playTone(frequency = 700.0, durationMs = 30, amplitude = 0.3)
        }
    }

    fun playMove() {
        playVehicleMove(isBlocked = false)
    }

    fun playBlocked() {
        playVehicleMove(isBlocked = true)
    }

    fun playNoSlot() {
        if (isMuted) return
        scope.launch {
            playTone(frequency = 220.0, durationMs = 120, amplitude = 0.5)
        }
    }

    fun playBoard() {
        if (isMuted) return
        scope.launch {
            playTone(frequency = 880.0, durationMs = 60, amplitude = 0.35)
            delay(30)
            playTone(frequency = 1174.66, durationMs = 90, amplitude = 0.4)
        }
    }

    fun playDepart() {
        if (isMuted) return
        scope.launch {
            playEngineRev(durationMs = 350, amplitude = 0.55)
            delay(180)
            playHornHonk(HornPattern.DOUBLE)
        }
    }

    fun playWin() {
        if (isMuted) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
            for (freq in notes) {
                playTone(frequency = freq, durationMs = 110, amplitude = 0.45)
                delay(90)
            }
        }
    }

    fun playLose() {
        if (isMuted) return
        scope.launch {
            val notes = listOf(440.0, 392.0, 349.23, 293.66)
            for (freq in notes) {
                playTone(frequency = freq, durationMs = 100, amplitude = 0.4)
                delay(80)
            }
        }
    }

    // ---------------------------------------------------------
    // Tone Synthesizer Helpers
    // ---------------------------------------------------------

    private fun playHornTone(durationMs: Int, amplitude: Double) {
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val buffer = ShortArray(numSamples)
        // Authentic dual-tone automotive horn frequencies: ~440 Hz (A4) and ~554.37 Hz (C#5) + harmonic
        val f1 = 440.0
        val f2 = 554.37
        val f3 = 880.0

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val a1 = 2.0 * PI * f1 * (i.toDouble() / SAMPLE_RATE)
            val a2 = 2.0 * PI * f2 * (i.toDouble() / SAMPLE_RATE)
            val a3 = 2.0 * PI * f3 * (i.toDouble() / SAMPLE_RATE)

            val wave = sin(a1) * 0.52 + sin(a2) * 0.38 + sin(a3) * 0.10
            val envelope = when {
                progress < 0.06 -> progress / 0.06
                progress > 0.85 -> (1.0 - progress) / 0.15
                else -> 1.0
            }

            val clamped = (wave * amplitude * envelope * Short.MAX_VALUE).toInt()
            buffer[i] = clamped.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        writeAndPlay(buffer)
    }

    private fun playTone(frequency: Double, durationMs: Int, amplitude: Double) {
        val numSamples = (SAMPLE_RATE * durationMs / 1000.0).toInt()
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val angle = 2.0 * PI * i / (SAMPLE_RATE / frequency)
            val envelope = (1.0 - (i.toDouble() / numSamples))
            buffer[i] = (sin(angle) * amplitude * envelope * Short.MAX_VALUE).toInt().toShort()
        }
        writeAndPlay(buffer)
    }

    private fun writeAndPlay(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack?) {
                    t?.release()
                }
                override fun onPeriodicNotification(t: AudioTrack?) {}
            })
        } catch (_: Exception) {
            // Audio policy exception handled gracefully
        }
    }
}
