package com.focusforge.app;

import android.util.Log;

/**
 * React Native bridge module stub.
 * In the standalone APK build, FocusForgeConfig handles configuration directly.
 * When built as a React Native app, this would bridge JS to native.
 */
public class FocusForgeModule {
    private static final String TAG = "FocusForge";

    public static void setBlockedApps(java.util.List<String> apps) {
        FocusForgeConfig.blockedPackages.clear();
        for (String pkg : apps) {
            FocusForgeConfig.blockedPackages.add(pkg);
        }
        Log.d(TAG, "Blocked apps updated: " + apps.size());
    }

    public static void setYoutubeStudyMode(boolean enabled) {
        FocusForgeConfig.youtubeStudyModeEnabled = enabled;
        Log.d(TAG, "YouTube Study Mode: " + enabled);
    }

    public static void setGlobalBlockerEnabled(boolean enabled) {
        FocusForgeConfig.globalBlockerEnabled = enabled;
        Log.d(TAG, "Global Blocker: " + enabled);
    }
}
