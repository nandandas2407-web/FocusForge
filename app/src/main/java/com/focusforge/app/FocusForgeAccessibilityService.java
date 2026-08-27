package com.focusforge.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * FocusForge Accessibility Service
 * Intercepts foreground app events and blocks content at the system level.
 *
 * Blocking logic:
 * 1. Full app block → GLOBAL_ACTION_HOME (bounces user to home screen)
 * 2. Instagram Reels → GLOBAL_ACTION_BACK + overlay notification
 * 3. YouTube Shorts → GLOBAL_ACTION_BACK + overlay notification
 * 4. YouTube Study Mode → Screen snapshot → channel match → BACK/HOME
 * 5. Website Blocker → GLOBAL_ACTION_BACK + overlay notification
 */
public class FocusForgeAccessibilityService extends AccessibilityService {

    private static final String TAG = "FocusForge";
    private static FocusForgeAccessibilityService instance;

    // Browser packages
    private static final Set<String> BROWSER_PACKAGES = new HashSet<>(Arrays.asList(
        "com.android.chrome",
        "com.sec.android.app.sbrowser",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "com.brave.browser"
    ));

    public static FocusForgeAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "FocusForge Accessibility Service connected");

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags = info.flags | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                         AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            setServiceInfo(info);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!FocusForgeConfig.globalBlockerEnabled) return;
        if (event == null) return;

        int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        String packageName = pkgCs.toString();

        // Don't block ourselves
        if (packageName.equals(getPackageName())) return;

        CharSequence classCs = event.getClassName();
        String className = classCs != null ? classCs.toString() : null;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        // 1. Full app block
        if (FocusForgeConfig.blockedPackages.contains(packageName)) {
            Log.d(TAG, "BLOCKED app: " + packageName);
            performGlobalAction(GLOBAL_ACTION_HOME);
            showOverlay(packageName, "App blocked by FocusForge");
            return;
        }

        // 2. Instagram Reels
        if ("com.instagram.android".equals(packageName) && FocusForgeConfig.reelsShortsBlockingEnabled && FocusForgeConfig.reelsBlockedPackages.contains(packageName)) {
            if (detectInstagramReels(rootNode)) {
                Log.d(TAG, "BLOCKED Instagram Reels");
                performGlobalAction(GLOBAL_ACTION_BACK);
                showOverlay(packageName, "Instagram Reels blocked");
                return;
            }
        }

        // 3. YouTube Shorts
        if ("com.google.android.youtube".equals(packageName) && FocusForgeConfig.reelsShortsBlockingEnabled && FocusForgeConfig.shortsBlockedPackages.contains(packageName)) {
            if (detectYoutubeShorts(rootNode)) {
                Log.d(TAG, "BLOCKED YouTube Shorts");
                performGlobalAction(GLOBAL_ACTION_BACK);
                showOverlay(packageName, "YouTube Shorts blocked");
                return;
            }
        }

        // 4. YouTube Study Mode
        if ("com.google.android.youtube".equals(packageName) && FocusForgeConfig.youtubeStudyModeEnabled) {
            BlockDecision decision = evaluateYoutubeStudyMode(rootNode);
            if (decision != null) {
                Log.d(TAG, "YouTube Study Mode: " + decision.reason);
                if (decision.isWholeApp) {
                    performGlobalAction(GLOBAL_ACTION_HOME);
                } else {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                }
                showOverlay(packageName, decision.reason);
                return;
            }
        }

        // 5. Website blocker
        if (BROWSER_PACKAGES.contains(packageName) && !FocusForgeConfig.blockedDomains.isEmpty()) {
            String blockedDomain = checkWebsiteBlocks(rootNode);
            if (blockedDomain != null) {
                Log.d(TAG, "BLOCKED website: " + blockedDomain);
                performGlobalAction(GLOBAL_ACTION_BACK);
                showOverlay(packageName, blockedDomain + " blocked by FocusForge");
                return;
            }
        }
    }

    // ── YouTube Shorts Detection ────────────────────────────────────

    private boolean detectYoutubeShorts(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return false;

        String joined = getAllTextJoined(rootNode);

        // Check for shorts-specific UI indicators
        boolean hasShortsPlayer = joined.contains("reel_player") ||
                                   joined.contains("shorts_player") ||
                                   joined.contains("ytd-reel-player");
        boolean hasShortsActions = joined.contains("remix") ||
                                    joined.contains("use this sound") ||
                                    joined.contains("original audio");
        boolean lacksNavBar = !joined.contains("home") ||
                               !joined.contains("subscriptions") ||
                               !joined.contains("library");

        // Check resource IDs for shorts containers
        String resourceIds = getAllResourceIds(rootNode);
        boolean hasShortsResourceId = resourceIds.contains("reel_player") ||
                                       resourceIds.contains("shorts_player") ||
                                       resourceIds.contains("ytd-reel");

        return hasShortsPlayer || hasShortsResourceId || (hasShortsActions && lacksNavBar);
    }

    // ── Instagram Reels Detection ───────────────────────────────────

    private boolean detectInstagramReels(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return false;

        String joined = getAllTextJoined(rootNode);

        // Check if we're on main feed (has navigation indicators)
        boolean hasMainNav = joined.contains("direct") || joined.contains("messenger") || joined.contains("activity");
        if (hasMainNav) return false;

        // Check for reels-specific content
        boolean hasReelsContent = joined.contains("clips_viewer") ||
                                   joined.contains("original audio") ||
                                   joined.contains("use audio");

        String resourceIds = getAllResourceIds(rootNode);
        boolean hasReelsResourceId = resourceIds.contains("reel") || resourceIds.contains("clips");

        return hasReelsContent || hasReelsResourceId;
    }

    // ── YouTube Study Mode ──────────────────────────────────────────

    private static class BlockDecision {
        boolean isWholeApp;
        String reason;
        BlockDecision(boolean isWholeApp, String reason) {
            this.isWholeApp = isWholeApp;
            this.reason = reason;
        }
    }

    private BlockDecision evaluateYoutubeStudyMode(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return null;

        // Detect screen type
        String joined = getAllTextJoined(rootNode).toLowerCase();
        String resourceIds = getAllResourceIds(rootNode).toLowerCase();

        // Shorts → always block in study mode
        if (joined.contains("reel_player") || joined.contains("shorts_player") ||
            resourceIds.contains("reel") || resourceIds.contains("shorts")) {
            return new BlockDecision(false, "YouTube Shorts not allowed in Study Mode");
        }

        // Watch page detection: has subscribe button + engagement actions
        boolean hasSubscribe = joined.contains("subscribe");
        boolean hasEngagement = joined.contains("like") || joined.contains("share") || joined.contains("download");
        boolean hasVideoMetadata = joined.contains("views") || joined.contains("subscribers") || joined.contains("comments");
        boolean hasPlayerControls = joined.contains("play") || joined.contains("pause") || joined.contains("fullscreen") || joined.contains("seekbar");

        boolean isWatchPage = (hasSubscribe && hasEngagement) ||
                               (hasSubscribe && hasVideoMetadata) ||
                               (hasPlayerControls && hasSubscribe) ||
                               (resourceIds.contains("watch") && hasPlayerControls);

        if (isWatchPage) {
            // Extract channel info and check whitelist
            String channelName = extractChannelName(rootNode);
            String channelHandle = extractChannelHandle(joined);

            if (channelName == null && channelHandle == null) {
                // Cannot identify channel → BLOCK (fail closed)
                return new BlockDecision(false, "Cannot verify channel — blocked by Study Mode");
            }

            boolean isWhitelisted = false;
            if (channelName != null) {
                for (String whitelist : FocusForgeConfig.youtubeWhitelistChannels) {
                    if (normalize(channelName).contains(normalize(whitelist)) ||
                        normalize(whitelist).contains(normalize(channelName))) {
                        isWhitelisted = true;
                        break;
                    }
                }
            }
            if (!isWhitelisted && channelHandle != null) {
                for (String handle : FocusForgeConfig.youtubeWhitelistHandles) {
                    if (normalize(channelHandle).equals(normalize(handle))) {
                        isWhitelisted = true;
                        break;
                    }
                }
            }

            if (isWhitelisted) return null; // Allow

            String who = channelName != null ? channelName : channelHandle;
            return new BlockDecision(false, "\"" + who + "\" not on whitelist — only approved channels allowed");
        }

        // Home, Search, Channel pages → allow
        return null;
    }

    private String extractChannelName(AccessibilityNodeInfo rootNode) {
        // Look for content descriptions with "subscribe" pattern
        String allDesc = getAllContentDescriptions(rootNode);
        String[] parts = allDesc.split("\\|");
        for (String part : parts) {
            String lower = part.toLowerCase().trim();
            if (lower.contains("subscribe")) {
                // "Channel Name · Subscribe" or "Subscribe to Channel Name"
                String before = part.substring(0, part.toLowerCase().indexOf("subscribe")).trim();
                before = before.replaceAll("[·|•]", "").trim();
                if (before.length() >= 2 && before.length() <= 60) return before;

                String after = part.substring(part.toLowerCase().indexOf("subscribe") + 9).trim();
                after = after.replaceAll("[·|•]", "").trim();
                if (after.length() >= 2 && after.length() <= 60) return after;
            }
        }

        // Look for "· @" pattern
        String allText = getAllTextJoined(rootNode);
        if (allText.contains("·@") || allText.contains(" · @")) {
            String name = allText.split("·@")[0].split(" · @")[0].trim();
            if (name.length() >= 2 && name.length() <= 60) return name;
        }

        return null;
    }

    private String extractChannelHandle(String joined) {
        String[] parts = joined.split("\\s+");
        for (String part : parts) {
            if (part.startsWith("@") && part.length() >= 3) {
                return part;
            }
        }
        return null;
    }

    // ── Website Blocker ─────────────────────────────────────────────

    private String checkWebsiteBlocks(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return null;

        String joined = getAllTextJoined(rootNode).toLowerCase();
        for (String domain : FocusForgeConfig.blockedDomains) {
            String cleanDomain = domain.toLowerCase()
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "");
            if (cleanDomain.length() > 2 && joined.contains(cleanDomain)) {
                return domain;
            }
        }
        return null;
    }

    // ── Utility Methods ─────────────────────────────────────────────

    private String getAllTextJoined(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectText(node, sb, 0);
        return sb.toString().toLowerCase();
    }

    private void collectText(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 8) return;

        CharSequence text = node.getText();
        if (text != null && text.length() > 0) {
            sb.append(" ").append(text.toString().toLowerCase());
        }

        CharSequence desc = node.getContentDescription();
        if (desc != null && desc.length() > 0) {
            sb.append(" ").append(desc.toString().toLowerCase());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectText(child, sb, depth + 1);
                child.recycle();
            }
        }
    }

    private String getAllResourceIds(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectResourceIds(node, sb, 0);
        return sb.toString().toLowerCase();
    }

    private void collectResourceIds(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 8) return;

        CharSequence viewId = node.getViewIdResourceName();
        if (viewId != null) {
            sb.append(" ").append(viewId.toString().toLowerCase());
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectResourceIds(child, sb, depth + 1);
                child.recycle();
            }
        }
    }

    private String getAllContentDescriptions(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectDescriptions(node, sb, 0);
        return sb.toString();
    }

    private void collectDescriptions(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 8) return;

        CharSequence desc = node.getContentDescription();
        if (desc != null && desc.length() > 0) {
            sb.append(desc.toString()).append("|");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectDescriptions(child, sb, depth + 1);
                child.recycle();
            }
        }
    }

    private String normalize(String s) {
        return s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private void showOverlay(String packageName, String reason) {
        Intent intent = new Intent(this, BlockOverlayService.class);
        intent.putExtra("PACKAGE_NAME", packageName);
        intent.putExtra("REASON", reason);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "FocusForge Accessibility Service interrupted");
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public static boolean isServiceEnabled() {
        return instance != null;
    }
}
