package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.AppRepository
import com.example.levels.LevelRepository
import com.example.model.LevelData
import com.example.ui.components.VirtualTourDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CreditsScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.PlayScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TestRunnerScreen
import com.example.ui.screens.TitleScreen
import com.example.ui.theme.TrotroRushTheme

sealed class Screen {
    object Title : Screen()
    object LevelSelect : Screen()
    data class Play(val level: LevelData) : Screen()
    object Leaderboard : Screen()
    object Settings : Screen()
    object Admin : Screen()
    object TestRunner : Screen()
    object Privacy : Screen()
    object Credits : Screen()
}

@Composable
fun TrotroRushApp(repository: AppRepository) {
    val themeMode by repository.themeMode.collectAsState()
    val hasCompletedTour by repository.hasCompletedTour.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Title) }
    val progressList by repository.allProgress.collectAsState(initial = emptyList())

    // Automatically trigger Virtual Tour on first-time launch
    var showVirtualTour by remember(hasCompletedTour) { mutableStateOf(!hasCompletedTour) }

    TrotroRushTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (val screen = currentScreen) {
                is Screen.Title -> {
                    TitleScreen(
                        onPlay = {
                            // Find highest unlocked level or Level 1
                            val highestUnlocked = LevelRepository.levels.lastOrNull { lvl ->
                                progressList.find { it.levelId == lvl.id }?.isUnlocked == true
                            } ?: LevelRepository.levels.first()
                            currentScreen = Screen.Play(highestUnlocked)
                        },
                        onTour = { showVirtualTour = true },
                        onLevelSelect = { currentScreen = Screen.LevelSelect },
                        onLeaderboard = { currentScreen = Screen.Leaderboard },
                        onSettings = { currentScreen = Screen.Settings },
                        onAdmin = { currentScreen = Screen.Admin },
                        onTestRunner = { currentScreen = Screen.TestRunner },
                        onPrivacy = { currentScreen = Screen.Privacy },
                        onCredits = { currentScreen = Screen.Credits }
                    )
                }

                is Screen.LevelSelect -> {
                    BackHandler { currentScreen = Screen.Title }
                    LevelSelectScreen(
                        repository = repository,
                        onLevelSelected = { level -> currentScreen = Screen.Play(level) },
                        onLeaderboard = { currentScreen = Screen.Leaderboard },
                        onBack = { currentScreen = Screen.Title }
                    )
                }

                is Screen.Play -> {
                    PlayScreen(
                        level = screen.level,
                        repository = repository,
                        onBackToLevels = { currentScreen = Screen.LevelSelect },
                        onNextLevel = { nextLevel -> currentScreen = Screen.Play(nextLevel) },
                        onOpenSettings = { currentScreen = Screen.Settings }
                    )
                }

                is Screen.Leaderboard -> {
                    BackHandler { currentScreen = Screen.Title }
                    LeaderboardScreen(
                        repository = repository,
                        onBack = { currentScreen = Screen.Title },
                        onPlayLevel = { level -> currentScreen = Screen.Play(level) }
                    )
                }

                is Screen.Settings -> {
                    BackHandler { currentScreen = Screen.Title }
                    SettingsScreen(
                        repository = repository,
                        onBack = { currentScreen = Screen.Title },
                        onOpenAdmin = { currentScreen = Screen.Admin }
                    )
                }

                is Screen.Admin -> {
                    BackHandler { currentScreen = Screen.Title }
                    AdminScreen(
                        repository = repository,
                        onBack = { currentScreen = Screen.Title }
                    )
                }

                is Screen.TestRunner -> {
                    BackHandler { currentScreen = Screen.Title }
                    TestRunnerScreen(
                        repository = repository,
                        onBack = { currentScreen = Screen.Title }
                    )
                }

                is Screen.Privacy -> {
                    BackHandler { currentScreen = Screen.Title }
                    PrivacyScreen(onBack = { currentScreen = Screen.Title })
                }

                is Screen.Credits -> {
                    BackHandler { currentScreen = Screen.Title }
                    CreditsScreen(onBack = { currentScreen = Screen.Title })
                }
            }

            if (showVirtualTour) {
                VirtualTourDialog(
                    onDismiss = {
                        showVirtualTour = false
                        repository.setHasCompletedTour(true)
                    }
                )
            }
        }
    }
}
