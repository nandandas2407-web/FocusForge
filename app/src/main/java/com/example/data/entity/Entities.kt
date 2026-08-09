// ============================================================
// FILE: app/src/main/java/com/example/data/entity/Entities.kt
// PURPOSE: Room database entities for FocusForge app blocking, study sessions,
//          tasks, calendar events, streak tracking, theme settings, and whitelists.
// CREATED: 2026-08-09
// ============================================================

package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val category: String = "Social",
    val isFullyBlocked: Boolean = true,
    val isReelsBlocked: Boolean = false,
    val isShortsBlocked: Boolean = false,
    val allowMinutesPerDay: Int = 0,
    val timesBlockedToday: Int = 0
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val durationMinutes: Int,
    val mode: String = "POMODORO", // POMODORO, STOPWATCH, TIMER
    val startTimeMs: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isStrictMode: Boolean = false,
    val associatedTaskId: Long? = null,
    val notes: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDateMs: Long? = null,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val category: String = "Study",
    val isCompleted: Boolean = false,
    val estimatedMinutes: Int = 25,
    val createdTimeMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val eventType: String = "STUDY_BLOCK", // STUDY_BLOCK, EXAM, CLASS, DEADLINE
    val startTimeMs: Long,
    val endTimeMs: Long,
    val dateString: String, // e.g. "2026-08-09"
    val notes: String = "",
    val isExamCountdown: Boolean = false
)

@Entity(tableName = "streak_goals")
data class StreakGoalEntity(
    @PrimaryKey val id: Int = 1,
    val dailyScreenTimeGoalMinutes: Int = 120,
    val currentStreakDays: Int = 3,
    val bestStreakDays: Int = 12,
    val lastActiveDateString: String = "",
    val totalFocusMinutesAllTime: Int = 450
)

@Entity(tableName = "theme_settings")
data class ThemeSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkTheme: Boolean = true,
    val accentColorHex: String = "#6366F1", // Indigo Accent
    val glassBlurSigma: Float = 24f,
    val glassOpacity: Float = 0.15f,
    val wallpaperPreset: String = "COSMIC_NEON", // COSMIC_NEON, LOFI_STUDY, AURORA, CUSTOM
    val customWallpaperPath: String? = null,
    val focusTimerWallpaperOverride: String? = null,
    val isStrictModeEnabled: Boolean = false,
    val isUninstallProtectionActive: Boolean = false,
    val isYoutubeStudyModeEnabled: Boolean = false,
    val isGlobalBlockerEnabled: Boolean = true
)

@Entity(tableName = "youtube_whitelist")
data class YoutubeWhitelistEntity(
    @PrimaryKey val channelId: String,
    val channelTitle: String,
    val category: String = "Education",
    val addedTimestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "website_blocks")
data class WebsiteBlockEntity(
    @PrimaryKey val domain: String,
    val category: String = "Distracting",
    val isBlocked: Boolean = true
)
