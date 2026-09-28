package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundPlayer
import com.example.data.AppRepository
import com.example.data.ProgressEntity
import com.example.levels.LevelRepository
import com.example.model.LevelData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LeaderboardFilter {
    ALL,
    COMPLETED_ONLY,
    AT_OR_UNDER_PAR
}

enum class LeaderboardSort {
    LEVEL_ORDER,
    FEWEST_MOVES,
    HIGHEST_SCORE,
    MOST_STARS
}

data class LevelLeaderboardItem(
    val level: LevelData,
    val progress: ProgressEntity?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    repository: AppRepository,
    onBack: () -> Unit,
    onPlayLevel: (LevelData) -> Unit
) {
    val progressList by repository.allProgress.collectAsState(initial = emptyList())
    var currentFilter by remember { mutableStateOf(LeaderboardFilter.COMPLETED_ONLY) }
    var currentSort by remember { mutableStateOf(LeaderboardSort.LEVEL_ORDER) }

    // Aggregate statistics
    val progressMap = remember(progressList) { progressList.associateBy { it.levelId } }
    val completedEntries = remember(progressList) { progressList.filter { it.isCompleted && it.bestMoves > 0 } }
    val totalScore = remember(completedEntries) { completedEntries.sumOf { it.highScore } }
    val totalStars = remember(completedEntries) { completedEntries.sumOf { it.stars } }
    val completedCount = completedEntries.size
    val totalLevels = LevelRepository.levels.size

    // Calculate moves efficiency
    val movesDelta = remember(completedEntries) {
        completedEntries.sumOf { entry ->
            val lvl = LevelRepository.getLevelById(entry.levelId)
            val par = lvl?.par ?: entry.bestMoves
            entry.bestMoves - par
        }
    }

    // Determine Station Master rank title
    val rankTitle = remember(completedCount, totalStars) {
        when {
            completedCount == 0 -> "Novice Commuter"
            completedCount < 5 -> "Apprentice Trotro Mate"
            completedCount < 15 -> "Transit Route Driver"
            completedCount < 25 -> "Circle Interchange Pro"
            completedCount < 35 -> "Madina Express Master"
            totalStars >= 110 -> "Grand Station Master 🇬🇭"
            else -> "Senior Transit Marshal"
        }
    }

    // Filter and sort level records
    val displayItems = remember(progressMap, currentFilter, currentSort) {
        val allItems = LevelRepository.levels.map { level ->
            LevelLeaderboardItem(level = level, progress = progressMap[level.id])
        }

        val filtered = when (currentFilter) {
            LeaderboardFilter.ALL -> allItems
            LeaderboardFilter.COMPLETED_ONLY -> allItems.filter { it.progress?.isCompleted == true }
            LeaderboardFilter.AT_OR_UNDER_PAR -> allItems.filter {
                val p = it.progress
                p != null && p.isCompleted && p.bestMoves <= it.level.par
            }
        }

        when (currentSort) {
            LeaderboardSort.LEVEL_ORDER -> filtered.sortedBy { it.level.id }
            LeaderboardSort.FEWEST_MOVES -> filtered.sortedWith(
                compareBy<LevelLeaderboardItem> { it.progress?.bestMoves ?: Int.MAX_VALUE }
                    .thenBy { it.level.id }
            )
            LeaderboardSort.HIGHEST_SCORE -> filtered.sortedWith(
                compareByDescending<LevelLeaderboardItem> { it.progress?.highScore ?: 0 }
                    .thenBy { it.level.id }
            )
            LeaderboardSort.MOST_STARS -> filtered.sortedWith(
                compareByDescending<LevelLeaderboardItem> { it.progress?.stars ?: 0 }
                    .thenBy { it.progress?.bestMoves ?: Int.MAX_VALUE }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Local Leaderboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                        Text(
                            text = "Room-Persisted High Scores & Minimum Moves",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            SoundPlayer.playClick()
                            onBack()
                        },
                        modifier = Modifier.testTag("leaderboard_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Station Master Header Summary Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leaderboard_summary_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "TRANSIT CAREER RANK",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = rankTitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid (4 columns)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LeaderboardMetric(
                                label = "High Score",
                                value = String.format(Locale.US, "%,d", totalScore),
                                icon = "🏆"
                            )
                            LeaderboardMetric(
                                label = "Completed",
                                value = "$completedCount / $totalLevels",
                                icon = "🏁"
                            )
                            LeaderboardMetric(
                                label = "Stars",
                                value = "$totalStars / ${totalLevels * 3}",
                                icon = "⭐"
                            )
                            LeaderboardMetric(
                                label = "Par Delta",
                                value = if (completedCount == 0) "--" else if (movesDelta <= 0) "$movesDelta" else "+$movesDelta",
                                icon = "🎯"
                            )
                        }
                    }
                }
            }

            // Filter and Sort Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = currentFilter == LeaderboardFilter.COMPLETED_ONLY,
                                onClick = { currentFilter = LeaderboardFilter.COMPLETED_ONLY },
                                label = { Text("Completed (${completedCount})", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors()
                            )
                            FilterChip(
                                selected = currentFilter == LeaderboardFilter.ALL,
                                onClick = { currentFilter = LeaderboardFilter.ALL },
                                label = { Text("All ($totalLevels)", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = currentFilter == LeaderboardFilter.AT_OR_UNDER_PAR,
                                onClick = { currentFilter = LeaderboardFilter.AT_OR_UNDER_PAR },
                                label = { Text("≤ Par", fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort by:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = currentSort == LeaderboardSort.LEVEL_ORDER,
                                onClick = { currentSort = LeaderboardSort.LEVEL_ORDER },
                                label = { Text("Level", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = currentSort == LeaderboardSort.FEWEST_MOVES,
                                onClick = { currentSort = LeaderboardSort.FEWEST_MOVES },
                                label = { Text("Min Moves", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = currentSort == LeaderboardSort.HIGHEST_SCORE,
                                onClick = { currentSort = LeaderboardSort.HIGHEST_SCORE },
                                label = { Text("Score", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = currentSort == LeaderboardSort.MOST_STARS,
                                onClick = { currentSort = LeaderboardSort.MOST_STARS },
                                label = { Text("Stars", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Empty state if no items match
            if (displayItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🚌", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Completed Levels Yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Solve traffic puzzles to record your minimum moves and high scores into the local Room database!",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    SoundPlayer.playClick()
                                    onPlayLevel(LevelRepository.levels.first())
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play Level 1 Now")
                            }
                        }
                    }
                }
            } else {
                items(displayItems, key = { it.level.id }) { item ->
                    LeaderboardLevelCard(
                        item = item,
                        onPlay = {
                            SoundPlayer.playClick()
                            onPlayLevel(item.level)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardMetric(
    label: String,
    value: String,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun LeaderboardLevelCard(
    item: LevelLeaderboardItem,
    onPlay: () -> Unit
) {
    val level = item.level
    val progress = item.progress
    val isCompleted = progress?.isCompleted == true
    val bestMoves = progress?.bestMoves ?: 0
    val highScore = progress?.highScore ?: 0
    val stars = progress?.stars ?: 0
    val isUnlocked = progress?.isUnlocked == true || level.id == "L001"

    val diffFromPar = if (bestMoves > 0) bestMoves - level.par else 0

    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val dateText = remember(progress?.updatedAt) {
        if (progress != null && progress.updatedAt > 0) {
            dateFormat.format(Date(progress.updatedAt))
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_level_${level.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Level ID, Name, and Stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = level.id,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = level.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Star Rating
                Row {
                    repeat(3) { index ->
                        Icon(
                            imageVector = if (index < stars) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (index < stars) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Metrics details (Minimum Moves, Par, High Score, Delta)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minimum Moves badge
                Column {
                    Text(
                        text = "MINIMUM MOVES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (bestMoves > 0) "$bestMoves moves" else "--",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (bestMoves in 1..level.par) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Par: ${level.par})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // High Score badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ROOM HIGH SCORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (highScore > 0) String.format(Locale.US, "%,d pts", highScore) else "Unranked",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Efficiency badge & Replay Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCompleted) {
                        val badgeColor = when {
                            diffFromPar < 0 -> Color(0xFF1B5E20)
                            diffFromPar == 0 -> Color(0xFF2E7D32)
                            diffFromPar <= 2 -> Color(0xFFE65100)
                            else -> Color(0xFFC62828)
                        }
                        val badgeText = when {
                            diffFromPar < 0 -> "🏆 $diffFromPar UNDER PAR"
                            diffFromPar == 0 -> "✨ EXACT PAR MATCH"
                            else -> "+$diffFromPar OVER PAR"
                        }
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isUnlocked) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "UNLOCKED · NOT CLEARED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "🔒 LOCKED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    if (dateText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "· $dateText",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (isUnlocked) {
                    OutlinedButton(
                        onClick = onPlay,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCompleted) "Replay" else "Play",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
