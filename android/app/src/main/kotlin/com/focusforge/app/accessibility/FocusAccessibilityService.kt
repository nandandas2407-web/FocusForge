// ============================================================
// FILE: android/.../accessibility/FocusAccessibilityService.kt
// PURPOSE: Core detection engine — watches foreground app/window,
//          decides block/allow via BlockDecisionEngine.
//          Persists session state to SharedPreferences so blocking
//          survives service restarts and device reboots.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-06
// ============================================================
package com.focusforge.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focusforge.app.overlay.BlockOverlayService

class FocusAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FocusAccessibility"
        private const val PREFS_NAME = "FlutterSharedPreferences"
        private const val KEY_SESSION_ACTIVE = "flutter.rules_session_active"
        private const val KEY_STRICT_MODE = "flutter.rules_strict_mode"
        private const val KEY_STUDY_MODE = "flutter.rules_youtube_study_mode"
        private const val KEY_BLOCKED_APPS = "flutter.rules_blocked_apps"
        private const val KEY_SHORTS_BLOCKED = "flutter.rules_blocked_shorts_apps"
        private const val KEY_WHITELIST = "flutter.rules_youtube_whitelist"

        @Volatile
        var instance: FocusAccessibilityService? = null
            private set

        var blockEventCount: Int = 0
            private set

        var currentForegroundPackage: String? = null
            private set

        /**
         * Apps this service has bundled detection rules for. This is
         * the maximum set the service will ever watch — a superset of
         * the user's *currently* blocked apps, refreshed against
         * whenever session state changes, so newly-blocked apps aren't
         * silently ignored just because they weren't blocked when the
         * service first connected.
         */
        private val KNOWN_TARGET_PACKAGES = setOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically",
            "com.snapchat.android",
            "com.facebook.katana",
            "com.twitter.android",
        )
    }

    private val detectionRules = AppDetectionRules()
    private val blockDecisionEngine = BlockDecisionEngine()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        // Restore persisted session state so blocking survives service restarts.
        restoreSessionState()
        Log.i(TAG, "FocusAccessibilityService connected")
    }

    /**
     * Reads persisted session state from FlutterSharedPreferences and
     * pushes it into the BlockDecisionEngine, so blocking is restored
     * even if the accessibility service was killed and restarted by the OS.
     */
    private fun restoreSessionState() {
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val sessionActive = prefs.getBoolean(KEY_SESSION_ACTIVE, false)
            if (!sessionActive) {
                applyWatchedPackages(emptySet())
                return
            }

            val strictMode = prefs.getBoolean(KEY_STRICT_MODE, false)
            val studyMode = prefs.getBoolean(KEY_STUDY_MODE, false)

            // Parse blocked apps list (stored as a JSON array string by Flutter)
            val blockedAppsRaw = prefs.getString(KEY_BLOCKED_APPS, null)
            val blockedPkgs = parseStringList(blockedAppsRaw)

            val shortsBlockedRaw = prefs.getString(KEY_SHORTS_BLOCKED, null)
            val shortsPkgs = parseStringList(shortsBlockedRaw)

            val whitelistRaw = prefs.getString(KEY_WHITELIST, null)
            val whitelist = parseStringList(whitelistRaw)

            blockDecisionEngine.updateSessionState(
                active = true,
                blockedPkgs = blockedPkgs,
                shortsBlockedPkgs = shortsPkgs,
                strictMode = strictMode,
                ytStudyMode = studyMode,
                ytWhitelist = whitelist.toList()
            )
            applyWatchedPackages(blockedPkgs + shortsPkgs)
            Log.i(TAG, "Restored session state: blocked=${blockedPkgs.size} shorts=${shortsPkgs.size} strict=$strictMode study=$studyMode")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore session state", e)
            applyWatchedPackages(emptySet())
        }
    }

    /**
     * Parses a Flutter SharedPreferences string list value.
     * Flutter stores List<String> as either a JSON array string
     * or via its own encoding — handles both formats.
     */
    private fun parseStringList(raw: String?): Set<String> {
        if (raw.isNullOrBlank()) return emptySet()
        // Flutter SharedPreferences can store lists as JSON arrays
        return try {
            val cleaned = raw.removePrefix("[").removeSuffix("]").trim()
            if (cleaned.isEmpty()) return emptySet()
            cleaned.split(",").map {
                it.trim().removePrefix("\"").removeSuffix("\"")
            }.filter { it.isNotEmpty() }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun applyWatchedPackages(currentlyBlocked: Set<String>) {
        val watchSet = (KNOWN_TARGET_PACKAGES + currentlyBlocked).toTypedArray()
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            packageNames = watchSet
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == "com.focusforge.app") return

        // Check if this package is temporarily unlocked (5-min grace period)
        if (BlockOverlayService.isTemporarilyUnlocked(packageName)) return

        currentForegroundPackage = packageName

        val rootNode = rootInActiveWindow ?: return

        val decision = blockDecisionEngine.evaluate(
            packageName = packageName,
            rootNode = rootNode,
            event = event,
            rules = detectionRules
        )

        when (decision) {
            is BlockDecision.BlockWholeApp -> {
                blockEventCount++
                performGlobalAction(GLOBAL_ACTION_HOME)
                BlockOverlayService.show(
                    context = this,
                    packageName = packageName,
                    reason = decision.reason,
                    isStrictMode = blockDecisionEngine.isStrictModeActive()
                )
                broadcastBlockEvent(packageName, decision.reason, "whole_app")
            }
            is BlockDecision.BlockSubScreen -> {
                blockEventCount++
                performGlobalAction(GLOBAL_ACTION_BACK)
                BlockOverlayService.showTransient(
                    context = this,
                    packageName = packageName,
                    subScreen = decision.subScreenName,
                    reason = decision.reason
                )
                broadcastBlockEvent(packageName, decision.reason, decision.subScreenName)
            }
            is BlockDecision.Allow -> {
                // No action needed
            }
        }

        rootNode.recycle()
    }

    fun updateSessionState(
        active: Boolean,
        blockedPkgs: Set<String>,
        shortsBlockedPkgs: Set<String>,
        strictMode: Boolean,
        ytStudyMode: Boolean,
        ytWhitelist: List<String>
    ) {
        blockDecisionEngine.updateSessionState(
            active = active,
            blockedPkgs = blockedPkgs,
            shortsBlockedPkgs = shortsBlockedPkgs,
            strictMode = strictMode,
            ytStudyMode = ytStudyMode,
            ytWhitelist = ytWhitelist
        )
        applyWatchedPackages(blockedPkgs + shortsBlockedPkgs)
        Log.i(TAG, "Session state updated: active=$active blocked=${blockedPkgs.size} shortsBlocked=${shortsBlockedPkgs.size} strict=$strictMode ytStudy=$ytStudyMode")
    }

    override fun onInterrupt() {
        Log.w(TAG, "FocusAccessibilityService interrupted")
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    private fun broadcastBlockEvent(
        packageName: String,
        reason: String,
        subScreen: String
    ) {
        val intent = Intent("com.focusforge.app.BLOCK_EVENT").apply {
            putExtra("package_name", packageName)
            putExtra("reason", reason)
            putExtra("sub_screen", subScreen)
            putExtra("timestamp", System.currentTimeMillis())
            putExtra("total_blocks_today", blockEventCount)
        }
        sendBroadcast(intent)
    }
}
