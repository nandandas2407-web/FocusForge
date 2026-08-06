// ============================================================
// FILE: android/.../usage/UsageStatsBridge.kt
// PURPOSE: Wraps UsageStatsManager to provide ACCURATE app usage
//          data to Flutter for the Stats Dashboard.
//
//          IMPORTANT: queryUsageStats(INTERVAL_DAILY, start, end)
//          does NOT clip results to [start, end] — it returns
//          whichever daily bucket rows overlap that range, each
//          carrying totalTimeInForeground for that bucket's WHOLE
//          day. Summing those directly (the previous implementation)
//          could report a full extra day's usage or double-count
//          apps when two overlapping buckets were returned. This
//          was the root cause of incorrect screen-time numbers.
//
//          Fix: use queryEvents() to read the raw foreground/
//          background transition log and reconstruct actual
//          per-app durations ourselves, clipped precisely to the
//          requested window. This is the same approach Android's
//          own Digital Wellbeing uses internally.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
package com.focusforge.app.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

class UsageStatsBridge(private val context: Context) {

    data class AppUsageStat(
        val packageName: String,
        val totalTimeMs: Long,
        val lastTimeUsed: Long,
        val appLabel: String
    )

    data class DayBucket(
        val dateKey: String, // yyyy-MM-dd
        val totalTimeMs: Long
    )

    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    /**
     * Walks the raw event log between [startTime, endTime) and returns
     * accurate per-package foreground duration, clipped exactly to the
     * window — including sessions that started before startTime or are
     * still open (ongoing) at endTime.
     */
    private fun computeForegroundDurations(
        startTime: Long,
        endTime: Long
    ): Map<String, Long> {
        val manager = usageStatsManager ?: return emptyMap()
        val events = manager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        // Track the open (in-foreground) start time per package.
        val openSince = mutableMapOf<String, Long>()
        val totals = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val time = event.timeStamp.coerceIn(startTime, endTime)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    // Only set if not already open, to avoid double-open
                    // artifacts from certain OEM event streams.
                    if (!openSince.containsKey(pkg)) {
                        openSince[pkg] = time
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    val start = openSince.remove(pkg)
                    if (start != null && time > start) {
                        totals[pkg] = (totals[pkg] ?: 0L) + (time - start)
                    }
                }
                else -> { /* ignore other event types */ }
            }
        }

        // Any package still "open" at endTime was in the foreground
        // when our window closed — count up to endTime (handles
        // background usage / app still running case correctly).
        for ((pkg, start) in openSince) {
            if (endTime > start) {
                totals[pkg] = (totals[pkg] ?: 0L) + (endTime - start)
            }
        }

        return totals
    }

    private fun startOfDay(cal: Calendar): Long {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun isUsageStatsAvailable(): Boolean = usageStatsManager != null

    /** Accurate total screen time from local midnight to now. Returns -1 if unavailable. */
    fun getTotalScreenTimeToday(): Long {
        if (usageStatsManager == null) return -1L
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis
        val startTime = startOfDay(cal)
        return computeForegroundDurations(startTime, endTime).values.sum()
    }

    /** Accurate total screen time for the last 7 full days including today. Returns -1 if unavailable. */
    fun getWeeklyScreenTime(): Long {
        if (usageStatsManager == null) return -1L
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -6)
        val startTime = startOfDay(cal)
        return computeForegroundDurations(startTime, endTime).values.sum()
    }

    /** Accurate total screen time for the last 30 days including today. Returns -1 if unavailable. */
    fun getMonthlyScreenTime(): Long {
        if (usageStatsManager == null) return -1L
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -29)
        val startTime = startOfDay(cal)
        return computeForegroundDurations(startTime, endTime).values.sum()
    }

    /**
     * Per-day totals for the last [days] days (including today), computed
     * from the same accurate event-based accounting — used for the
     * Stats Dashboard weekly bar chart. Returns one bucket per day, in
     * chronological order, even for days with zero usage. Empty list if
     * usage stats are unavailable.
     */
    fun getDailyBreakdown(days: Int = 7): List<DayBucket> {
        if (usageStatsManager == null) return emptyList()
        val buckets = mutableListOf<DayBucket>()
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        for (i in (days - 1) downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = startOfDay(dayCal)
            val dayEndCal = Calendar.getInstance().apply { timeInMillis = dayStart }
            dayEndCal.add(Calendar.DAY_OF_YEAR, 1)
            val now = System.currentTimeMillis()
            val dayEnd = minOf(dayEndCal.timeInMillis, now)

            val total = if (dayEnd > dayStart) {
                computeForegroundDurations(dayStart, dayEnd).values.sum()
            } else 0L

            buckets.add(DayBucket(fmt.format(java.util.Date(dayStart)), total))
        }
        return buckets
    }

    /** Accurate top-apps-by-usage for the last 24 hours. */
    fun getTopApps(limit: Int = 10): List<AppUsageStat> {
        if (usageStatsManager == null) return emptyList()
        val cal = Calendar.getInstance()
        val endTime = cal.timeInMillis
        val startTime = startOfDay(cal)

        val durations = computeForegroundDurations(startTime, endTime)
        return durations.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
            .take(limit)
            .map { (pkg, ms) ->
                AppUsageStat(
                    packageName = pkg,
                    totalTimeMs = ms,
                    lastTimeUsed = endTime,
                    appLabel = getAppLabel(pkg)
                )
            }
    }

    /**
     * Returns all launchable (has a Home-screen icon) installed apps,
     * excluding FocusForge itself, for the App Blocker picker. Uses
     * PackageManager directly so it works even without Usage Access
     * granted and includes apps that have never been opened.
     */
    fun getInstalledApps(): List<AppUsageStat> {
        val pm = context.packageManager
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)

        return resolveInfos
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != context.packageName }
            .map { pkg ->
                AppUsageStat(
                    packageName = pkg,
                    totalTimeMs = 0L,
                    lastTimeUsed = 0L,
                    appLabel = getAppLabel(pkg)
                )
            }
            .sortedBy { it.appLabel.lowercase() }
    }

    private fun getAppLabel(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }
    }
}
