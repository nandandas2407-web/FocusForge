// ============================================================
// FILE: android/.../accessibility/BlockDecisionEngine.kt
// PURPOSE: Evaluates whether the foreground app/sub-screen should
//          be blocked based on current block list and session state.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
package com.focusforge.app.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

sealed class BlockDecision {
    data class BlockWholeApp(val reason: String) : BlockDecision()
    data class BlockSubScreen(
        val subScreenName: String,
        val reason: String
    ) : BlockDecision()
    data object Allow : BlockDecision()
}

class BlockDecisionEngine {

    private var strictModeActive = false
    private var sessionActive = false
    private var blockedPackages: Set<String> = emptySet()
    private var shortsBlockedPackages: Set<String> = emptySet()
    private var youtubeWhitelist: List<String> = emptyList()
    private var youtubeStudyModeEnabled = false

    fun isStrictModeActive(): Boolean = strictModeActive

    fun updateSessionState(
        active: Boolean,
        blockedPkgs: Set<String>,
        shortsBlockedPkgs: Set<String>,
        strictMode: Boolean,
        ytStudyMode: Boolean,
        ytWhitelist: List<String>
    ) {
        sessionActive = active
        blockedPackages = blockedPkgs
        shortsBlockedPackages = shortsBlockedPkgs
        strictModeActive = strictMode
        youtubeStudyModeEnabled = ytStudyMode
        youtubeWhitelist = ytWhitelist
    }

    fun evaluate(
        packageName: String,
        rootNode: AccessibilityNodeInfo,
        event: AccessibilityEvent,
        rules: AppDetectionRules
    ): BlockDecision {
        if (!sessionActive) return BlockDecision.Allow
        if (packageName == "com.focusforge.app") return BlockDecision.Allow

        val appRule = rules.getRule(packageName)

        // YouTube Study Mode check
        if (youtubeStudyModeEnabled && packageName == "com.google.android.youtube") {
            return evaluateYouTubeStudyMode(rootNode, event)
        }

        // Full app block — from the App Blocker screen. Blocks the
        // whole app outright, regardless of any sub-screen rules.
        if (blockedPackages.contains(packageName)) {
            return BlockDecision.BlockWholeApp(
                reason = "App is blocked during your focus session"
            )
        }

        // Shorts/Reels-only block — from the Shorts & Reels Blocker
        // screen. Only blocks the specific short-form sub-screen
        // (Reels tab, Shorts player, Spotlight, etc.) via the bundled
        // per-app rule; the rest of the app remains usable. If the app
        // has no bundled sub-screen rule at all (e.g. TikTok, which is
        // short-form-only end to end), block the whole app since there
        // is no non-Shorts mode to preserve.
        if (shortsBlockedPackages.contains(packageName)) {
            if (appRule != null && appRule.subScreenBlocks.isNotEmpty()) {
                appRule.subScreenBlocks.forEach { rule ->
                    if (detectSubScreen(rootNode, event, rule)) {
                        return BlockDecision.BlockSubScreen(
                            subScreenName = rule.name,
                            reason = "The ${rule.name} tab is blocked during your focus session"
                        )
                    }
                }
                return BlockDecision.Allow
            }
            // No sub-screen rule bundled for this app (e.g. TikTok) —
            // there's no way to allow "the rest of the app" because
            // short-form video IS the app, so block it outright.
            return BlockDecision.BlockWholeApp(
                reason = "Short-form video is blocked during your focus session"
            )
        }

        return BlockDecision.Allow
    }

    private fun detectSubScreen(
        rootNode: AccessibilityNodeInfo,
        event: AccessibilityEvent,
        rule: AppDetectionRules.SubScreenRule
    ): Boolean {
        // Technique A: Window/class-name heuristics
        val className = event.className?.toString() ?: ""
        if (rule.classNameContains.any { className.contains(it, ignoreCase = true) }) {
            return true
        }

        // Technique B: View-tree inspection. NOTE:
        // findAccessibilityNodeInfosByViewId() requires an EXACT
        // fully-qualified id ("com.pkg:id/name"), not a substring —
        // passing a bare fragment like "clips_viewer" will never
        // match anything and always silently returns empty. Since app
        // resource ids are obfuscated/renamed across releases and we
        // can't know the exact current id, we instead walk the tree
        // ourselves and substring-match against each node's actual
        // view-id-resource-name, which does support partial matches.
        for (pattern in rule.resourceIdContains) {
            if (findNodeByResourceIdSubstring(rootNode, pattern)) {
                return true
            }
        }

        // Technique C: Content description
        for (pattern in rule.contentDescContains) {
            val nodes = findNodeByContentDescription(rootNode, pattern)
            if (nodes.isNotEmpty()) {
                nodes.forEach { it.recycle() }
                return true
            }
        }

        return false
    }

