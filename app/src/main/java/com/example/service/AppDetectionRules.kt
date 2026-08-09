// ============================================================
// FILE: app/src/main/java/com/example/service/AppDetectionRules.kt
// PURPOSE: Rule evaluation for detecting whole-app blocking or sub-screen blocking
//          (Instagram Reels, YouTube Shorts, YouTube Study Mode, Website Blocker).
// CREATED: 2026-08-09
// ============================================================

package com.example.service

import android.view.accessibility.AccessibilityNodeInfo
import com.example.data.entity.WebsiteBlockEntity
import com.example.data.entity.YoutubeWhitelistEntity

sealed class BlockDecision {
    data class BlockWholeApp(val packageName: String, val reason: String) : BlockDecision()
    data class BlockSubScreen(val packageName: String, val subScreenName: String, val reason: String) : BlockDecision()
    object Allow : BlockDecision()
}

object AppDetectionRules {

    // Study Mode is intentionally whitelist-first. Generic topic keywords are not
    // sufficient to prove that a YouTube video belongs to a productive channel.
    // A video is allowed only when its channel identity is visible and matches a
    // saved whitelist entry. Navigation/search/channel pages remain available.
    private val browserPackages = setOf(
        "com.android.chrome",
        "com.sec.android.app.sbrowser",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "com.brave.browser"
    )

    fun evaluate(
        packageName: String,
        className: String?,
        rootNode: AccessibilityNodeInfo?,
        blockedPackages: Set<String>,
        reelsBlockedPackages: Set<String>,
        shortsBlockedPackages: Set<String>,
        isYoutubeStudyModeEnabled: Boolean = false,
        youtubeWhitelist: List<YoutubeWhitelistEntity> = emptyList(),
        websiteBlocks: List<WebsiteBlockEntity> = emptyList()
    ): BlockDecision {

        // 1. Check Full App Block
        if (blockedPackages.contains(packageName)) {
            return BlockDecision.BlockWholeApp(packageName, "App is locked by FocusForge")
        }

        // 2. Check Instagram Reels
        if (packageName == "com.instagram.android" && reelsBlockedPackages.contains(packageName)) {
            if (isInstagramReels(className, rootNode)) {
                return BlockDecision.BlockSubScreen(packageName, "Instagram Reels", "Reels blocking is active")
            }
        }

        // 3. Check YouTube Shorts
        if (packageName == "com.google.android.youtube" && shortsBlockedPackages.contains(packageName)) {
            if (isYoutubeShorts(className, rootNode)) {
                return BlockDecision.BlockSubScreen(packageName, "YouTube Shorts", "Shorts blocking is active")
            }
        }

        // 4. Check TikTok
        if ((packageName == "com.zhiliaoapp.musically" || packageName == "com.ss.android.ugc.trill") &&
            shortsBlockedPackages.contains(packageName)) {
            return BlockDecision.BlockWholeApp(packageName, "TikTok is blocked in Focus Mode")
        }

        // 5. Check YouTube Study Mode
        if (packageName == "com.google.android.youtube" && isYoutubeStudyModeEnabled) {
            val isWhitelisted = isYoutubeContentWhitelisted(rootNode, youtubeWhitelist)
            if (!isWhitelisted) {
                return BlockDecision.BlockSubScreen(
                    packageName,
                    "YouTube Study Mode",
                    "Unproductive YouTube video blocked. Only whitelisted study channels are allowed."
                )
            }
        }

        // 6. Check Website Blocker (for Web Browsers)
        if (browserPackages.contains(packageName) && websiteBlocks.isNotEmpty()) {
            val blockedDomain = checkWebsiteBlocks(rootNode, websiteBlocks)
            if (blockedDomain != null) {
                return BlockDecision.BlockSubScreen(
                    packageName,
                    "Website Blocker",
                    "Access to $blockedDomain is blocked in Focus Mode."
                )
            }
        }

        return BlockDecision.Allow
    }

    private fun isInstagramReels(className: String?, rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        val visibleTexts = getAllTextFromNode(rootNode)

        val hasMainFeedNav = visibleTexts.any { t ->
            val text = t.trim().lowercase()
            text == "direct" || text == "messenger" || text == "activity"
        }
        if (hasMainFeedNav) {
            return false
        }

        val isClipsClass = className?.contains("clips", ignoreCase = true) == true ||
                           className?.contains("reel", ignoreCase = true) == true

        val joinedTextLower = visibleTexts.joinToString(" ").lowercase()
        val isClipsViewer = joinedTextLower.contains("clips_viewer") ||
                            visibleTexts.any { t ->
                                val text = t.trim().lowercase()
                                text == "original audio" || text == "use audio"
                            }

        return isClipsClass || isClipsViewer
    }

