// ============================================================
// FILE: android/.../accessibility/FocusAccessibilityService.kt
// PURPOSE: Core detection engine — watches foreground app/window,
//          decides block/allow via BlockDecisionEngine
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
package com.focusforge.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.focusforge.app.overlay.BlockOverlayService

class FocusAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FocusAccessibility"

        // Singleton reference for Flutter to query state
        @Volatile
        var instance: FocusAccessibilityService? = null
            private set

        // Block event counter for dashboard
        var blockEventCount: Int = 0
            private set

        // Current foreground package
        var currentForegroundPackage: String? = null
            private set

        /**
         * Apps this service has bundled detection rules for. This is
         * the maximum set the service will ever watch — a superset of
         * the user's *currently* blocked apps, refreshed against
         * whenever session state changes, so newly-blocked apps aren't
         * silently ignored just because they weren't blocked when the
         * service first connected. IMPORTANT: previously this filter
         * was a hardcoded, never-updated list set once in
         * onServiceConnected() — meaning any app the user later chose
         * to block that wasn't already in that list would never
         * generate accessibility events at all, so it could never
         * actually be blocked. This is why blocking was unreliable.
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
        applyWatchedPackages(emptySet())
        Log.i(TAG, "FocusAccessibilityService connected")
    }

    /**
     * Rebuilds the accessibility service's watched-package filter to
     * cover both the bundled known targets AND whatever the user has
     * currently selected to block — so custom-blocked apps (added via
     * App Blocker, not just the built-in Shorts/Reels list) actually
     * generate events and can be intercepted. Passing null/empty
     * currentlyBlocked still watches all KNOWN_TARGET_PACKAGES.
     */
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
        // Re-apply the watched-package filter so any newly blocked app
        // (even one outside the bundled known-targets list) starts
        // generating accessibility events immediately.
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
