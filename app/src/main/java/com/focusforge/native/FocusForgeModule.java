package com.focusforge.native;

import android.content.Intent;
import android.os.Build;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashSet;
import java.util.Set;

/**
 * React Native bridge module for FocusForge native blocking service.
 * Allows JavaScript to configure blocking rules that the AccessibilityService enforces.
 */
public class FocusForgeModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public FocusForgeModule(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @Override
    public String getName() {
        return "FocusForgeModule";
    }

    @ReactMethod
    public void startOverlayService(String packageName, String reason) {
        Intent intent = new Intent(reactContext, BlockOverlayService.class);
        intent.putExtra("PACKAGE_NAME", packageName);
        intent.putExtra("REASON", reason);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reactContext.startForegroundService(intent);
        } else {
            reactContext.startService(intent);
        }
    }

    @ReactMethod
    public void setBlockedApps(ReadableArray apps) {
        Set<String> blocked = new HashSet<>();
        Set<String> reels = new HashSet<>();
        Set<String> shorts = new HashSet<>();

        for (int i = 0; i < apps.size(); i++) {
            ReadableMap app = apps.getMap(i);
            if (app == null) continue;

            String pkg = app.getString("packageName");
            if (pkg == null) continue;

            if (app.getBoolean("isFullyBlocked")) blocked.add(pkg);
            if (app.getBoolean("isReelsBlocked")) reels.add(pkg);
            if (app.getBoolean("isShortsBlocked")) shorts.add(pkg);
        }

        FocusForgeAccessibilityService service = FocusForgeAccessibilityService.getInstance();
        if (service != null) {
            service.setBlockedPackages(blocked);
            service.setReelsBlockedPackages(reels);
            service.setShortsBlockedPackages(shorts);
        }
    }

    @ReactMethod
    public void setYoutubeStudyMode(boolean enabled) {
        FocusForgeAccessibilityService service = FocusForgeAccessibilityService.getInstance();
        if (service != null) {
            service.setYoutubeStudyMode(enabled);
        }
    }

    @ReactMethod
    public void setYoutubeWhitelist(ReadableArray channels) {
        Set<String> channelNames = new HashSet<>();
        Set<String> handles = new HashSet<>();

        for (int i = 0; i < channels.size(); i++) {
            ReadableMap ch = channels.getMap(i);
            if (ch == null) continue;
            String title = ch.getString("channelTitle");
            String id = ch.getString("channelId");
            if (title != null) channelNames.add(title);
            if (id != null) handles.add(id);
        }

        FocusForgeAccessibilityService service = FocusForgeAccessibilityService.getInstance();
        if (service != null) {
            service.setYoutubeWhitelist(channelNames, handles);
        }
    }

    @ReactMethod
    public void setBlockedWebsites(ReadableArray domains) {
        Set<String> blocked = new HashSet<>();
        for (int i = 0; i < domains.size(); i++) {
            ReadableMap dm = domains.getMap(i);
            if (dm != null) {
                String domain = dm.getString("domain");
                if (domain != null) blocked.add(domain);
            }
        }
        FocusForgeAccessibilityService service = FocusForgeAccessibilityService.getInstance();
        if (service != null) {
            service.setBlockedDomains(blocked);
        }
    }

    @ReactMethod
    public void setGlobalBlockerEnabled(boolean enabled) {
        FocusForgeAccessibilityService service = FocusForgeAccessibilityService.getInstance();
        if (service != null) {
            service.setGlobalBlockerEnabled(enabled);
        }
    }

    @ReactMethod
    public void isAccessibilityServiceEnabled(com.facebook.react.bridge.Callback callback) {
        boolean enabled = FocusForgeAccessibilityService.getInstance() != null;
        callback.invoke(enabled);
    }
}
