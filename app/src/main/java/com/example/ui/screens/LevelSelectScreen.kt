package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundPlayer
import com.example.data.AppRepository
import com.example.levels.LevelRepository
import com.example.model.LevelData
import com.example.model.LevelTier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    repository: AppRepository,
    onLevelSelected: (LevelData) -> Unit,
    onLeaderboard: () -> Unit,
    onBack: () -> Unit
) {
    val progressList by repository.allProgress.collectAsState(initial = emptyList())
    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelId }
    }

    val totalStars = remember(progressList) { progressList.sumOf { it.stars } }
    val completedCount = remember(progressList) { progressList.count { it.isCompleted } }
    val totalLevels = LevelRepository.levels.size

    var selectedBandIndex by remember { mutableIntStateOf(0) }
    val tierTabs = listOf(
        Pair("Tier 1 (L1-10)", LevelTier.BEGINNER),
        Pair("Tier 2 (L11-20)", LevelTier.INTERMEDIATE),
        Pair("Tier 3 (L21-30)", LevelTier.ADVANCED),
        Pair("Tier 4 (L31-40)", LevelTier.EXPERT)
    )

    val currentTier = tierTabs[selectedBandIndex].second

    val displayedLevels = remember(selectedBandIndex) {
        val start = selectedBandIndex * 10
        val end = (start + 10).coerceAtMost(LevelRepository.levels.size)
        LevelRepository.levels.subList(start, end)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Accra Lorry Stations", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Title")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            SoundPlayer.playClick()
                            onLeaderboard()
                        }
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Leaderboard",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Overall Progression Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalStars / ${totalLevels * 3} Stars",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏁", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$completedCount / $totalLevels Cleared",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (completedCount.toFloat() / totalLevels.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }

            // Difficulty Tier Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedBandIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tierTabs.forEachIndexed { index, pair ->
                    Tab(
                        selected = selectedBandIndex == index,
                        onClick = { selectedBandIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = pair.second.badgeColor,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.size(8.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(pair.first, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }

            // Tier Description Sub-header
            Surface(
                color = currentTier.badgeColor.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentTier.subtitle}: ${currentTier.description}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedLevels) { level ->
                    val progress = progressMap[level.id]
                    val isUnlocked = progress?.isUnlocked == true || level.id == "L001"
                    val stars = progress?.stars ?: 0
                    val isCompleted = progress?.isCompleted == true

                    LevelCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        stars = stars,
                        bestMoves = progress?.bestMoves ?: 0,
                        highScore = progress?.highScore ?: 0,
                        onClick = {
                            if (isUnlocked) {
                                SoundPlayer.playClick()
                                onLevelSelected(level)
                            } else {
                                SoundPlayer.playBlocked()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: LevelData,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    bestMoves: Int,
    highScore: Int = 0,
    onClick: () -> Unit
) {
    val cardBg = if (isUnlocked) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val shape = RoundedCornerShape(14.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(135.dp)
            .clip(shape)
            .clickable(enabled = isUnlocked, onClick = onClick)
            .border(
                width = if (isCompleted) 1.5.dp else 1.dp,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = shape
            )
            .semantics {
                contentDescription = "${level.id}, ${level.name}, ${if (isUnlocked) "unlocked" else "locked"}, $stars stars"
            },
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(if (isUnlocked) 3.dp else 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            if (!isUnlocked) {
                // Locked overlay with station name and unlock hint
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked level",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${level.id}: ${level.name}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val prevLevelNum = (level.id.removePrefix("L").toIntOrNull() ?: 2) - 1
                    Text(
                        text = "Clear Level $prevLevelNum to unlock",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Unlocked level content
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = level.id,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = level.difficultyTier.badgeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = level.difficultyTier.name,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = level.difficultyTier.badgeColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Row {
                            repeat(3) { index ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < stars) Color(0xFFFBBF24) else Color(0x3364748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Column {
                        Text(
                            text = level.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCompleted && highScore > 0) {
                            Text(
                                text = "🏆 %,d pts".format(highScore),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${level.vehicles.size} Trotros",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isCompleted && bestMoves > 0) {
                            Text(
                                text = "Best: $bestMoves (Par: ${level.par})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Par: ${level.par}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
