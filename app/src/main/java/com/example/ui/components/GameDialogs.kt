package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ScoreBreakdown
import com.example.engine.ScoreSystem
import com.example.model.LevelData
import com.example.model.TutorialStep

/**
 * Encapsulates performance rating evaluated based on move count relative to level par.
 */
data class PerformanceRating(
    val tierTitle: String,
    val rankBadge: String,
    val praiseText: String,
    val deltaText: String,
    val efficiencyPercent: Int,
    val accentColor: Color,
    val starCount: Int
)

fun evaluatePerformanceRating(moves: Int, par: Int): PerformanceRating {
    val delta = moves - par
    val efficiency = if (moves > 0) ((par.toFloat() / moves.toFloat()) * 100).toInt().coerceIn(10, 100) else 100
    return when {
        moves <= par -> PerformanceRating(
            tierTitle = "PERFECT RUN! 🏆",
            rankBadge = "MASTER STATION MASTER 🇬🇭",
            praiseText = "Flawless navigation! You untangled the lorry park at minimal Par target with zero wasted moves.",
            deltaText = "★ Exact Par ($moves / $par moves · 100% Optimal)",
            efficiencyPercent = efficiency,
            accentColor = Color(0xFFF59E0B),
            starCount = 3
        )
        moves <= par + 2 -> PerformanceRating(
            tierTitle = "GREAT EFFORT! 🎯",
            rankBadge = "EXPERT CONDUCTOR",
            praiseText = "Sharp coordination! Fast passenger boarding with only +$delta moves from perfect Par.",
            deltaText = "★ +$delta moves over Par ($moves / $par moves)",
            efficiencyPercent = efficiency,
            accentColor = Color(0xFF10B981),
            starCount = 2
        )
        else -> PerformanceRating(
            tierTitle = "SAFE ARRIVAL! 👍",
            rankBadge = "STEADY DRIVER",
            praiseText = "All passengers safely reached the terminal! Replay this station to aim for Par and claim 3 Stars.",
            deltaText = "★ +$delta moves over Par ($moves / $par moves)",
            efficiencyPercent = efficiency,
            accentColor = Color(0xFF3B82F6),
            starCount = 1
        )
    }
}

@Composable
fun WinDialog(
    moves: Int,
    par: Int,
    stars: Int,
    scoreBreakdown: ScoreBreakdown? = null,
    unlockedNextLevel: LevelData? = null,
    isReducedMotion: Boolean = false,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelSelect: () -> Unit
) {
    val bannerScale = remember { Animatable(if (isReducedMotion) 1f else 0.7f) }
    val starsScale = remember { Animatable(if (isReducedMotion) 1f else 0f) }
    val rating = remember(moves, par) { evaluatePerformanceRating(moves, par) }

    LaunchedEffect(Unit) {
        if (!isReducedMotion) {
            bannerScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)
            )
            starsScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessLow)
            )
        }
    }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = onNextLevel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("win_next_button")
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Next Station", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                OutlinedButton(onClick = onReplay, modifier = Modifier.testTag("win_replay_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Replay")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onLevelSelect, modifier = Modifier.testTag("win_levels_button")) {
                    Text("Stations")
                }
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 'Trotro Arrived!' Celebratory Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(bannerScale.value)
                        .testTag("trotro_arrived_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF9A3412), // Deep laterite earth
                                        Color(0xFFD97706), // Trotro gold
                                        Color(0xFFB45309)  // Warm ochre
                                    )
                                )
                            )
                            .border(2.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🚌", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TROTRO ARRIVED!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "🇬🇭", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Safe Journey · All Passengers Disembarked",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF3C7),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Highlighted Player Performance Rating based on move count
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("performance_rating_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, rating.accentColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = rating.accentColor.copy(alpha = 0.18f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, rating.accentColor)
                        ) {
                            Text(
                                text = rating.rankBadge,
                                color = rating.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = rating.tierTitle,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Animated Stars
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.scale(starsScale.value)
                        ) {
                            repeat(3) { index ->
                                val active = index < rating.starCount
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = if (active) "Star earned" else "Star unearned",
                                    tint = if (active) Color(0xFFFBBF24) else Color(0xFF64748B),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = rating.deltaText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = rating.accentColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = rating.praiseText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Station Unlocked Callout Banner if a new station was unlocked
                if (unlockedNextLevel != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Next Level Unlocked",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "🔓 NEXT STATION UNLOCKED!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${unlockedNextLevel.id}: ${unlockedNextLevel.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = unlockedNextLevel.difficultyTier.subtitle,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Score Card if available
                if (scoreBreakdown != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (scoreBreakdown.isNewHighScore) {
                                Text(
                                    text = "🌟 NEW HIGH SCORE! 🌟",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFBBF24)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = "TOTAL SCORE",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "%,d PTS".format(scoreBreakdown.totalScore),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = scoreBreakdown.speedRating,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Breakdown details
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🎯 Move Efficiency:", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                                    Text("+%,d".format(scoreBreakdown.moveEfficiencyBonus), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("⏱️ Speed Bonus (${scoreBreakdown.elapsedSeconds}s):", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                                    Text("+%,d".format(scoreBreakdown.speedBonus), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                }
                                if (scoreBreakdown.perfectParBonus > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("⭐ Under/At Par Bonus:", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                                        Text("+%,d".format(scoreBreakdown.perfectParBonus), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🚌 Station & Passengers:", fontSize = 11.sp, color = Color(0xFFE2E8F0))
                                    Text("+%,d".format(scoreBreakdown.baseClearPoints + scoreBreakdown.passengerBonus), fontSize = 11.sp, color = Color(0xFFE2E8F0))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Your Moves", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$moves", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Par Target", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$par", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        if (scoreBreakdown != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${scoreBreakdown.elapsedSeconds}s", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun LoseDialog(
    reason: String = "No free parking slots and no legal exit paths remain.",
    onUndo: () -> Unit,
    onRestart: () -> Unit,
    onLevelSelect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            Button(
                onClick = onUndo,
                modifier = Modifier.testTag("dialog_undo_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Undo Move", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                OutlinedButton(onClick = onRestart) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restart")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onLevelSelect) {
                    Text("Levels")
                }
            }
        },
        title = {
            Text(
                text = "TRAFFIC JAM! 🛑",
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(text = reason, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tip: You can use unlimited Undo to reverse your previous moves!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onLevelSelect: () -> Unit,
    onSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onResume,
        confirmButton = {
            Button(onClick = onResume) {
                Text("Resume")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onRestart) {
                Text("Restart Level")
            }
        },
        title = {
            Text("Game Paused", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onLevelSelect,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Level Select")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Settings & Sound")
                }
            }
        }
    )
}

@Composable
fun TutorialOverlay(
    step: TutorialStep,
    onDismiss: () -> Unit,
    onNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("tutorial_overlay"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF06B6D4)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Step counter + dots + close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF06B6D4)
                    ) {
                        Text(
                            text = "TUTORIAL · STEP ${step.stepIndex}/${step.totalSteps}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Step Dots Indicator
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..step.totalSteps).forEach { idx ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (idx == step.stepIndex) Color(0xFF06B6D4)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss tutorial hint",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title & Subtitle
            Text(
                text = step.title,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = step.subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF06B6D4)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Explanation body
            Text(
                text = step.message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.95f),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Conductor's Mate Callout / Authentic Tip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFEF3C7),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📢", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = step.mateTip,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF78350F),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Skip Tutorial",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (onNext != null && step.nextStep() != null) {
                        Button(
                            onClick = onNext,
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF06B6D4),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = "Next Tip ➔",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Got It! Let's Play ✓",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
