package com.focusforge.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FocusForgeAccessibilityService extends AccessibilityService {
    private static final String TAG = "FocusForge";
    private static volatile FocusForgeAccessibilityService instance;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long lastEventTime = 0;
    private static final long EVENT_THROTTLE_MS = 150;

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

    public static boolean isServiceEnabled() {
        return instance != null;
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d(TAG, "Accessibility Service connected");

        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                           AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
            setServiceInfo(info);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!FocusForgeConfig.globalBlockerEnabled) return;
        if (event == null) return;

        long now = System.currentTimeMillis();
        if (now - lastEventTime < EVENT_THROTTLE_MS) return;
        lastEventTime = now;

        int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_VIEW_CLICKED) {
            return;
        }

        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        String packageName = pkgCs.toString();
        if (packageName.equals(getPackageName())) return;

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        if (FocusForgeConfig.blockedPackages.contains(packageName)) {
            Log.d(TAG, "BLOCKED app: " + packageName);
            safePerformHome();
            showOverlay(packageName, "App blocked by FocusForge");
            return;
        }

        if (FocusForgeConfig.reelsShortsBlockingEnabled &&
            "com.instagram.android".equals(packageName) &&
            FocusForgeConfig.reelsBlockedPackages.contains(packageName)) {
            if (detectInstagramReels(rootNode)) {
                Log.d(TAG, "BLOCKED Instagram Reels");
                safePerformBack();
                showOverlay(packageName, "Instagram Reels blocked");
                return;
            }
        }

        if (FocusForgeConfig.reelsShortsBlockingEnabled &&
            "com.google.android.youtube".equals(packageName) &&
            FocusForgeConfig.shortsBlockedPackages.contains(packageName)) {
            if (detectYoutubeShorts(rootNode)) {
                Log.d(TAG, "BLOCKED YouTube Shorts");
                safePerformBack();
                showOverlay(packageName, "YouTube Shorts blocked");
                return;
            }
        }

        if ("com.google.android.youtube".equals(packageName) &&
            FocusForgeConfig.youtubeStudyModeEnabled) {
            BlockDecision decision = evaluateYoutubeStudyMode(rootNode);
            if (decision != null) {
                Log.d(TAG, "YouTube Study Mode: " + decision.reason);
                if (decision.isWholeApp) {
                    safePerformHome();
                } else {
                    safePerformBack();
                }
                showOverlay(packageName, decision.reason);
                return;
            }
        }

        if (BROWSER_PACKAGES.contains(packageName) && !FocusForgeConfig.blockedDomains.isEmpty()) {
            String blockedDomain = checkWebsiteBlocks(rootNode);
            if (blockedDomain != null) {
                Log.d(TAG, "BLOCKED website: " + blockedDomain);
                safePerformBack();
                showOverlay(packageName, blockedDomain + " blocked");
                return;
            }
        }
    }

    private void safePerformHome() {
        mainHandler.post(() -> {
            try { performGlobalAction(GLOBAL_ACTION_HOME); }
            catch (Exception e) { Log.e(TAG, "GLOBAL_ACTION_HOME failed", e); }
        });
    }

    private void safePerformBack() {
        mainHandler.post(() -> {
            try { performGlobalAction(GLOBAL_ACTION_BACK); }
            catch (Exception e) { Log.e(TAG, "GLOBAL_ACTION_BACK failed", e); }
        });
    }

    private boolean detectYoutubeShorts(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return false;
        try {
            String joined = getAllTextJoined(rootNode);
            String resourceIds = getAllResourceIds(rootNode);
            boolean hasShortsPlayer = joined.contains("reel_player") ||
                                       joined.contains("shorts_player") ||
                                       joined.contains("ytd-reel-player");
            boolean hasShortsResourceId = resourceIds.contains("reel_player") ||
                                           resourceIds.contains("shorts_player") ||
                                           resourceIds.contains("ytd-reel");
            boolean hasShortsActions = joined.contains("remix") ||
                                        joined.contains("use this sound") ||
                                        joined.contains("original audio");
            boolean lacksNavBar = !joined.contains("home") ||
                                   !joined.contains("subscriptions");
            return hasShortsPlayer || hasShortsResourceId || (hasShortsActions && lacksNavBar);
        } catch (Exception e) {
            Log.e(TAG, "detectYoutubeShorts error", e);
            return false;
        }
    }

    private boolean detectInstagramReels(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return false;
        try {
            String joined = getAllTextJoined(rootNode);
            boolean hasMainNav = joined.contains("direct") || joined.contains("messenger") || joined.contains("activity");
            if (hasMainNav) return false;
            boolean hasReelsContent = joined.contains("clips_viewer") ||
                                       joined.contains("original audio") ||
                                       joined.contains("use audio");
            boolean lacksMainFeed = !joined.contains("posts") || !joined.contains("suggestions");
            return hasReelsContent && lacksMainFeed;
        } catch (Exception e) {
            Log.e(TAG, "detectInstagramReels error", e);
            return false;
        }
    }

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
        try {
            String joined = getAllTextJoined(rootNode).toLowerCase();
            String resourceIds = getAllResourceIds(rootNode).toLowerCase();

            if (joined.contains("reel_player") || joined.contains("shorts_player") ||
                resourceIds.contains("reel") || resourceIds.contains("shorts")) {
                return new BlockDecision(false, "YouTube Shorts not allowed in Study Mode");
            }

            boolean hasSubscribe = joined.contains("subscribe");
            boolean hasEngagement = joined.contains("like") || joined.contains("share");
            boolean hasVideoMetadata = joined.contains("views") || joined.contains("subscribers");
            boolean hasPlayerControls = joined.contains("play") || joined.contains("pause") || joined.contains("seekbar");

            boolean isWatchPage = (hasSubscribe && hasEngagement) ||
                                   (hasSubscribe && hasVideoMetadata) ||
                                   (hasPlayerControls && hasSubscribe) ||
                                   (resourceIds.contains("watch") && hasPlayerControls);

            if (isWatchPage) {
                String channelName = extractChannelName(rootNode);
                String channelHandle = extractChannelHandle(joined);

                if (channelName == null && channelHandle == null) {
                    return new BlockDecision(false, "Cannot verify channel — blocked by Study Mode");
                }

                boolean isWhitelisted = false;
                if (channelName != null) {
                    for (String wl : FocusForgeConfig.youtubeWhitelistChannels) {
                        if (normalize(channelName).contains(normalize(wl)) ||
                            normalize(wl).contains(normalize(channelName))) {
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

                if (isWhitelisted) return null;
                String who = channelName != null ? channelName : channelHandle;
                return new BlockDecision(false, "\"" + who + "\" not on whitelist");
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, "evaluateYoutubeStudyMode error", e);
            return null;
        }
    }

    private String extractChannelName(AccessibilityNodeInfo rootNode) {
        try {
            String allDesc = getAllContentDescriptions(rootNode);
            String[] parts = allDesc.split("\\|");
            for (String part : parts) {
                String lower = part.toLowerCase().trim();
                if (lower.contains("subscribe")) {
                    String before = part.substring(0, part.toLowerCase().indexOf("subscribe")).trim();
                    before = before.replaceAll("[·|•]", "").trim();
                    if (before.length() >= 2 && before.length() <= 60) return before;
                    String after = part.substring(part.toLowerCase().indexOf("subscribe") + 9).trim();
                    after = after.replaceAll("[·|•]", "").trim();
                    if (after.length() >= 2 && after.length() <= 60) return after;
                }
            }
            String allText = getAllTextJoined(rootNode);
            if (allText.contains("·@") || allText.contains(" · @")) {
                String name = allText.split("·@")[0].split(" · @")[0].trim();
                if (name.length() >= 2 && name.length() <= 60) return name;
            }
        } catch (Exception e) {
            Log.e(TAG, "extractChannelName error", e);
        }
        return null;
    }

    private String extractChannelHandle(String joined) {
        String[] parts = joined.split("\\s+");
        for (String part : parts) {
            if (part.startsWith("@") && part.length() >= 3) return part;
        }
        return null;
    }

    private String checkWebsiteBlocks(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return null;
        try {
            String joined = getAllTextJoined(rootNode).toLowerCase();
            String urlBar = extractUrlBar(rootNode);
            if (urlBar != null) {
                for (String domain : FocusForgeConfig.blockedDomains) {
                    String clean = domain.toLowerCase().replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
                    if (clean.length() > 2 && urlBar.contains(clean)) return domain;
                }
            }
            for (String domain : FocusForgeConfig.blockedDomains) {
                String clean = domain.toLowerCase().replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
                if (clean.length() > 2 && joined.contains(clean)) return domain;
            }
        } catch (Exception e) {
            Log.e(TAG, "checkWebsiteBlocks error", e);
        }
        return null;
    }

    private String extractUrlBar(AccessibilityNodeInfo node) {
        if (node == null) return null;
        try {
            if (node.isEditable()) {
                CharSequence text = node.getText();
                if (text != null) return text.toString().toLowerCase();
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    String result = extractUrlBar(child);
                    child.recycle();
                    if (result != null) return result;
                }
            }
        } catch (Exception e) { /* ignore */ }
        return null;
    }

    private String getAllTextJoined(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectText(node, sb, 0);
        return sb.toString().toLowerCase();
    }

    private void collectText(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 8) return;
        try {
            CharSequence text = node.getText();
            if (text != null && text.length() > 0) sb.append(" ").append(text.toString().toLowerCase());
            CharSequence desc = node.getContentDescription();
            if (desc != null && desc.length() > 0) sb.append(" ").append(desc.toString().toLowerCase());
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    collectText(child, sb, depth + 1);
                    child.recycle();
                }
            }
        } catch (Exception e) { /* ignore */ }
    }

    private String getAllResourceIds(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectResourceIds(node, sb, 0);
        return sb.toString().toLowerCase();
    }

    private void collectResourceIds(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 8) return;
        try {
            CharSequence viewId = node.getViewIdResourceName();
            if (viewId != null) sb.append(" ").append(viewId.toLowerCase());
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    collectResourceIds(child, sb, depth + 1);
                    child.recycle();
                }
            }
        } catch (Exception e) { /* ignore */ }
    }

    private String getAllContentDescriptions(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectDescriptions(node, sb, 0);
        return sb.toString();
    }

    private void collectDescriptions(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 8) return;
        try {
            CharSequence desc = node.getContentDescription();
            if (desc != null && desc.length() > 0) sb.append(desc.toString()).append("|");
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    collectDescriptions(child, sb, depth + 1);
                    child.recycle();
                }
            }
        } catch (Exception e) { /* ignore */ }
    }

    private String normalize(String s) {
        return s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private void showOverlay(String packageName, String reason) {
        try {
            Intent intent = new Intent(this, BlockOverlayService.class);
            intent.putExtra("PACKAGE_NAME", packageName);
            intent.putExtra("REASON", reason);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "showOverlay failed", e);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted");
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