    /**
     * Walks the accessibility tree looking for any node whose resource
     * id (the part after "package:id/") contains [pattern]. Bounded to
     * a reasonable depth/node count so a deeply nested tree can't stall
     * the main thread during onAccessibilityEvent.
     */
    private fun findNodeByResourceIdSubstring(
        node: AccessibilityNodeInfo,
        pattern: String,
        depth: Int = 0,
        visited: IntArray = intArrayOf(0)
    ): Boolean {
        if (depth > 40 || visited[0] > 800) return false
        visited[0]++

        val viewId = node.viewIdResourceName
        if (viewId != null && viewId.contains(pattern, ignoreCase = true)) {
            return true
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findNodeByResourceIdSubstring(child, pattern, depth + 1, visited)
            if (found) return true
        }
        return false
    }

    private fun findNodeByContentDescription(
        node: AccessibilityNodeInfo,
        pattern: String
    ): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        val desc = node.contentDescription?.toString() ?: ""
        if (desc.contains(pattern, ignoreCase = true)) {
            results.add(AccessibilityNodeInfo.obtain(node))
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            results.addAll(findNodeByContentDescription(child, pattern))
        }
        return results
    }

    private fun evaluateYouTubeStudyMode(
        rootNode: AccessibilityNodeInfo,
        event: AccessibilityEvent
    ): BlockDecision {
        // Shorts are always blocked outright in Study Mode, regardless
        // of channel — checked first since a Shorts player can appear
        // without a channel_name node at all.
        val shortPlayerNodes = rootNode.findAccessibilityNodeInfosByViewId(
            "com.google.android.youtube:id/reel_player"
        )
        if (shortPlayerNodes.isNotEmpty()) {
            shortPlayerNodes.forEach { it.recycle() }
            return BlockDecision.BlockSubScreen(
                subScreenName = "shorts",
                reason = "YouTube Shorts are blocked in Study Mode"
            )
        }

        // Are we actually inside a video watch screen? Only the watch
        // screen carries a channel_name node in the standard YouTube
        // layout — home feed, search results, and the subscriptions
        // grid legitimately do not, and must NOT be treated as "no
        // channel detected, therefore block everything" (that was the
        // previous bug: it made the whole app unusable in Study Mode,
        // since even navigating TO a whitelisted channel got blocked
        // before the channel name was resolvable).
        val watchScreenMarkers = rootNode.findAccessibilityNodeInfosByViewId(
            "com.google.android.youtube:id/player_view"
        )
        val onWatchScreen = watchScreenMarkers.isNotEmpty()
        watchScreenMarkers.forEach { it.recycle() }

        if (!onWatchScreen) {
            // Home feed, search, subscriptions, channel pages, etc.
            // Recommendations on the home feed are visual noise but
            // not separately actionable here without false-positiving
            // on legitimate navigation — the actual distraction surface
            // (Shorts + non-whitelisted video playback) is enforced at
            // the watch screen, below.
            return BlockDecision.Allow
        }

        val channelNodes = rootNode.findAccessibilityNodeInfosByViewId(
            "com.google.android.youtube:id/channel_name"
        )

        if (channelNodes.isNotEmpty()) {
            val channelName = channelNodes.first().text?.toString() ?: ""
            channelNodes.forEach { it.recycle() }

            if (channelName.isNotBlank()) {
                val isWhitelisted = youtubeWhitelist.any {
                    it.equals(channelName, ignoreCase = true) ||
                            channelName.contains(it, ignoreCase = true) ||
                            it.contains(channelName, ignoreCase = true)
                }
                return if (isWhitelisted) {
                    BlockDecision.Allow
                } else {
                    BlockDecision.BlockSubScreen(
                        subScreenName = "video",
                        reason = "\"$channelName\" isn't on your Study Mode whitelist"
                    )
                }
            }
        }

        // On the watch screen but the channel name hasn't rendered yet
        // (e.g. page still loading) — allow this single frame rather
        // than bouncing the user; the next content-changed event will
        // re-evaluate once the channel name is available.
        return BlockDecision.Allow
    }
}
