// ============================================================
// FILE: app/src/main/java/com/example/data/repository/Repositories.kt
// PURPOSE: Repositories providing abstracted reactive access to Room DAO
//          and UsageStatsManager data for ViewModels.
// CREATED: 2026-08-09
// ============================================================

package com.example.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.data.dao.FocusDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FocusRepository(private val dao: FocusDao) {

    val allBlockedApps: Flow<List<BlockedAppEntity>> = dao.getAllBlockedApps()
    val allSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()
    val streakGoal: Flow<StreakGoalEntity?> = dao.getStreakGoal()
    val themeSettings: Flow<ThemeSettingsEntity?> = dao.getThemeSettings()
    val youtubeWhitelist: Flow<List<YoutubeWhitelistEntity>> = dao.getYoutubeWhitelist()
    val websiteBlocks: Flow<List<WebsiteBlockEntity>> = dao.getWebsiteBlocks()

    suspend fun insertOrUpdateBlockedApp(app: BlockedAppEntity) = dao.insertBlockedApp(app)
    suspend fun deleteBlockedApp(app: BlockedAppEntity) = dao.deleteBlockedApp(app)
    suspend fun incrementBlockedCount(packageName: String) = dao.incrementBlockedCount(packageName)

    suspend fun addFocusSession(session: FocusSessionEntity): Long = dao.insertFocusSession(session)
    suspend fun updateFocusSession(session: FocusSessionEntity) = dao.updateFocusSession(session)

    suspend fun saveStreakGoal(streak: StreakGoalEntity) = dao.insertOrUpdateStreakGoal(streak)
    suspend fun saveThemeSettings(settings: ThemeSettingsEntity) = dao.insertOrUpdateThemeSettings(settings)

    suspend fun addYoutubeWhitelist(item: YoutubeWhitelistEntity) = dao.insertYoutubeWhitelist(item)
    suspend fun deleteYoutubeWhitelist(item: YoutubeWhitelistEntity) = dao.deleteYoutubeWhitelist(item)

    suspend fun addWebsiteBlock(item: WebsiteBlockEntity) = dao.insertWebsiteBlock(item)
    suspend fun deleteWebsiteBlock(item: WebsiteBlockEntity) = dao.deleteWebsiteBlock(item)
}

class TaskRepository(private val dao: FocusDao) {
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(id: Long) = dao.deleteTaskById(id)
}

class CalendarRepository(private val dao: FocusDao) {
    val allEvents: Flow<List<CalendarEventEntity>> = dao.getAllCalendarEvents()
    suspend fun insertEvent(event: CalendarEventEntity): Long = dao.insertCalendarEvent(event)
    suspend fun deleteEvent(id: Long) = dao.deleteCalendarEventById(id)
}

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalTimeInForegroundMs: Long,
    val lastTimeUsedMs: Long
)

class UsageStatsRepository(private val context: Context) {

    fun hasUsageAccessPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager ?: return false
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun getTodayUsageStats(): List<AppUsageInfo> {
        if (!hasUsageAccessPermission()) return emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis()

        val queryStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val pm = context.packageManager

        if (!queryStats.isNullOrEmpty()) {
            return queryStats
                .filter { it.totalTimeInForeground > 1000 }
                .map { stat ->
                    val label = try {
                        val appInfo = pm.getApplicationInfo(stat.packageName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        stat.packageName
                    }
                    AppUsageInfo(
                        packageName = stat.packageName,
                        appName = label,
                        totalTimeInForegroundMs = stat.totalTimeInForeground,
                        lastTimeUsedMs = stat.lastTimeUsed
                    )
                }
                .sortedByDescending { it.totalTimeInForegroundMs }
        }

        return emptyList()
    }

    private fun getInstalledAppsFallback(): List<AppUsageInfo> {
        val pm = context.packageManager
        val mainIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        return resolveInfos.mapNotNull { info ->
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) return@mapNotNull null
            val label = info.loadLabel(pm).toString()
            AppUsageInfo(
                packageName = pkg,
                appName = label,
                totalTimeInForegroundMs = 0L,
                lastTimeUsedMs = System.currentTimeMillis()
            )
        }.distinctBy { it.packageName }.take(12)
    }
}
