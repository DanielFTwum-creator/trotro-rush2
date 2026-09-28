package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "level_progress")
data class ProgressEntity(
    @PrimaryKey val levelId: String,
    val isCompleted: Boolean = false,
    val bestMoves: Int = 0,
    val stars: Int = 0,
    val highScore: Int = 0,
    val isUnlocked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val username: String = "Admin",
    val details: String = ""
)

@Dao
interface ProgressDao {
    @Query("SELECT * FROM level_progress")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    suspend fun getProgressForLevel(levelId: String): ProgressEntity?

    @Query("SELECT * FROM level_progress WHERE isCompleted = 1 AND bestMoves > 0 ORDER BY levelId ASC")
    fun getCompletedLeaderboard(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE isCompleted = 1 AND bestMoves > 0 ORDER BY highScore DESC")
    fun getLeaderboardByHighScore(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE isCompleted = 1 AND bestMoves > 0 ORDER BY bestMoves ASC")
    fun getLeaderboardByBestMoves(): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(progressList: List<ProgressEntity>)

    @Query("DELETE FROM level_progress")
    suspend fun clearAll()
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertAudit(log: AuditLogEntity)

    @Query("DELETE FROM audit_log")
    suspend fun clearLogs()
}

@Database(entities = [ProgressEntity::class, AuditLogEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
    abstract fun auditDao(): AuditDao
}
