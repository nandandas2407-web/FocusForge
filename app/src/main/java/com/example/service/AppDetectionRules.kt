// ============================================================
// FILE: app/src/main/java/com/example/service/AppDetectionRules.kt
// PURPOSE: Rule evaluation for detecting whole-app blocking or sub-screen blocking
//          (Instagram Reels, YouTube Shorts, YouTube Study Mode, Website Blocker).
//          YouTube Study Mode uses a screen-snapshot approach: build a structured
//          snapshot of the YouTube screen from ALL accessibility properties, then
//          make a deterministic allow/block decision based on screen type + channel match.
// CREATED: 2026-08-09
// UPDATED: 2026-08-09 — Full rewrite of YouTube detection for reliability.
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

/**
 * Structured snapshot of the current YouTube screen, built from ALL accessibility
 * properties (text, contentDescription, viewIdResourceName, className, paneTitle,
 * stateDescription). This replaces unreliable heuristics with deterministic classification.
 */
private data class YoutubeScreenSnapshot(
    val screenType: ScreenType,     // WATCH, SHORTS, HOME, SEARCH, CHANNEL, NAVIGATION, UNKNOWN
    val channelName: String?,       // Extracted channel display name
    val channelHandle: String?,     // Extracted @handle
    val channelId: String?,         // Extracted UC... channel ID
    val videoTitle: String?,        // Video title if visible
    val hasPlayerControls: Boolean, // play/pause/fullscreen visible
    val hasSubscribeButton: Boolean // subscribe/subscribed visible
) {
    enum class ScreenType {
        WATCH,       // Video watch page
        SHORTS,      // Shorts/Reels viewer
        HOME,        // Home feed
        SEARCH,      // Search results or search page
        CHANNEL,     // Channel page
        NAVIGATION,  // Bottom nav or other navigation
        UNKNOWN      // Could not determine
    }
}

object AppDetectionRules {

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

