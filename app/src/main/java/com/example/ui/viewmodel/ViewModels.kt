// ============================================================
// FILE: app/src/main/java/com/example/ui/viewmodel/ViewModels.kt
// PURPOSE: ViewModels for FocusForge state management (App Blocker, Focus Timer,
//          Tasks, Calendar, Stats, Theme).
// CREATED: 2026-08-09
// ============================================================

package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.FocusDatabase
import com.example.data.entity.*
import com.example.data.repository.CalendarRepository
import com.example.data.repository.FocusRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.UsageStatsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FocusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FocusRepository(FocusDatabase.getDatabase(application).focusDao())

    val blockedApps: StateFlow<List<BlockedAppEntity>> = repository.allBlockedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streakGoal: StateFlow<StreakGoalEntity?> = repository.streakGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeSettings: StateFlow<ThemeSettingsEntity?> = repository.themeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val youtubeWhitelist: StateFlow<List<YoutubeWhitelistEntity>> = repository.youtubeWhitelist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val websiteBlocks: StateFlow<List<WebsiteBlockEntity>> = repository.websiteBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Timer State
    private val _timerSeconds = MutableStateFlow(25 * 60)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerMode = MutableStateFlow("POMODORO") // POMODORO, SHORT_BREAK, LONG_BREAK
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    init {
        // Sync system installed launcher apps into DB so user can block any installed app
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            val myPackage = context.packageName

            val existingApps = repository.allBlockedApps.first().associateBy { it.packageName }

            resolveInfos.forEach { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val pkg = appInfo.packageName
                if (pkg != myPackage && !existingApps.containsKey(pkg)) {
                    val label = resolveInfo.loadLabel(pm).toString()
                    val category = when {
                        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 -> "System"
                        pkg.contains("game", ignoreCase = true) -> "Games"
                        pkg.contains("social", ignoreCase = true) || pkg.contains("facebook") || pkg.contains("instagram") || pkg.contains("twitter") || pkg.contains("snapchat") || pkg.contains("reddit") || pkg.contains("tiktok") -> "Social"
                        pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("video") || pkg.contains("twitch") -> "Video"
                        pkg.contains("shop") || pkg.contains("amazon") -> "Shopping"
                        else -> "Social"
                    }
                    val isDefaultBlocked = pkg == "com.zhiliaoapp.musically" || pkg == "com.ss.android.ugc.trill"
                    val isReelsBlocked = pkg == "com.instagram.android"
                    val isShortsBlocked = pkg == "com.google.android.youtube"

                    repository.insertOrUpdateBlockedApp(
                        BlockedAppEntity(
                            packageName = pkg,
                            appName = label,
                            category = category,
                            isFullyBlocked = isDefaultBlocked,
                            isReelsBlocked = isReelsBlocked,
                            isShortsBlocked = isShortsBlocked
                        )
                    )
                }
            }

            // Seed default Youtube study whitelist
            repository.youtubeWhitelist.first().let { whitelist ->
                if (whitelist.isEmpty()) {
                    repository.addYoutubeWhitelist(YoutubeWhitelistEntity("UC_x5XG1OV2P6uZZ5FSM9Ttw", "MIT OpenCourseWare"))
                    repository.addYoutubeWhitelist(YoutubeWhitelistEntity("UCsXVk37bltHxD1rDPwtNM8Q", "Kurzgesagt - In a Nutshell"))
                    repository.addYoutubeWhitelist(YoutubeWhitelistEntity("UC8butISFwT-Wl7EV0hUK0BQ", "freeCodeCamp.org"))
                }
            }

            // Seed default website block list
            repository.websiteBlocks.first().let { blocks ->
                if (blocks.isEmpty()) {
                    repository.addWebsiteBlock(WebsiteBlockEntity("reddit.com", "Social"))
                    repository.addWebsiteBlock(WebsiteBlockEntity("twitter.com", "Social"))
                    repository.addWebsiteBlock(WebsiteBlockEntity("x.com", "Social"))
                }
            }
        }
    }

    fun toggleAppBlocked(packageName: String, appName: String, category: String, isFullyBlocked: Boolean) {
        viewModelScope.launch {
            val existing = blockedApps.value.find { it.packageName == packageName }
            val updated = existing?.copy(isFullyBlocked = isFullyBlocked)
                ?: BlockedAppEntity(packageName = packageName, appName = appName, category = category, isFullyBlocked = isFullyBlocked)
            repository.insertOrUpdateBlockedApp(updated)
        }
    }

    fun toggleReelsBlocked(packageName: String, appName: String, isReelsBlocked: Boolean) {
        viewModelScope.launch {
            val existing = blockedApps.value.find { it.packageName == packageName }
            val updated = existing?.copy(isReelsBlocked = isReelsBlocked)
                ?: BlockedAppEntity(packageName = packageName, appName = appName, isReelsBlocked = isReelsBlocked)
            repository.insertOrUpdateBlockedApp(updated)
        }
    }

    fun toggleShortsBlocked(packageName: String, appName: String, isShortsBlocked: Boolean) {
        viewModelScope.launch {
            val existing = blockedApps.value.find { it.packageName == packageName }
            val updated = existing?.copy(isShortsBlocked = isShortsBlocked)
                ?: BlockedAppEntity(packageName = packageName, appName = appName, isShortsBlocked = isShortsBlocked)
            repository.insertOrUpdateBlockedApp(updated)
        }
    }

    fun setTimerMode(mode: String, minutes: Int) {
        _timerMode.value = mode
        _timerSeconds.value = minutes * 60
        _isTimerRunning.value = false
    }

    fun startTimer() {
        _isTimerRunning.value = true
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
    }

    fun resetTimer(minutes: Int = 25) {
        _isTimerRunning.value = false
        _timerSeconds.value = minutes * 60
    }

    fun tickTimer() {
        if (_isTimerRunning.value && _timerSeconds.value > 0) {
            _timerSeconds.value -= 1
            if (_timerSeconds.value == 0) {
                _isTimerRunning.value = false
                // Log focus session
                viewModelScope.launch {
                    repository.addFocusSession(
                        FocusSessionEntity(
                            title = "Focus Session (${_timerMode.value})",
                            durationMinutes = 25,
                            isCompleted = true
                        )
                    )
                }
            }
        }
    }

    fun toggleYoutubeStudyMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = themeSettings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(isYoutubeStudyModeEnabled = enabled))
        }
    }

    fun toggleGlobalBlocker(enabled: Boolean) {
        viewModelScope.launch {
            val current = themeSettings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(isGlobalBlockerEnabled = enabled))
        }
    }

    fun updateStreakGoal(goalMinutes: Int) {
        viewModelScope.launch {
            val current = streakGoal.value ?: StreakGoalEntity()
            repository.saveStreakGoal(current.copy(dailyScreenTimeGoalMinutes = goalMinutes))
        }
    }

    fun addYoutubeChannel(channelId: String, channelTitle: String) {
        viewModelScope.launch {
            repository.addYoutubeWhitelist(YoutubeWhitelistEntity(channelId, channelTitle))
        }
    }

    fun removeYoutubeChannel(channel: YoutubeWhitelistEntity) {
        viewModelScope.launch {
            repository.deleteYoutubeWhitelist(channel)
        }
    }

    fun addWebsiteBlock(domain: String, category: String) {
        viewModelScope.launch {
            repository.addWebsiteBlock(WebsiteBlockEntity(domain, category))
        }
    }

    fun removeWebsiteBlock(item: WebsiteBlockEntity) {
        viewModelScope.launch {
            repository.deleteWebsiteBlock(item)
        }
    }
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TaskRepository(FocusDatabase.getDatabase(application).focusDao())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed sample tasks if empty
        viewModelScope.launch {
            repository.allTasks.first().let { current ->
                if (current.isEmpty()) {
                    repository.insertTask(
                        TaskEntity(
                            title = "Complete Calculus Chapter 4 Exercises",
                            notes = "Problems 1 to 15, focusing on integration techniques",
                            priority = "HIGH",
                            category = "Math",
                            dueDateMs = System.currentTimeMillis() + 86400000 * 2
                        )
                    )
                    repository.insertTask(
                        TaskEntity(
                            title = "Review Physics Thermodynamics Notes",
                            notes = "Prep for Friday quiz",
                            priority = "MEDIUM",
                            category = "Physics",
                            dueDateMs = System.currentTimeMillis() + 86400000 * 3
                        )
                    )
                }
            }
        }
    }

    fun addTask(title: String, notes: String, priority: String, category: String, estimatedMinutes: Int) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    notes = notes,
                    priority = priority,
                    category = category,
                    estimatedMinutes = estimatedMinutes,
                    dueDateMs = System.currentTimeMillis() + 86400000
                )
            )
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CalendarRepository(FocusDatabase.getDatabase(application).focusDao())

    val events: StateFlow<List<CalendarEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.allEvents.first().let { current ->
                if (current.isEmpty()) {
                    repository.insertEvent(
                        CalendarEventEntity(
                            title = "Final Physics Exam",
                            eventType = "EXAM",
                            startTimeMs = System.currentTimeMillis() + 86400000 * 5,
                            endTimeMs = System.currentTimeMillis() + 86400000 * 5 + 7200000,
                            dateString = "2026-08-14",
                            isExamCountdown = true,
                            notes = "Exam Hall 3B"
                        )
                    )
                    repository.insertEvent(
                        CalendarEventEntity(
                            title = "Organic Chemistry Study Block",
                            eventType = "STUDY_BLOCK",
                            startTimeMs = System.currentTimeMillis() + 86400000,
                            endTimeMs = System.currentTimeMillis() + 86400000 + 5400000,
                            dateString = "2026-08-10",
                            notes = "Focus on reaction mechanisms"
                        )
                    )
                }
            }
        }
    }

    fun addEvent(title: String, eventType: String, dateString: String, isExam: Boolean) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.insertEvent(
                CalendarEventEntity(
                    title = title,
                    eventType = eventType,
                    startTimeMs = now + 86400000,
                    endTimeMs = now + 86400000 + 3600000,
                    dateString = dateString,
                    isExamCountdown = isExam
                )
            )
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteEvent(id)
        }
    }
}

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val usageRepo = UsageStatsRepository(application)

    private val _usageStats = MutableStateFlow<List<com.example.data.repository.AppUsageInfo>>(emptyList())
    val usageStats: StateFlow<List<com.example.data.repository.AppUsageInfo>> = _usageStats.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        if (usageRepo.hasUsageAccessPermission()) {
            _usageStats.value = usageRepo.getTodayUsageStats()
        } else {
            _usageStats.value = emptyList()
        }
    }
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FocusRepository(FocusDatabase.getDatabase(application).focusDao())

    val settings: StateFlow<ThemeSettingsEntity?> = repository.themeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateWallpaperPreset(preset: String) {
        viewModelScope.launch {
            val current = settings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(wallpaperPreset = preset))
        }
    }

    fun updateCustomWallpaper(path: String?) {
        viewModelScope.launch {
            val current = settings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(customWallpaperPath = path))
        }
    }

    fun updateFocusWallpaperOverride(path: String?) {
        viewModelScope.launch {
            val current = settings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(focusTimerWallpaperOverride = path))
        }
    }

    fun updateAccentColor(hex: String) {
        viewModelScope.launch {
            val current = settings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(accentColorHex = hex))
        }
    }

    fun updateGlassBlur(blur: Float) {
        viewModelScope.launch {
            val current = settings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(glassBlurSigma = blur))
        }
    }

    fun updateGlassOpacity(opacity: Float) {
        viewModelScope.launch {
            val current = settings.value ?: ThemeSettingsEntity()
            repository.saveThemeSettings(current.copy(glassOpacity = opacity))
        }
    }
}
