package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AppThemeMode
import com.example.model.Direction
import com.example.ui.theme.LocalAppThemeMode
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GridBurstData(
    val row: Int,
    val col: Int,
    val direction: Direction,
    val timestamp: Long
)

/**
 * Data structure representing a single particle of Ghanaian road dust / harmattan haze
 * kicked up when a trotro accelerates or pulls into a bay.
 */
private data class DustParticle(
    val relX: Float, // Relative origin fraction along vehicle width [0..1]
    val relY: Float, // Relative origin fraction along vehicle height [0..1]
    val driftAngleRad: Float, // Direction of particle dispersion
    val speed: Float, // Dispersion distance multiplier
    val baseRadiusDp: Float, // Initial radius in dp
    val maxExpansion: Float, // Max radius multiplier (e.g. 1.8x)
    val colorIndex: Int, // Index into the dust palette
    val peakAlpha: Float // Maximum opacity before dissolving
)

/**
 * Subtle "road dust" particle animation that puffs around a trotro vehicle
 * whenever it is successfully moved to a new slot on the game board.
 *
 * @param triggerKey Changing this key triggers a fresh burst of road dust particles.
 * @param modifier Layout modifier.
 * @param particleCount Number of dust particles generated (default 16).
 * @param durationMs Animation duration in milliseconds.
 * @param isReducedMotion When true, skips particle expansion for motion sensitivity.
 */
