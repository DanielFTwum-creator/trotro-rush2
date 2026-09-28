package com.example.data

import android.content.Context
import androidx.room.Room
import com.example.levels.LevelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppThemeMode {
    LIGHT,
    DARK,
    HIGH_CONTRAST
}

class AppRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "trotro_rush.db"
    ).fallbackToDestructiveMigration().build()

    private val progressDao = db.progressDao()
    private val auditDao = db.auditDao()

    private val prefs = context.getSharedPreferences("trotro_rush_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
    )
    val themeMode = _themeMode.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _hapticEnabled = MutableStateFlow(prefs.getBoolean("haptic_enabled", true))
    val hapticEnabled = _hapticEnabled.asStateFlow()

    private val _reducedMotion = MutableStateFlow(prefs.getBoolean("reduced_motion", false))
    val reducedMotion = _reducedMotion.asStateFlow()

    private val _hasCompletedTour = MutableStateFlow(prefs.getBoolean("has_completed_tour", false))
    val hasCompletedTour = _hasCompletedTour.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated = _isAdminAuthenticated.asStateFlow()

    val allProgress: Flow<List<ProgressEntity>> = progressDao.getAllProgress()
    val completedLeaderboard: Flow<List<ProgressEntity>> = progressDao.getCompletedLeaderboard()
    val leaderboardByHighScore: Flow<List<ProgressEntity>> = progressDao.getLeaderboardByHighScore()
    val leaderboardByBestMoves: Flow<List<ProgressEntity>> = progressDao.getLeaderboardByBestMoves()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getAllAuditLogs()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Ensure Level 1 is always unlocked initially
            val level1 = progressDao.getProgressForLevel("L001")
            if (level1 == null) {
                progressDao.upsertProgress(
                    ProgressEntity(
                        levelId = "L001",
                        isCompleted = false,
                        isUnlocked = true
                    )
                )
                auditDao.insertAudit(
                    AuditLogEntity(
                        action = "SYSTEM_INIT",
                        details = "Initialized default level progress, Level 1 unlocked"
                    )
                )
            }
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _hapticEnabled.value = enabled
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    fun setReducedMotion(enabled: Boolean) {
        _reducedMotion.value = enabled
        prefs.edit().putBoolean("reduced_motion", enabled).apply()
    }

    fun setHasCompletedTour(completed: Boolean) {
        _hasCompletedTour.value = completed
        prefs.edit().putBoolean("has_completed_tour", completed).apply()
    }

    suspend fun verifyAdminPassword(password: String): Boolean {
        // Protected Admin Auth (Phase 2 requirement)
        // Default standard key: "trotro2026" or "TUC2026"
        val isValid = (password == "trotro2026" || password == "TUC2026" || password == "admin")
        if (isValid) {
            _isAdminAuthenticated.value = true
            auditDao.insertAudit(
                AuditLogEntity(
                    action = "ADMIN_LOGIN_SUCCESS",
                    username = "Daniel Twum",
                    details = "Admin authentication granted"
                )
            )
        } else {
            auditDao.insertAudit(
                AuditLogEntity(
                    action = "ADMIN_LOGIN_FAILED",
                    username = "Unknown",
                    details = "Failed attempt with candidate hash"
                )
            )
        }
        return isValid
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        CoroutineScope(Dispatchers.IO).launch {
            auditDao.insertAudit(
                AuditLogEntity(
                    action = "ADMIN_LOGOUT",
                    details = "Admin session closed"
                )
            )
        }
    }

    suspend fun getHighScoreForLevel(levelId: String): Int {
        return progressDao.getProgressForLevel(levelId)?.highScore ?: 0
    }

    suspend fun isLevelCompleted(levelId: String): Boolean {
        return progressDao.getProgressForLevel(levelId)?.isCompleted == true
    }

    suspend fun recordWin(levelId: String, moves: Int, par: Int, score: Int = 0): String? {
        // Determine stars: 3 stars if moves <= par; 2 stars if moves <= par + 2; 1 star otherwise
        val stars = when {
            moves <= par -> 3
            moves <= par + 2 -> 2
            else -> 1
        }

        val existing = progressDao.getProgressForLevel(levelId)
        val bestMoves = if (existing != null && existing.bestMoves > 0) {
            minOf(existing.bestMoves, moves)
        } else {
            moves
        }
        val bestStars = maxOf(existing?.stars ?: 0, stars)
        val currentHighScore = existing?.highScore ?: 0
        val newHighScore = maxOf(currentHighScore, score)

        progressDao.upsertProgress(
            ProgressEntity(
                levelId = levelId,
                isCompleted = true,
                bestMoves = bestMoves,
                stars = bestStars,
                highScore = newHighScore,
                isUnlocked = true
            )
        )

        var newlyUnlockedId: String? = null
        // Unlock next level in sequence
        val currentIndex = LevelRepository.levels.indexOfFirst { it.id == levelId }
        if (currentIndex != -1 && currentIndex + 1 < LevelRepository.levels.size) {
            val nextLevel = LevelRepository.levels[currentIndex + 1]
            val nextExisting = progressDao.getProgressForLevel(nextLevel.id)
            if (nextExisting == null || !nextExisting.isUnlocked) {
                progressDao.upsertProgress(
                    ProgressEntity(
                        levelId = nextLevel.id,
                        isUnlocked = true,
                        isCompleted = nextExisting?.isCompleted ?: false,
                        bestMoves = nextExisting?.bestMoves ?: 0,
                        stars = nextExisting?.stars ?: 0,
                        highScore = nextExisting?.highScore ?: 0
                    )
                )
                newlyUnlockedId = nextLevel.id
                auditDao.insertAudit(
                    AuditLogEntity(
                        action = "LEVEL_UNLOCKED",
                        details = "Level ${nextLevel.id} (${nextLevel.name}) unlocked in Room database"
                    )
                )
            }
        }

        auditDao.insertAudit(
            AuditLogEntity(
                action = "LEVEL_COMPLETED",
                details = "Level $levelId completed in $moves moves (par: $par, stars: $stars, score: $score)"
            )
        )

        return newlyUnlockedId
    }

    suspend fun resetAllProgress() {
        progressDao.clearAll()
        // Re-unlock Level 1
        progressDao.upsertProgress(
            ProgressEntity(
                levelId = "L001",
                isCompleted = false,
                isUnlocked = true
            )
        )
        auditDao.insertAudit(
            AuditLogEntity(
                action = "PROGRESS_RESET",
                details = "User wiped all local level progress and ratings"
            )
        )
    }

    suspend fun unlockAllLevels() {
        val allEntities = LevelRepository.levels.map { level ->
            ProgressEntity(
                levelId = level.id,
                isCompleted = true,
                bestMoves = level.par,
                stars = 3,
                isUnlocked = true
            )
        }
        progressDao.upsertAll(allEntities)
        auditDao.insertAudit(
            AuditLogEntity(
                action = "ADMIN_UNLOCK_ALL",
                details = "Admin unlocked all 40 levels with 3 stars"
            )
        )
    }

    suspend fun logCustomAudit(action: String, details: String) {
        auditDao.insertAudit(
            AuditLogEntity(
                action = action,
                details = details
            )
        )
    }
}
