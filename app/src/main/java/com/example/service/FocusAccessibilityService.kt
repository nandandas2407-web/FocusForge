// ============================================================
// FILE: app/src/main/java/com/example/service/FocusAccessibilityService.kt
// PURPOSE: Core Android AccessibilityService listening for active foreground apps,
//          evaluating block rules, bouncing user home/back, and showing overlay.
// CREATED: 2026-08-09
// ============================================================

package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.data.db.FocusDatabase
import com.example.data.entity.WebsiteBlockEntity
import com.example.data.entity.YoutubeWhitelistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var blockedPackages = setOf<String>()
    private var reelsBlockedPackages = setOf<String>()
    private var shortsBlockedPackages = setOf<String>()
    private var isGlobalBlockerEnabled = true
    private var isYoutubeStudyModeEnabled = false
    private var youtubeWhitelist = listOf<YoutubeWhitelistEntity>()
    private var websiteBlocks = listOf<WebsiteBlockEntity>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("FocusAccessibility", "FocusForge Accessibility Service connected")

        val db = FocusDatabase.getDatabase(applicationContext)

        serviceScope.launch {
            db.focusDao().getThemeSettings().collect { settings ->
                isGlobalBlockerEnabled = settings?.isGlobalBlockerEnabled ?: true
                isYoutubeStudyModeEnabled = settings?.isYoutubeStudyModeEnabled ?: false
            }
        }

        serviceScope.launch {
            db.focusDao().getAllBlockedApps().collect { list ->
                blockedPackages = list.filter { it.isFullyBlocked }.map { it.packageName }.toSet()
                reelsBlockedPackages = list.filter { it.isReelsBlocked }.map { it.packageName }.toSet()
                shortsBlockedPackages = list.filter { it.isShortsBlocked }.map { it.packageName }.toSet()
            }
        }

        serviceScope.launch {
            db.focusDao().getYoutubeWhitelist().collect { list ->
                youtubeWhitelist = list
            }
        }

        serviceScope.launch {
            db.focusDao().getWebsiteBlocks().collect { list ->
                websiteBlocks = list
            }
        }
    }

    fun refreshBlockedList() {
        // Kept for backward compatibility
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isGlobalBlockerEnabled) return
        event ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_CLICKED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return // Do not block FocusForge itself

        val className = event.className?.toString()
        val rootNode = rootInActiveWindow

        if (packageName == "com.google.android.youtube") {
            Log.d("FocusAccessibility", "YouTube event=${event.eventType} class=$className studyMode=$isYoutubeStudyModeEnabled")
        }

        val decision = AppDetectionRules.evaluate(
            packageName = packageName,
            className = className,
            rootNode = rootNode,
            blockedPackages = blockedPackages,
            reelsBlockedPackages = reelsBlockedPackages,
            shortsBlockedPackages = shortsBlockedPackages,
            isYoutubeStudyModeEnabled = isYoutubeStudyModeEnabled,
            youtubeWhitelist = youtubeWhitelist,
            websiteBlocks = websiteBlocks
        )

        if (decision !is BlockDecision.Allow && packageName == "com.google.android.youtube") {
            val reason = when (decision) {
                is BlockDecision.BlockSubScreen -> decision.reason
                is BlockDecision.BlockWholeApp -> decision.reason
                else -> ""
            }
            Log.w("FocusAccessibility", "YouTube BLOCKED: $reason")
        }

        val pm = applicationContext.packageManager
        val appName = try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }

        when (decision) {
            is BlockDecision.BlockWholeApp -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
                recordBlockInDb(packageName)
                showOverlayNotification(packageName, appName, decision.reason)
            }
            is BlockDecision.BlockSubScreen -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
                recordBlockInDb(packageName)
                showOverlayNotification(packageName, appName, "${decision.subScreenName}: ${decision.reason}")
            }
            is BlockDecision.Allow -> Unit
        }
    }

    private fun recordBlockInDb(pkg: String) {
        serviceScope.launch {
            try {
                val db = FocusDatabase.getDatabase(applicationContext)
                db.focusDao().incrementBlockedCount(pkg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showOverlayNotification(pkg: String, appName: String, reason: String) {
        val intent = Intent(applicationContext, BlockOverlayService::class.java).apply {
            putExtra("PACKAGE_NAME", pkg)
            putExtra("APP_NAME", appName)
            putExtra("REASON", reason)
        }
        try {
            applicationContext.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e("FocusAccessibility", "Could not start overlay service", e)
        }
    }

    override fun onInterrupt() {
        Log.d("FocusAccessibility", "FocusForge Accessibility Service interrupted")
    }
}
