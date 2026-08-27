package com.focusforge.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FocusForgeConfig {
    private static final String TAG = "FocusForge";
    private static final String PREFS_NAME = "focusforge_config";
    private static SharedPreferences prefs;

    public static Set<String> blockedPackages = ConcurrentHashMap.newKeySet();
    public static Set<String> reelsBlockedPackages = ConcurrentHashMap.newKeySet();
    public static Set<String> shortsBlockedPackages = ConcurrentHashMap.newKeySet();
    public static boolean reelsShortsBlockingEnabled = true;
    public static boolean youtubeStudyModeEnabled = false;
    public static Set<String> youtubeWhitelistChannels = ConcurrentHashMap.newKeySet();
    public static Set<String> youtubeWhitelistHandles = ConcurrentHashMap.newKeySet();
    public static Set<String> blockedDomains = ConcurrentHashMap.newKeySet();
    public static boolean globalBlockerEnabled = true;

    static {
        reelsBlockedPackages.add("com.instagram.android");
        shortsBlockedPackages.add("com.google.android.youtube");
    }

    public static void init(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        load();
    }

    public static void save() {
        if (prefs == null) return;
        SharedPreferences.Editor ed = prefs.edit();
        ed.putBoolean("globalBlockerEnabled", globalBlockerEnabled);
        ed.putBoolean("youtubeStudyModeEnabled", youtubeStudyModeEnabled);
        ed.putBoolean("reelsShortsBlockingEnabled", reelsShortsBlockingEnabled);
        ed.putStringSet("blockedPackages", new HashSet<>(blockedPackages));
        ed.putStringSet("blockedDomains", new HashSet<>(blockedDomains));
        ed.putStringSet("youtubeWhitelistChannels", new HashSet<>(youtubeWhitelistChannels));
        ed.putStringSet("youtubeWhitelistHandles", new HashSet<>(youtubeWhitelistHandles));
        ed.apply();
    }

    private static void load() {
        if (prefs == null) return;
        globalBlockerEnabled = prefs.getBoolean("globalBlockerEnabled", true);
        youtubeStudyModeEnabled = prefs.getBoolean("youtubeStudyModeEnabled", false);
        reelsShortsBlockingEnabled = prefs.getBoolean("reelsShortsBlockingEnabled", true);

        Set<String> savedPackages = prefs.getStringSet("blockedPackages", null);
        if (savedPackages != null) {
            blockedPackages.clear();
            blockedPackages.addAll(savedPackages);
        } else {
            blockedPackages.addAll(Arrays.asList(
                "com.zhiliaoapp.musically",
                "com.twitter.android",
                "com.reddit.frontpage",
                "com.netflix.mediaclient",
                "com.twitch.android.app"
            ));
        }

        Set<String> savedDomains = prefs.getStringSet("blockedDomains", null);
        if (savedDomains != null) {
            blockedDomains.clear();
            blockedDomains.addAll(savedDomains);
        } else {
            blockedDomains.addAll(Arrays.asList("reddit.com", "twitter.com", "x.com", "tiktok.com"));
        }

        Set<String> savedChannels = prefs.getStringSet("youtubeWhitelistChannels", null);
        if (savedChannels != null) {
            youtubeWhitelistChannels.clear();
            youtubeWhitelistChannels.addAll(savedChannels);
        } else {
            youtubeWhitelistChannels.addAll(Arrays.asList(
                "MIT OpenCourseWare", "Kurzgesagt", "freeCodeCamp.org"
            ));
        }

        Set<String> savedHandles = prefs.getStringSet("youtubeWhitelistHandles", null);
        if (savedHandles != null) {
            youtubeWhitelistHandles.clear();
            youtubeWhitelistHandles.addAll(savedHandles);
        }
    }
}
