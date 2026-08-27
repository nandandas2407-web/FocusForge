package com.focusforge.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final Set<String> BROWSER_PACKAGES = new HashSet<>(Arrays.asList(
        "com.android.chrome",
        "com.sec.android.app.sbrowser",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.microsoft.emmx",
        "com.brave.browser"
    ));

    private static final Set<String> YOUTUBE_PACKAGES = new HashSet<>(Arrays.asList(
        "com.google.android.youtube",
        "com.google.android.apps.youtube.music"
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
        Log.d(TAG, "Service connected");

        try {
            AccessibilityServiceInfo info = getServiceInfo();
            if (info != null) {
                info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                               AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
                setServiceInfo(info);
            }
        } catch (Exception e) {
            Log.e(TAG, "onServiceConnected setup failed", e);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        if (!FocusForgeConfig.globalBlockerEnabled) return;

        try {
            processEvent(event);
        } catch (Exception e) {
            Log.e(TAG, "onAccessibilityEvent error", e);
        }
    }

    private void processEvent(AccessibilityEvent event) {
        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        String packageName = pkgCs.toString();
        if (packageName.equals(getPackageName())) return;

        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        // FULL APP BLOCK — check on every event type for consistency
        if (FocusForgeConfig.blockedPackages.contains(packageName)) {
            Log.d(TAG, "BLOCKED app: " + packageName);
            dismissOverlayIfExists();
            performHome();
            showOverlay(packageName, appNameFromPackage(packageName) + " is blocked");
            return;
        }

        // INSTAGRAM REELS — check on window state change and content change
        if (FocusForgeConfig.reelsShortsBlockingEnabled &&
            "com.instagram.android".equals(packageName) &&
            FocusForgeConfig.reelsBlockedPackages.contains(packageName)) {
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                if (detectInstagramReels(rootNode)) {
                    Log.d(TAG, "BLOCKED Instagram Reels");
                    dismissOverlayIfExists();
                    performBack();
                    showOverlay(packageName, "Instagram Reels blocked");
                    return;
                }
            }
        }

        // YOUTUBE SHORTS — check on window state change and content change
        if (FocusForgeConfig.reelsShortsBlockingEnabled &&
            "com.google.android.youtube".equals(packageName) &&
            FocusForgeConfig.shortsBlockedPackages.contains(packageName)) {
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                if (detectYoutubeShorts(rootNode)) {
                    Log.d(TAG, "BLOCKED YouTube Shorts");
                    dismissOverlayIfExists();
                    performBack();
                    showOverlay(packageName, "YouTube Shorts blocked");
                    return;
                }
            }
        }

        // YOUTUBE STUDY MODE — only on window state change (new screen)
        if ("com.google.android.youtube".equals(packageName) &&
            FocusForgeConfig.youtubeStudyModeEnabled &&
            eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (isWatchPage(rootNode)) {
                String channel = extractChannelFromWatchPage(rootNode);
                if (channel != null && !isChannelWhitelisted(channel)) {
                    Log.d(TAG, "BLOCKED non-study channel: " + channel);
                    dismissOverlayIfExists();
                    performBack();
                    showOverlay(packageName, "\"" + channel + "\" is not a study channel");
                    return;
                }
                if (channel == null) {
                    // Can't identify channel — block to be safe
                    Log.d(TAG, "BLOCKED unknown channel in study mode");
                    dismissOverlayIfExists();
                    performBack();
                    showOverlay(packageName, "Cannot verify channel — blocked in Study Mode");
                    return;
                }
            }
        }

        // WEBSITE BLOCKER — check on window state change only
        if (BROWSER_PACKAGES.contains(packageName) &&
            !FocusForgeConfig.blockedDomains.isEmpty() &&
            eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String blocked = checkWebsiteBlocks(rootNode);
            if (blocked != null) {
                Log.d(TAG, "BLOCKED website: " + blocked);
                dismissOverlayIfExists();
                performBack();
                showOverlay(packageName, blocked + " is blocked");
                return;
            }
        }
    }

    private void dismissOverlayIfExists() {
        try {
            BlockOverlayService.dismissCurrent();
        } catch (Exception e) {
            Log.e(TAG, "dismissOverlayIfExists error", e);
        }
    }

    // ── Instagram Reels Detection ───────────────────────────────────
    private boolean detectInstagramReels(AccessibilityNodeInfo root) {
        if (root == null) return false;
        try {
            String text = getAllText(root);
            String ids = getAllResourceIds(root);

            boolean hasReelsIndicators = ids.contains("reels") ||
                                          ids.contains("clips") ||
                                          text.contains("reel") ||
                                          text.contains("clips_viewer") ||
                                          text.contains("use audio") ||
                                          text.contains("original audio") ||
                                          text.contains("remix");

            boolean hasMainNav = text.contains("direct") ||
                                 text.contains("messenger") ||
                                 text.contains("activity") ||
                                 text.contains("new post");

            return hasReelsIndicators && !hasMainNav;
        } catch (Exception e) {
            return false;
        }
    }

    // ── YouTube Shorts Detection ────────────────────────────────────
    private boolean detectYoutubeShorts(AccessibilityNodeInfo root) {
        if (root == null) return false;
        try {
            String text = getAllText(root);
            String ids = getAllResourceIds(root);

            boolean hasShortsUI = ids.contains("reel_player") ||
                                   ids.contains("shorts_player") ||
                                   ids.contains("ytd-reel") ||
                                   text.contains("shorts") ||
                                   text.contains("remix") ||
                                   text.contains("use this sound");

            boolean hasNavBar = text.contains("home") &&
                                text.contains("subscriptions") &&
                                text.contains("you");

            return hasShortsUI && !hasNavBar;
        } catch (Exception e) {
            return false;
        }
    }

    // ── YouTube Study Mode ──────────────────────────────────────────
    private boolean isWatchPage(AccessibilityNodeInfo root) {
        if (root == null) return false;
        try {
            String text = getAllText(root);
            String ids = getAllResourceIds(root);

            boolean hasPlayer = ids.contains("player") ||
                                 ids.contains("watch") ||
                                 text.contains("subscribe") ||
                                 text.contains("like") ||
                                 text.contains("share");

            boolean hasVideoUI = (text.contains("subscribe") && text.contains("like")) ||
                                  (text.contains("subscribe") && text.contains("views")) ||
                                  ids.contains("watch-player");

            return hasPlayer && hasVideoUI;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractChannelFromWatchPage(AccessibilityNodeInfo root) {
        if (root == null) return null;
        try {
            // Method 1: Content descriptions with "subscribe" pattern
            String allDesc = getAllContentDescriptions(root);
            for (String desc : allDesc.split("\\|")) {
                String lower = desc.toLowerCase().trim();
                if (lower.contains("subscribe")) {
                    String before = desc.substring(0, desc.toLowerCase().indexOf("subscribe")).trim();
                    before = before.replaceAll("[·|•]", "").trim();
                    if (before.length() >= 2 && before.length() <= 80) return before;

                    String after = desc.substring(desc.toLowerCase().indexOf("subscribe") + 9).trim();
                    after = after.replaceAll("[·|•]", "").trim();
                    if (after.length() >= 2 && after.length() <= 80) return after;
                }
            }

            // Method 2: Look for @handle pattern
            String allText = getAllText(root);
            String[] words = allText.split("\\s+");
            for (String word : words) {
                if (word.startsWith("@") && word.length() >= 3) {
                    return word;
                }
            }

            // Method 3: Look for "· @" pattern
            if (allText.contains("·@") || allText.contains(" · @")) {
                String name = allText.split("·@")[0].split(" · @")[0].trim();
                if (name.length() >= 2 && name.length() <= 80) return name;
            }
        } catch (Exception e) {
            Log.e(TAG, "extractChannel error", e);
        }
        return null;
    }

    private boolean isChannelWhitelisted(String channel) {
        String normalized = normalize(channel);
        for (String wl : FocusForgeConfig.youtubeWhitelistChannels) {
            if (normalized.contains(normalize(wl)) || normalize(wl).contains(normalized)) {
                return true;
            }
        }
        for (String handle : FocusForgeConfig.youtubeWhitelistHandles) {
            if (normalized.contains(normalize(handle)) || normalize(handle).contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    // ── Website Blocker ─────────────────────────────────────────────
    private String checkWebsiteBlocks(AccessibilityNodeInfo root) {
        if (root == null) return null;
        try {
            // Check URL bar first
            String url = extractUrlBar(root);
            if (url != null) {
                for (String domain : FocusForgeConfig.blockedDomains) {
                    String clean = domain.toLowerCase().replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
                    if (clean.length() > 2 && url.contains(clean)) return domain;
                }
            }
            // Fallback: check all visible text
            String text = getAllText(root);
            for (String domain : FocusForgeConfig.blockedDomains) {
                String clean = domain.toLowerCase().replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
                if (clean.length() > 2 && text.contains(clean)) return domain;
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

    // ── UI Text Extraction ──────────────────────────────────────────
    private String getAllText(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectText(node, sb, 0);
        return sb.toString().toLowerCase();
    }

    private void collectText(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 10) return;
        try {
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
        } catch (Exception e) { /* continue */ }
    }

    private String getAllResourceIds(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectResourceIds(node, sb, 0);
        return sb.toString().toLowerCase();
    }

    private void collectResourceIds(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 10) return;
        try {
            CharSequence viewId = node.getViewIdResourceName();
            if (viewId != null) sb.append(" ").append(viewId.toString().toLowerCase());
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    collectResourceIds(child, sb, depth + 1);
                    child.recycle();
                }
            }
        } catch (Exception e) { /* continue */ }
    }

    private String getAllContentDescriptions(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();
        collectDescriptions(node, sb, 0);
        return sb.toString();
    }

    private void collectDescriptions(AccessibilityNodeInfo node, StringBuilder sb, int depth) {
        if (node == null || depth > 10) return;
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
        } catch (Exception e) { /* continue */ }
    }

    // ── Actions ─────────────────────────────────────────────────────
    private void performHome() {
        mainHandler.post(() -> {
            try {
                performGlobalAction(GLOBAL_ACTION_HOME);
            } catch (Exception e) {
                Log.e(TAG, "performHome failed", e);
            }
        });
    }

    private void performBack() {
        mainHandler.post(() -> {
            try {
                performGlobalAction(GLOBAL_ACTION_BACK);
            } catch (Exception e) {
                Log.e(TAG, "performBack failed", e);
            }
        });
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

    // ── Helpers ─────────────────────────────────────────────────────
    private String normalize(String s) {
        return s.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    private String appNameFromPackage(String pkg) {
        switch (pkg) {
            case "com.instagram.android": return "Instagram";
            case "com.zhiliaoapp.musically": return "TikTok";
            case "com.twitter.android": return "Twitter";
            case "com.facebook.katana": return "Facebook";
            case "com.reddit.frontpage": return "Reddit";
            case "com.netflix.mediaclient": return "Netflix";
            case "com.google.android.youtube": return "YouTube";
            default: return pkg.substring(pkg.lastIndexOf('.') + 1);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