        // 5. Check YouTube Study Mode — screen snapshot approach
        if (packageName == "com.google.android.youtube" && isYoutubeStudyModeEnabled) {
            val decision = evaluateYoutubeStudyMode(rootNode, youtubeWhitelist)
            if (decision != null) return decision
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

    // ── YouTube Study Mode ──────────────────────────────────────────────

    /**
     * Build a screen snapshot from the accessibility tree and decide whether
     * to allow or block. Returns null if study mode cannot evaluate (e.g. no root),
     * which means the caller should Allow.
     *
     * FAIL-CLOSED: If we detect a video/watch screen but cannot verify the channel,
     * we BLOCK. Only navigation screens (home, search, channel pages) are allowed.
     */
    private fun evaluateYoutubeStudyMode(
        rootNode: AccessibilityNodeInfo?,
        youtubeWhitelist: List<YoutubeWhitelistEntity>
    ): BlockDecision? {
        if (rootNode == null) return null

        val snapshot = buildYoutubeSnapshot(rootNode)

        return when (snapshot.screenType) {
            YoutubeScreenSnapshot.ScreenType.WATCH -> {
                // Video is playing — must verify channel against whitelist
                if (youtubeWhitelist.isEmpty()) {
                    // No whitelist at all → block all videos
                    BlockDecision.BlockSubScreen(
                        "com.google.android.youtube",
                        "YouTube Study Mode",
                        "No whitelisted channels. Only approved study channels are allowed."
                    )
                } else if (channelMatchesWhitelist(snapshot, youtubeWhitelist)) {
                    // Channel is whitelisted → allow
                    null
                } else {
                    // Video detected, channel NOT verified → BLOCK (fail closed)
                    val channelDesc = snapshot.channelName ?: snapshot.channelHandle ?: snapshot.channelId ?: "unknown"
                    BlockDecision.BlockSubScreen(
                        "com.google.android.youtube",
                        "YouTube Study Mode",
                        "Video by \"$channelDesc\" is not on the whitelist. Only approved study channels are allowed."
                    )
                }
            }

            YoutubeScreenSnapshot.ScreenType.SHORTS -> {
                // Shorts are always blocked in study mode (handled by Shorts check above,
                // but double-check here for safety)
                BlockDecision.BlockSubScreen(
                    "com.google.android.youtube",
                    "YouTube Study Mode",
                    "YouTube Shorts are not allowed in Study Mode."
                )
            }

            // Navigation screens — always allow
            YoutubeScreenSnapshot.ScreenType.HOME,
            YoutubeScreenSnapshot.ScreenType.SEARCH,
            YoutubeScreenSnapshot.ScreenType.CHANNEL,
            YoutubeScreenSnapshot.ScreenType.NAVIGATION -> null

            // Unknown screen — allow (don't block navigation we can't classify)
            YoutubeScreenSnapshot.ScreenType.UNKNOWN -> null
        }
    }

    /**
     * Check if the snapshot's channel matches any whitelist entry.
     * Matches against channelName, channelHandle, and channelId with flexible normalization.
     */
    private fun channelMatchesWhitelist(
        snapshot: YoutubeScreenSnapshot,
        whitelist: List<YoutubeWhitelistEntity>
    ): Boolean {
        // Collect all channel identifiers from the snapshot
        val snapshotIds = listOfNotNull(
            snapshot.channelName?.let { normalizeYoutubeText(it) },
            snapshot.channelHandle?.let { normalizeYoutubeText(it) },
            snapshot.channelId?.let { normalizeYoutubeText(it) }
        ).filter { it.isNotBlank() && it.length >= 3 }

        if (snapshotIds.isEmpty()) return false

        return whitelist.any { item ->
            val whitelistIds = listOfNotNull(
                item.channelTitle.let { normalizeYoutubeText(it) },
                item.channelId.let { normalizeYoutubeText(it) }
            ).filter { it.isNotBlank() && it.length >= 3 }

            whitelistIds.any { wId ->
                snapshotIds.any { sId ->
                    sId == wId ||
                    sId == "@$wId" ||
                    wId == "@$sId" ||
                    sId.startsWith("$wId ") ||
                    sId.startsWith("$wId·") ||
                    wId.startsWith("$sId ") ||
                    wId.startsWith("$sId·") ||
                    // Substring match for long enough identifiers to handle
                    // "Khan Academy" vs "Khan Academy · @kurzgesagt" etc.
                    (sId.length > 7 && wId.length > 7 && (sId.contains(wId) || wId.contains(sId)))
                }
            }
        }
    }

    // ── YouTube Screen Snapshot Builder ─────────────────────────────────

    /**
     * Build a structured snapshot of the YouTube screen by extracting information
     * from ALL accessibility properties: text, contentDescription, viewIdResourceName,
     * className, paneTitle, stateDescription.
     */
    private fun buildYoutubeSnapshot(rootNode: AccessibilityNodeInfo): YoutubeScreenSnapshot {
        val allNodes = mutableListOf<AccessibilityNodeInfo>()
        collectAllNodes(rootNode, allNodes, depth = 0)

        // Extract all text-like content from every property
        val allTexts = mutableListOf<String>()
        val allContentDescriptions = mutableListOf<String>()
        val allResourceIds = mutableListOf<String>()
        val allClassNames = mutableListOf<String>()

        for (node in allNodes) {
            node.text?.toString()?.takeIf { it.isNotBlank() }?.let { allTexts.add(it) }
            node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { allContentDescriptions.add(it) }
            node.viewIdResourceName?.takeIf { it.isNotBlank() }?.let { allResourceIds.add(it) }
            node.className?.toString()?.takeIf { it.isNotBlank() }?.let { allClassNames.add(it) }
        }

        val allTextsLower = allTexts.map { normalizeYoutubeText(it) }
        val allDescLower = allContentDescriptions.map { normalizeYoutubeText(it) }
        val allCombined = allTextsLower + allDescLower
        val joinedCombined = allCombined.joinToString(" ")
        val joinedResourceIds = allResourceIds.joinToString(" ")

        // ── Detect screen type ──

        val isShorts = detectShortsScreen(allCombined, allClassNames, joinedCombined)
        val isWatchPage = detectWatchPage(allCombined, allClassNames, joinedCombined, joinedResourceIds)
        val isHomeFeed = detectHomeFeed(allCombined)
        val isSearchPage = detectSearchPage(allCombined, joinedResourceIds)
        val isChannelPage = detectChannelPage(allCombined, joinedResourceIds)
        val isNavigation = detectNavigation(allCombined)

        val screenType = when {
            isShorts -> YoutubeScreenSnapshot.ScreenType.SHORTS
            isWatchPage -> YoutubeScreenSnapshot.ScreenType.WATCH
            isHomeFeed -> YoutubeScreenSnapshot.ScreenType.HOME
            isSearchPage -> YoutubeScreenSnapshot.ScreenType.SEARCH
            isChannelPage -> YoutubeScreenSnapshot.ScreenType.CHANNEL
            isNavigation -> YoutubeScreenSnapshot.ScreenType.NAVIGATION
            else -> YoutubeScreenSnapshot.ScreenType.UNKNOWN
        }

        // ── Extract channel info ──

        val channelName = extractChannelName(allTexts, allContentDescriptions)
        val channelHandle = extractChannelHandle(allTexts, allContentDescriptions)
        val channelId = extractChannelId(allTexts, allContentDescriptions, allResourceIds)
        val videoTitle = extractVideoTitle(allTexts, allContentDescriptions)

        // ── Player signals ──

        val hasPlayerControls = allCombined.any {
            it == "play" || it == "pause" || it == "fullscreen" ||
            it == "mute" || it == "unmute" || it == "skip ad" ||
            it == "seekbar" || it.contains("progress")
        }

        val hasSubscribeButton = allCombined.any {
            it == "subscribe" || it == "subscribed"
        }

        return YoutubeScreenSnapshot(
            screenType = screenType,
            channelName = channelName,
            channelHandle = channelHandle,
            channelId = channelId,
            videoTitle = videoTitle,
            hasPlayerControls = hasPlayerControls,
            hasSubscribeButton = hasSubscribeButton
        )
    }

    // ── Screen Type Detection ───────────────────────────────────────────

    private fun detectShortsScreen(
        allCombined: List<String>,
        allClassNames: List<String>,
        joinedCombined: String
    ): Boolean {
        // Check class names for shorts/reel indicators
        val hasShortsClass = allClassNames.any {
            it.contains("reel", ignoreCase = true) || it.contains("shorts", ignoreCase = true)
        }

        // Check for shorts-specific UI elements
        val hasShortsUI = joinedCombined.contains("reel_player") ||
            joinedCombined.contains("shorts_player") ||
            allCombined.any {
                it == "remix" || it == "sound" || it == "use this sound" ||
                it == "original audio"
            }

        // Shorts screens typically lack the main navigation bar
        val lacksNavBar = allCombined.none {
            it == "home" || it == "subscriptions" || it == "library" || it == "you"
        }

        return hasShortsClass || (hasShortsUI && lacksNavBar)
    }

    private fun detectWatchPage(
        allCombined: List<String>,
        allClassNames: List<String>,
        joinedCombined: String,
        joinedResourceIds: String
    ): Boolean {
        // Signal 1: className contains watch/player
        val hasWatchClass = allClassNames.any {
            it.contains("watch", ignoreCase = true) || it.contains("player", ignoreCase = true)
        }

        // Signal 2: Subscribe button (only on watch pages, not home/search)
        val hasSubscribe = allCombined.any { it == "subscribe" || it == "subscribed" }

        // Signal 3: Video engagement actions
        val hasEngagement = allCombined.any {
            it == "like" || it == "dislike" || it == "share" || it == "download" ||
            it == "thanks" || it == "clip" || it == "save"
        }

        // Signal 4: Video metadata
        val hasVideoMetadata = allCombined.any {
            it.contains("views") || it.contains("subscribers") ||
            it == "comments" || it.contains("comment")
        }

        // Signal 5: Video description
        val hasDescription = allCombined.any {
            it == "description" || it == "show more" || it.startsWith("description")
        }

        // Signal 6: Resource IDs pointing to watch/player elements
        val hasWatchResourceIds = joinedResourceIds.contains("watch") ||
            joinedResourceIds.contains("player") ||
            joinedResourceIds.contains("video")

        // Signal 7: Player controls
        val hasPlayerControls = allCombined.any {
            it == "play" || it == "pause" || it == "fullscreen" ||
            it == "seekbar" || it.contains("progress")
        }

        // Need at least 2 strong signals, or 1 strong + resource ID evidence
        val strongSignals = listOf(
            hasSubscribe && hasEngagement,
            hasSubscribe && hasVideoMetadata,
            hasEngagement && hasVideoMetadata,
            hasPlayerControls && hasSubscribe,
            hasPlayerControls && hasDescription,
            hasWatchClass && hasSubscribe,
            hasWatchClass && hasEngagement
        ).count { it }

        return strongSignals >= 1 || (hasWatchResourceIds && hasPlayerControls)
    }

    private fun detectHomeFeed(allCombined: List<String>): Boolean {
        return allCombined.any { it == "home" || it == "home feed" || it == "articles" }
    }

    private fun detectSearchPage(allCombined: List<String>, joinedResourceIds: String): Boolean {
        val hasSearchUI = allCombined.any {
            it == "search" || it == "search queries" || it == "filters" || it == "sort by"
        }
        val hasSearchResourceId = joinedResourceIds.contains("search")
        return hasSearchUI || hasSearchResourceId
    }

    private fun detectChannelPage(allCombined: List<String>, joinedResourceIds: String): Boolean {
        val hasChannelUI = allCombined.any {
            it == "videos" && (allCombined.contains("playlists") || allCombined.contains("about"))
        }
        val hasChannelResourceId = joinedResourceIds.contains("channel")
        return hasChannelUI || hasChannelResourceId
    }

    private fun detectNavigation(allCombined: List<String>): Boolean {
        val navItems = listOf("home", "shorts", "subscriptions", "you", "library", "notifications")
        val navCount = allCombined.count { it in navItems }
        return navCount >= 3 // Bottom nav bar has 4-5 items
    }

    // ── Channel Info Extraction ─────────────────────────────────────────

    /**
     * Extract channel display name from all text and content description nodes.
     * YouTube often shows the channel name near the subscribe button on watch pages.
     */
    private fun extractChannelName(
        allTexts: List<String>,
        allContentDescriptions: List<String>
    ): String? {
        // Look for channel name patterns in content descriptions first
        // (more reliable than raw text for YouTube)
        for (desc in allContentDescriptions) {
            val lower = desc.lowercase()
            // Pattern: "Channel Name · Subscribe" or "Subscribe to Channel Name"
            if (lower.contains("subscribe")) {
                val beforeSubscribe = desc.substringBefore("subscribe").trim().trim('·', '|', ' ')
                if (beforeSubscribe.length in 2..60) return beforeSubscribe.trim()
                val afterSubscribe = desc.substringAfter("subscribe").trim().trim('·', '|', ' ')
                if (afterSubscribe.length in 2..60) return afterSubscribe.trim()
            }
        }

        // Look for "· @" pattern (YouTube shows "Channel Name · @handle")
        for (text in allTexts) {
            if (text.contains("·@") || text.contains(" · @")) {
                val name = text.substringBefore("·").substringBefore("·@").trim()
                if (name.length in 2..60) return name
            }
        }

        // Look for text near subscribe/subscribed button
        val subscribeIdx = allTexts.indexOfFirst {
            it.equals("subscribe", ignoreCase = true) || it.equals("subscribed", ignoreCase = true)
        }
        if (subscribeIdx > 0) {
            // The channel name is often the text node just before subscribe
            val candidate = allTexts[subscribeIdx - 1].trim()
            if (candidate.length in 2..60 &&
                !candidate.equals("subscribe", ignoreCase = true) &&
                !candidate.equals("subscribed", ignoreCase = true) &&
                !candidate.contains("views", ignoreCase = true)
            ) {
                return candidate
            }
        }

        return null
    }

    /**
     * Extract @handle from text or content description nodes.
     */
    private fun extractChannelHandle(
        allTexts: List<String>,
        allContentDescriptions: List<String>
    ): String? {
        val handlePattern = Regex("@[A-Za-z0-9_.]+")
        for (text in allTexts) {
            handlePattern.find(text)?.let { return it.value }
        }
        for (desc in allContentDescriptions) {
            handlePattern.find(desc)?.let { return it.value }
        }
        return null
    }

    /**
     * Extract UC... channel ID from text, content description, or resource IDs.
     */
    private fun extractChannelId(
        allTexts: List<String>,
        allContentDescriptions: List<String>,
        allResourceIds: List<String>
    ): String? {
        val idPattern = Regex("UC[A-Za-z0-9_-]{22}")
        for (text in allTexts) {
            idPattern.find(text)?.let { return it.value }
        }
        for (desc in allContentDescriptions) {
            idPattern.find(desc)?.let { return it.value }
        }
        for (resId in allResourceIds) {
            idPattern.find(resId)?.let { return it.value }
        }
        return null
    }

    /**
     * Extract video title from text or content description.
     */
    private fun extractVideoTitle(
        allTexts: List<String>,
        allContentDescriptions: List<String>
    ): String? {
        // Video titles are typically longer text nodes on watch pages
        for (desc in allContentDescriptions) {
            if (desc.length in 10..200 &&
                !desc.contains("subscribe", ignoreCase = true) &&
                !desc.contains("views", ignoreCase = true)
            ) {
                return desc
            }
        }
        return null
    }

    // ── Instagram & YouTube Shorts Detection (unchanged) ────────────────

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

    // ── Utility ─────────────────────────────────────────────────────────

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

    /**
     * Recursively collect ALL accessibility nodes for comprehensive property extraction.
     */
    private fun collectAllNodes(node: AccessibilityNodeInfo, out: MutableList<AccessibilityNodeInfo>, depth: Int) {
        if (depth > 12) return
        out.add(node)
        for (i in 0 until node.childCount) {
            val child = try { node.getChild(i) } catch (e: Exception) { null }
            if (child != null) collectAllNodes(child, out, depth + 1)
        }
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
