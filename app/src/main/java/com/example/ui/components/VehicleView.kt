package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HapticFeedbackManager
import com.example.data.AppThemeMode
import com.example.model.Axis
import com.example.model.Direction
import com.example.model.Vehicle
import com.example.model.VehicleType
import com.example.ui.theme.LocalAppThemeMode
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun VehicleView(
    vehicle: Vehicle,
    cellSize: Dp,
    modifier: Modifier = Modifier,
    isBlocked: Boolean = false,
    isClearToExit: Boolean = false,
    isHinted: Boolean = false,
    tutorialBadge: String? = null,
    onClick: () -> Unit
) {
    val themeMode = LocalAppThemeMode.current
    val isHorizontal = vehicle.axis == Axis.HORIZONTAL

    val vehicleWidth = if (isHorizontal) cellSize * vehicle.length else cellSize
    val vehicleHeight = if (isHorizontal) cellSize else cellSize * vehicle.length

    val bodyColor = when (themeMode) {
        AppThemeMode.LIGHT -> vehicle.colour.lightColor
        AppThemeMode.DARK -> vehicle.colour.darkColor
        AppThemeMode.HIGH_CONTRAST -> vehicle.colour.highContrastColor
    }

    // Hint / Tutorial pulsating glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val hintBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_border_alpha"
    )
    val hintScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_scale"
    )

    val isTutorialTarget = tutorialBadge != null
    val borderColor = when {
        themeMode == AppThemeMode.HIGH_CONTRAST -> Color.White
        isTutorialTarget -> Color(0xFF06B6D4).copy(alpha = hintBorderAlpha) // Radiant Cyan for Tutorial
        isHinted -> Color(0xFFF59E0B).copy(alpha = hintBorderAlpha)
        isClearToExit -> Color(0xFF4ADE80) // Bright energetic green border when clear
        else -> Color(0x99FFFFFF)
    }
    val borderWidth = when {
        isTutorialTarget -> 3.5.dp
        isHinted -> 3.dp
        isClearToExit -> 2.5.dp
        else -> 2.dp
    }
    val shape = RoundedCornerShape(10.dp)

    // Bump animation on blocked path
    val bumpOffset = remember { Animatable(0f) }
    LaunchedEffect(isBlocked) {
        if (isBlocked) {
            val delta = 12f
            when (vehicle.direction) {
                Direction.UP -> {
                    bumpOffset.animateTo(-delta, tween(60))
                    bumpOffset.animateTo(0f, tween(80))
                }
                Direction.DOWN -> {
                    bumpOffset.animateTo(delta, tween(60))
                    bumpOffset.animateTo(0f, tween(80))
                }
                Direction.LEFT -> {
                    bumpOffset.animateTo(-delta, tween(60))
                    bumpOffset.animateTo(0f, tween(80))
                }
                Direction.RIGHT -> {
                    bumpOffset.animateTo(delta, tween(60))
                    bumpOffset.animateTo(0f, tween(80))
                }
            }
        }
    }

    val xOffset = if (isHorizontal) bumpOffset.value.roundToInt() else 0
    val yOffset = if (!isHorizontal) bumpOffset.value.roundToInt() else 0

    Box(
        modifier = modifier
            .offset { IntOffset(xOffset, yOffset) }
            .then(if (isHinted) Modifier.scale(hintScale) else Modifier)
            .size(width = vehicleWidth, height = vehicleHeight)
            .padding(2.dp)
            .shadow(if (isHinted) 8.dp else 4.dp, shape)
            .clip(shape)
            .background(bodyColor)
            .border(borderWidth, borderColor, shape)
            .clickable(
                onClick = {
                    HapticFeedbackManager.performDragSnap()
                    onClick()
                }
            )
            .semantics {
                role = Role.Button
                val hintNotice = if (isHinted) " (Optimal next move recommended)" else ""
                val tutorialNotice = if (tutorialBadge != null) " (Tutorial Target: $tutorialBadge)" else ""
                contentDescription = "${vehicle.colour.displayName} ${vehicle.type.displayName}$hintNotice$tutorialNotice, " +
                        "arrow ${vehicle.direction.name.lowercase()}, ${vehicle.seats} seats, " +
                        "row ${vehicle.row + 1} column ${vehicle.col + 1}"
            },
        contentAlignment = Alignment.Center
    ) {
        if (tutorialBadge != null) {
            Box(
                modifier = Modifier
                    .align(if (isHinted) Alignment.BottomStart else Alignment.TopStart)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF06B6D4))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = tutorialBadge,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }
        if (isHinted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF59E0B))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "💡 NEXT",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }
        if (isHorizontal) {
            // Horizontal layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading arrow or trailing arrow depending on direction
                if (vehicle.direction == Direction.LEFT) {
                    ArrowBadge(vehicle.direction, vehicle.colour.onColor, isClearToExit)
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Center info: symbol and slogan
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vehicle.colour.symbolChar,
                            color = vehicle.colour.onColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${vehicle.seats} seats",
                            color = vehicle.colour.onColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (vehicle.type != VehicleType.CAR) {
                        Text(
                            text = "\"${vehicle.slogan}\"",
                            color = vehicle.colour.onColor.copy(alpha = 0.9f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (vehicle.direction == Direction.RIGHT) {
                    Spacer(modifier = Modifier.width(4.dp))
                    ArrowBadge(vehicle.direction, vehicle.colour.onColor, isClearToExit)
                }
            }
        } else {
            // Vertical layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (vehicle.direction == Direction.UP) {
                    ArrowBadge(vehicle.direction, vehicle.colour.onColor, isClearToExit)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = vehicle.colour.symbolChar,
                        color = vehicle.colour.onColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${vehicle.seats}s",
                        color = vehicle.colour.onColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (vehicle.type != VehicleType.CAR) {
                        Text(
                            text = vehicle.slogan.take(8),
                            color = vehicle.colour.onColor.copy(alpha = 0.9f),
                            fontSize = 8.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (vehicle.direction == Direction.DOWN) {
                    Spacer(modifier = Modifier.height(4.dp))
                    ArrowBadge(vehicle.direction, vehicle.colour.onColor, isClearToExit)
                }
            }
        }
    }
}

@Composable
private fun ArrowBadge(direction: Direction, tint: Color, isClear: Boolean) {
    val bg = if (isClear) Color(0xFF15803D) else Color(0x55000000)
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .then(
                if (isClear) Modifier.border(1.5.dp, Color(0xFF4ADE80), RoundedCornerShape(6.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = direction.arrowSymbol,
            color = if (isClear) Color.White else tint,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