@Composable
fun RoadDustEffect(
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    particleCount: Int = 16,
    durationMs: Int = 750,
    isReducedMotion: Boolean = false
) {
    if (isReducedMotion || triggerKey == null) return

    val themeMode = LocalAppThemeMode.current
    val progress = remember(triggerKey) { Animatable(0f) }

    // Generate deterministic particles based on triggerKey to avoid re-randomising during recomposition
    val particles = remember(triggerKey, particleCount) {
        val seed = triggerKey.hashCode().toLong()
        val rng = Random(seed)
        List(particleCount) { i ->
            // Particles emanate from tires / edges (bottom, left, right flanks)
            val isFlank = (i % 3 == 0)
            val relX = when {
                isFlank && (i % 2 == 0) -> rng.nextFloat() * 0.25f // Left flank
                isFlank -> 0.75f + rng.nextFloat() * 0.25f // Right flank
                else -> 0.1f + rng.nextFloat() * 0.8f // Underside/tires
            }
            val relY = if (isFlank) 0.3f + rng.nextFloat() * 0.6f else 0.7f + rng.nextFloat() * 0.3f

            // Disperse outward with upward drift (heat/air turbulence)
            val outwardSign = if (relX < 0.5f) -1f else 1f
            val baseAngle = if (outwardSign < 0) {
                // Drift left and slightly up (-135 deg to -180 deg)
                Math.toRadians((140.0 + rng.nextDouble() * 50.0))
            } else {
                // Drift right and slightly up (-45 deg to 0 deg)
                Math.toRadians((-10.0 + rng.nextDouble() * 50.0))
            }

            DustParticle(
                relX = relX,
                relY = relY,
                driftAngleRad = baseAngle.toFloat(),
                speed = 28f + rng.nextFloat() * 32f,
                baseRadiusDp = 3.5f + rng.nextFloat() * 4.5f,
                maxExpansion = 1.6f + rng.nextFloat() * 0.8f,
                colorIndex = rng.nextInt(5),
                peakAlpha = 0.55f + rng.nextFloat() * 0.35f
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs, easing = FastOutSlowInEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress in 0.001f..0.999f) {
        // Authentic Ghanaian laterite earth & harmattan road dust palette
        val dustPalette = when (themeMode) {
            AppThemeMode.HIGH_CONTRAST -> listOf(
                Color(0xCCFFFFFF),
                Color(0x99F1F5F9),
                Color(0xB3E2E8F0),
                Color(0x80CBD5E1),
                Color(0xAAFFFFFF)
            )
            else -> listOf(
                Color(0xDDE2C499), // Warm sandy dust
                Color(0xCCD4A373), // Red clay soil
                Color(0xD9C58940), // Accra ochre
                Color(0xE6E6CCB2), // Harmattan haze
                Color(0xCCEDE0D4)  // Chalky road powder
            )
        }

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .testTag("road_dust_effect")
                .clearAndSetSemantics { /* Decorative particle effect — silent for TalkBack */ }
        ) {
            val width = size.width
            val height = size.height

            particles.forEach { p ->
                // Particle position: starts at vehicle edge, drifts along vector
                val startX = p.relX * width
                val startY = p.relY * height

                // Drift outward with ease-out physics and gentle upward buoyancy
                val travel = p.speed * currentProgress * density
                val buoyancy = currentProgress * 12f * density // floats upward slightly
                val currentX = startX + cos(p.driftAngleRad) * travel
                val currentY = startY + sin(p.driftAngleRad) * travel - buoyancy

                // Expansion: puff expands as it diffuses into the air
                val radius = (p.baseRadiusDp * density) * (1f + (p.maxExpansion - 1f) * currentProgress)

                // Alpha envelope: quick fade-in, smooth exponential fade-out
                val alpha = when {
                    currentProgress < 0.15f -> (currentProgress / 0.15f) * p.peakAlpha
                    else -> (1f - (currentProgress - 0.15f) / 0.85f).coerceIn(0f, 1f) * p.peakAlpha
                }

                if (alpha > 0.01f) {
                    val particleColor = dustPalette[p.colorIndex].copy(alpha = alpha)

                    // Draw soft atmospheric dust puff
                    drawCircle(
                        color = particleColor,
                        radius = radius,
                        center = Offset(currentX, currentY)
                    )

                    // Inner denser core for texture
                    drawCircle(
                        color = particleColor.copy(alpha = (alpha * 1.25f).coerceAtMost(1f)),
                        radius = radius * 0.45f,
                        center = Offset(currentX, currentY)
                    )
                }
            }
        }
    }
}

/**
 * Road dust skid burst rendered on the car park board at the position
 * where a vehicle just accelerated away toward the exit gate.
 */
@Composable
fun RoadDustGridBurst(
    cellSize: Dp,
    direction: Direction,
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    isReducedMotion: Boolean = false
) {
    if (isReducedMotion || triggerKey == null) return

    val progress = remember(triggerKey) { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    val currentProgress = progress.value
    if (currentProgress in 0.001f..0.999f) {
        Canvas(
            modifier = modifier
                .testTag("road_dust_grid_burst")
                .clearAndSetSemantics { }
        ) {
            val cellPx = cellSize.toPx()
            val alpha = (1f - currentProgress).coerceIn(0f, 1f) * 0.6f

            // Skid dust clouds kicking back opposite to travel direction
            val kickbackAngle = when (direction) {
                Direction.UP -> Math.toRadians(90.0) // Kicks down
                Direction.DOWN -> Math.toRadians(-90.0) // Kicks up
                Direction.LEFT -> Math.toRadians(0.0) // Kicks right
                Direction.RIGHT -> Math.toRadians(180.0) // Kicks left
            }

            val dustColor = Color(0xD4A373).copy(alpha = alpha)
            val spreadRadius = (8f + 16f * currentProgress) * density

            val centerX = cellPx * 0.5f + cos(kickbackAngle).toFloat() * (12f * currentProgress * density)
            val centerY = cellPx * 0.5f + sin(kickbackAngle).toFloat() * (12f * currentProgress * density)

            drawCircle(
                color = dustColor,
                radius = spreadRadius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xE2C499).copy(alpha = alpha * 0.8f),
                radius = spreadRadius * 0.65f,
                center = Offset(centerX - 4f, centerY - 4f)
            )
        }
    }
}