    private fun isYoutubeShorts(className: String?, rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        val visibleTexts = getAllTextFromNode(rootNode)

        val hasMainNavBar = visibleTexts.any { t ->
            val text = t.trim().lowercase()
            text == "home" || text == "subscriptions" || text == "library" || text == "you"
        }
        if (hasMainNavBar) {
            return false
        }

        val isReelClass = className?.contains("Reel", ignoreCase = true) == true ||
                          className?.contains("Shorts", ignoreCase = true) == true

        val joinedTextLower = visibleTexts.joinToString(" ").lowercase()
        val hasShortsIdentifiers = joinedTextLower.contains("reel_player") ||
                                   joinedTextLower.contains("shorts_player") ||
                                   visibleTexts.any { t ->
                                       val text = t.trim().lowercase()
                                       text == "remix" || text == "sound" || text == "use this sound"
                                   }

        return isReelClass || hasShortsIdentifiers
    }

    private fun isYoutubeContentWhitelisted(
        rootNode: AccessibilityNodeInfo?,
        youtubeWhitelist: List<YoutubeWhitelistEntity>
    ): Boolean {
        if (rootNode == null) return false

        val visibleTexts = getAllTextFromNode(rootNode)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (visibleTexts.isEmpty()) return false

        // Do not block ordinary YouTube navigation. Study Mode enforcement starts
        // only when the accessibility tree looks like a watch/video screen.
        if (!isYoutubeWatchPage(rootNode, visibleTexts)) return true

        if (youtubeWhitelist.isEmpty()) return false

        val normalizedVisible = visibleTexts.map(::normalizeYoutubeText)

        // IMPORTANT: channel matching is exact-ish, not substring keyword matching.
        // This prevents a random video whose title/description contains words such
        // as "math" or "physics" from bypassing Study Mode.
        return youtubeWhitelist.any { item ->
            val candidates = listOf(item.channelTitle, item.channelId)
                .map(::normalizeYoutubeText)
                .filter { it.isNotBlank() }

            candidates.any { candidate ->
                normalizedVisible.any { visible ->
                    visible == candidate ||
                    visible == "@$candidate" ||
                    visible.startsWith("$candidate ·") ||
                    visible.startsWith("$candidate ")
                }
            }
        }
    }

    private fun isYoutubeWatchPage(
        rootNode: AccessibilityNodeInfo,
        visibleTexts: List<String>
    ): Boolean {
        val normalized = visibleTexts.map(::normalizeYoutubeText)

        val hasWatchClass = rootNode.className?.toString()?.let { className ->
            className.contains("watch", ignoreCase = true) ||
            className.contains("player", ignoreCase = true)
        } == true

        val hasVideoControls = normalized.any {
            it == "play" || it == "pause" || it == "fullscreen" ||
            it == "mute" || it == "unmute" || it == "skip ad" ||
            it == "subscribe" || it == "subscribed"
        }

        val hasVideoMetadata = normalized.any {
            it.contains("views") ||
            it.contains("subscribers") ||
            it == "comments" ||
            it == "share" ||
            it == "download"
        }

        return hasWatchClass || (hasVideoControls && hasVideoMetadata) ||
            (hasVideoControls && normalized.size > 5)
    }

    private fun normalizeYoutubeText(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim(' ', '·', '|', '•')
    }

    private fun checkWebsiteBlocks(
        rootNode: AccessibilityNodeInfo?,
        websiteBlocks: List<WebsiteBlockEntity>
    ): String? {
        if (rootNode == null || websiteBlocks.isEmpty()) return null

        val visibleTexts = getAllTextFromNode(rootNode)
        val joinedTextLower = visibleTexts.joinToString(" ").lowercase()

        for (item in websiteBlocks) {
            val domainLower = item.domain.lowercase().removePrefix("https://").removePrefix("http://").removePrefix("www.")
            if (domainLower.isNotBlank() && joinedTextLower.contains(domainLower)) {
                return item.domain
            }
        }
        return null
    }

    private fun getAllTextFromNode(node: AccessibilityNodeInfo?, depth: Int = 0): List<String> {
        if (node == null || depth > 8) return emptyList()
        val textList = mutableListOf<String>()

        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { textList.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { textList.add(it) }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = try { node.getChild(i) } catch (e: Exception) { null }
            if (child != null) {
                textList.addAll(getAllTextFromNode(child, depth + 1))
            }
        }
        return textList
    }
}

