package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ConfettiShape {
    RIBBON,
    CIRCLE,
    DIAMOND
}

private data class ConfettiParticle(
    val relX: Float, // Initial normalized X [0..1]
    val fallSpeed: Float, // Falling speed multiplier
    val flutterFreq: Float, // Frequency of lateral swaying
    val flutterAmplitude: Float, // Pixel amplitude of swaying
    val initialPhase: Float, // Phase offset
    val rotationSpeed: Float, // Spin speed in degrees per second
    val tumbleSpeed: Float, // 3D flip speed
    val widthPx: Float,
    val heightPx: Float,
    val color: Color,
    val shape: ConfettiShape
)

/**
 * End-of-level celebratory Confetti effect rendering vibrant, fluttering
 * paper ribbons and shapes celebrating the 'Trotro Arrived!' victory.
 *
 * @param modifier Layout modifier.
 * @param particleCount Number of confetti particles (default 65).
 * @param isReducedMotion When true, avoids motion for accessibility.
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 65,
    isReducedMotion: Boolean = false
) {
    if (isReducedMotion) return

    val confettiColors = remember {
        listOf(
            Color(0xFFFBBF24), // Ghanaian Gold
            Color(0xFFEF4444), // Crimson Red
            Color(0xFF10B981), // Emerald Green
            Color(0xFF38BDF8), // Trotro Sky Blue
            Color(0xFFA855F7), // Royal Purple
            Color(0xFFFB923C), // Sunset Orange
            Color(0xFFFFFFFF), // Crisp White
            Color(0xFFFCD34D)  // Bright Yellow
        )
    }

    val particles = remember(particleCount) {
        val rng = Random(42)
        List(particleCount) { i ->
            val shape = when (i % 5) {
                0, 1 -> ConfettiShape.RIBBON
                2, 3 -> ConfettiShape.CIRCLE
                else -> ConfettiShape.DIAMOND
            }
            ConfettiParticle(
                relX = rng.nextFloat(),
                fallSpeed = 0.55f + rng.nextFloat() * 0.75f,
                flutterFreq = 2.0f + rng.nextFloat() * 3.5f,
                flutterAmplitude = 18f + rng.nextFloat() * 26f,
                initialPhase = rng.nextFloat() * 6.28f,
                rotationSpeed = (if (rng.nextBoolean()) 1f else -1f) * (60f + rng.nextFloat() * 180f),
                tumbleSpeed = 3f + rng.nextFloat() * 6f,
                widthPx = 10f + rng.nextFloat() * 12f,
                heightPx = 14f + rng.nextFloat() * 14f,
                color = confettiColors[rng.nextInt(confettiColors.size)],
                shape = shape
            )
        }
    }

    // Continuous time accumulator for smooth floating confetti
    val animTime = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animTime.animateTo(
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 60000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    val t = animTime.value

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("victory_confetti_effect")
            .clearAndSetSemantics { /* Purely decorative visual effect */ }
    ) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            // Normalised cycle [0..1] with staggered starting offsets
            val cycle = ((t * p.fallSpeed + p.initialPhase) % 1.0f)
            // Y position falls from top (-50px) to bottom (h + 50px)
            val currentY = -40f + cycle * (h + 80f)

            // Horizontal sway
            val currentX = (p.relX * w) + sin(t * p.flutterFreq + p.initialPhase) * p.flutterAmplitude

            // 3D tumble flip factor [-1..1]
            val tumble = cos(t * p.tumbleSpeed + p.initialPhase)
            val effectiveHeight = p.heightPx * kotlin.math.abs(tumble).coerceAtLeast(0.15f)

            // Rotation angle
            val angle = (t * p.rotationSpeed) % 360f

            rotate(degrees = angle, pivot = Offset(currentX, currentY)) {
                when (p.shape) {
                    ConfettiShape.RIBBON -> {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(currentX - p.widthPx / 2f, currentY - effectiveHeight / 2f),
                            size = Size(p.widthPx, effectiveHeight)
                        )
                    }
                    ConfettiShape.CIRCLE -> {
                        drawCircle(
                            color = p.color,
                            radius = p.widthPx * 0.45f,
                            center = Offset(currentX, currentY)
                        )
                    }
                    ConfettiShape.DIAMOND -> {
                        // Draw diamond by drawing a rotated square
                        rotate(degrees = 45f, pivot = Offset(currentX, currentY)) {
                            drawRect(
                                color = p.color,
                                topLeft = Offset(currentX - p.widthPx / 2f, currentY - p.widthPx / 2f),
                                size = Size(p.widthPx, p.widthPx)
                            )
                        }
                    }
                }
            }
        }
    }
}
