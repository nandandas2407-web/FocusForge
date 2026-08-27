package com.focusforge.app;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FocusForgeConfig {
    private static final String TAG = "FocusForge";

    public static Set<String> blockedPackages = new HashSet<>();
    public static Set<String> reelsBlockedPackages = new HashSet<>();
    public static Set<String> shortsBlockedPackages = new HashSet<>();
    public static boolean reelsShortsBlockingEnabled = true;
    public static boolean youtubeStudyModeEnabled = false;
    public static Set<String> youtubeWhitelistChannels = new HashSet<>(Arrays.asList(
        "MIT OpenCourseWare",
        "Kurzgesagt",
        "freeCodeCamp.org"
    ));
    public static Set<String> youtubeWhitelistHandles = new HashSet<>();
    public static Set<String> blockedDomains = new HashSet<>(Arrays.asList(
        "reddit.com",
        "twitter.com",
        "x.com",
        "tiktok.com"
    ));
    public static boolean globalBlockerEnabled = true;

    // Default blocked apps
    static {
        blockedPackages.add("com.zhiliaoapp.musically"); // TikTok
        blockedPackages.add("com.twitter.android");      // Twitter/X
        blockedPackages.add("com.reddit.frontpage");     // Reddit
        blockedPackages.add("com.netflix.mediaclient");  // Netflix
        blockedPackages.add("com.twitch.android.app");   // Twitch
        reelsBlockedPackages.add("com.instagram.android");
        shortsBlockedPackages.add("com.google.android.youtube");
    }
}
