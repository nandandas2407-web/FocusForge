// ============================================================
// FILE: app/src/main/java/com/example/data/dao/FocusDao.kt
// PURPOSE: Room DAO providing reactive Flow queries for apps, tasks, sessions,
//          calendar, streaks, and theme settings.
// CREATED: 2026-08-09
// ============================================================

package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {

    // Blocked Apps
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isFullyBlocked = 1 OR isReelsBlocked = 1 OR isShortsBlocked = 1")
    fun getActiveBlockedAppsSync(): List<BlockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedAppEntity)

    @Query("UPDATE blocked_apps SET timesBlockedToday = timesBlockedToday + 1 WHERE packageName = :packageName")
    suspend fun incrementBlockedCount(packageName: String)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedAppEntity)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY startTimeMs DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity): Long

    @Update
    suspend fun updateFocusSession(session: FocusSessionEntity)

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMs ASC, createdTimeMs DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // Calendar Events
    @Query("SELECT * FROM calendar_events ORDER BY startTimeMs ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEventEntity): Long

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteCalendarEventById(id: Long)

    // Streak & Goals
    @Query("SELECT * FROM streak_goals WHERE id = 1")
    fun getStreakGoal(): Flow<StreakGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreakGoal(streak: StreakGoalEntity)

    // Theme Settings
    @Query("SELECT * FROM theme_settings WHERE id = 1")
    fun getThemeSettings(): Flow<ThemeSettingsEntity?>

    @Query("SELECT * FROM theme_settings WHERE id = 1")
    fun getThemeSettingsSync(): ThemeSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateThemeSettings(settings: ThemeSettingsEntity)

    // YouTube Whitelist
    @Query("SELECT * FROM youtube_whitelist ORDER BY channelTitle ASC")
    fun getYoutubeWhitelist(): Flow<List<YoutubeWhitelistEntity>>

    @Query("SELECT * FROM youtube_whitelist")
    fun getYoutubeWhitelistSync(): List<YoutubeWhitelistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertYoutubeWhitelist(item: YoutubeWhitelistEntity)

    @Delete
    suspend fun deleteYoutubeWhitelist(item: YoutubeWhitelistEntity)

    // Website Blocklist
    @Query("SELECT * FROM website_blocks ORDER BY domain ASC")
    fun getWebsiteBlocks(): Flow<List<WebsiteBlockEntity>>

    @Query("SELECT * FROM website_blocks")
    fun getWebsiteBlocksSync(): List<WebsiteBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebsiteBlock(item: WebsiteBlockEntity)

    @Delete
    suspend fun deleteWebsiteBlock(item: WebsiteBlockEntity)
}
